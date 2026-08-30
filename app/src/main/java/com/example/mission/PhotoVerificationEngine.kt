package com.example.mission

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class PhotoVerificationResult(
    val isSuccess: Boolean,
    val confidenceScore: Float,
    val message: String,
    val targetType: String,
    val details: String
)

object PhotoVerificationEngine {

    /**
     * Verifies the captured photo against a registered reference spot photo (strict comparison >= 79%)
     * or fallback spot visual heuristic rules.
     */
    suspend fun verifyPhoto(
        bitmap: Bitmap,
        targetPlace: String,
        referenceBitmap: Bitmap? = null
    ): PhotoVerificationResult = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height

        if (width < 40 || height < 40) {
            return@withContext PhotoVerificationResult(
                isSuccess = false,
                confidenceScore = 0.1f,
                message = "Foto terlalu kecil atau buram. Coba ambil ulang.",
                targetType = targetPlace,
                details = "Ukuran gambar tidak memadai"
            )
        }

        // Downscale for robust pixel analysis & structural comparison
        val sampleSize = 64
        val scaledCaptured = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)

        var totalLuminance = 0.0
        var totalRed = 0L
        var totalGreen = 0L
        var totalBlue = 0L
        var brightPixelCount = 0
        var highContrastCount = 0

        val capturedPixels = IntArray(sampleSize * sampleSize)
        scaledCaptured.getPixels(capturedPixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)

        for (i in capturedPixels.indices) {
            val pixel = capturedPixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            totalRed += r
            totalGreen += g
            totalBlue += b

            // Relative luminance (standard rec601)
            val lum = (0.299 * r + 0.587 * g + 0.114 * b)
            totalLuminance += lum

            if (lum > 140) brightPixelCount++
            if (abs(r - g) < 25 && abs(g - b) < 25 && lum > 90) {
                // Neutral / Porcelain / Tile / Ceramic tones
                highContrastCount++
            }
        }

        val totalPixels = sampleSize * sampleSize
        val avgLuminance = (totalLuminance / totalPixels).toFloat()

        // 1. Darkness / Covered Camera Check
        // If user covers the camera with hand, pillow, bedsheet or takes in total darkness
        if (avgLuminance < 26f) {
            return@withContext PhotoVerificationResult(
                isSuccess = false,
                confidenceScore = 0.1f,
                message = "Foto terlalu gelap atau kamera tertutup! Nyalakan lampu atau dekati spot $targetPlace.",
                targetType = targetPlace,
                details = "Kecerahan: ${avgLuminance.toInt()}/255 (Minimal 26)"
            )
        }

        // 2. Strict Reference Comparison (When user registered a reference spot photo)
        if (referenceBitmap != null) {
            val scaledRef = Bitmap.createScaledBitmap(referenceBitmap, sampleSize, sampleSize, true)
            val refPixels = IntArray(sampleSize * sampleSize)
            scaledRef.getPixels(refPixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)

            // A. Color Histogram Comparison (8 bins per channel = 512 bins)
            val refHist = FloatArray(8 * 8 * 8)
            val capHist = FloatArray(8 * 8 * 8)

            for (i in 0 until totalPixels) {
                val p1 = capturedPixels[i]
                val r1 = (Color.red(p1) / 32).coerceIn(0, 7)
                val g1 = (Color.green(p1) / 32).coerceIn(0, 7)
                val b1 = (Color.blue(p1) / 32).coerceIn(0, 7)
                capHist[r1 * 64 + g1 * 8 + b1] += 1f

                val p2 = refPixels[i]
                val r2 = (Color.red(p2) / 32).coerceIn(0, 7)
                val g2 = (Color.green(p2) / 32).coerceIn(0, 7)
                val b2 = (Color.blue(p2) / 32).coerceIn(0, 7)
                refHist[r2 * 64 + g2 * 8 + b2] += 1f
            }

            // Normalize histograms
            for (k in 0 until 512) {
                refHist[k] /= totalPixels.toFloat()
                capHist[k] /= totalPixels.toFloat()
            }

            // Histogram Intersection score
            var histIntersection = 0f
            for (k in 0 until 512) {
                histIntersection += min(refHist[k], capHist[k])
            }

            // B. Spatial / Grid Block Structural Comparison (4x4 grid blocks)
            val blockSize = sampleSize / 4 // 16x16 pixels per block
            var blockMatchSum = 0f

            for (by in 0 until 4) {
                for (bx in 0 until 4) {
                    var capLumSum = 0.0
                    var refLumSum = 0.0
                    var capRSum = 0.0
                    var capGSum = 0.0
                    var capBSum = 0.0
                    var refRSum = 0.0
                    var refGSum = 0.0
                    var refBSum = 0.0

                    for (y in 0 until blockSize) {
                        for (x in 0 until blockSize) {
                            val pxIdx = (by * blockSize + y) * sampleSize + (bx * blockSize + x)
                            val pCap = capturedPixels[pxIdx]
                            val pRef = refPixels[pxIdx]

                            val cr = Color.red(pCap)
                            val cg = Color.green(pCap)
                            val cb = Color.blue(pCap)
                            capRSum += cr
                            capGSum += cg
                            capBSum += cb
                            capLumSum += (0.299 * cr + 0.587 * cg + 0.114 * cb)

                            val rr = Color.red(pRef)
                            val rg = Color.green(pRef)
                            val rb = Color.blue(pRef)
                            refRSum += rr
                            refGSum += rg
                            refBSum += rb
                            refLumSum += (0.299 * rr + 0.587 * rg + 0.114 * rb)
                        }
                    }

                    val blockPixels = (blockSize * blockSize).toDouble()
                    val avgCapLum = capLumSum / blockPixels
                    val avgRefLum = refLumSum / blockPixels

                    val lumDiff = abs(avgCapLum - avgRefLum) / 255.0
                    val rDiff = abs((capRSum - refRSum) / blockPixels) / 255.0
                    val gDiff = abs((capGSum - refGSum) / blockPixels) / 255.0
                    val bDiff = abs((capBSum - refBSum) / blockPixels) / 255.0

                    val blockColorDiff = (lumDiff * 0.4 + (rDiff + gDiff + bDiff) / 3.0 * 0.6)
                    val blockSimilarity = max(0.0, 1.0 - blockColorDiff)
                    blockMatchSum += blockSimilarity.toFloat()
                }
            }

            val gridScore = (blockMatchSum / 16f).coerceIn(0f, 1f)

            // C. Pixel by Pixel RGB Euclidean Distance
            var pixelDiffSum = 0.0
            for (i in 0 until totalPixels) {
                val p1 = capturedPixels[i]
                val p2 = refPixels[i]
                val dr = (Color.red(p1) - Color.red(p2))
                val dg = (Color.green(p1) - Color.green(p2))
                val db = (Color.blue(p1) - Color.blue(p2))
                pixelDiffSum += sqrt((dr * dr + dg * dg + db * db).toDouble())
            }
            val maxPossibleDist = sqrt(255.0 * 255.0 * 3.0) * totalPixels
            val pixelEuclideanScore = (1.0 - (pixelDiffSum / maxPossibleDist)).toFloat().coerceIn(0f, 1f)

            // Combined multi-layer similarity:
            // 40% Color Histogram + 35% Spatial Grid Blocks + 25% Pixel Euclidean
            val combinedSimilarity = (histIntersection * 0.40f) + (gridScore * 0.35f) + (pixelEuclideanScore * 0.25f)
            val matchPercent = (combinedSimilarity * 100).toInt()

            // Robust threshold: 79% similarity allows for natural morning light shifts while rejecting invalid places
            val requiredThreshold = 0.79f

            if (combinedSimilarity >= requiredThreshold) {
                return@withContext PhotoVerificationResult(
                    isSuccess = true,
                    confidenceScore = combinedSimilarity,
                    message = "✅ Sangat Cocok dengan spot terdaftar ($matchPercent%)! Alarm berhasil dimatikan.",
                    targetType = targetPlace,
                    details = "Foto sesuai dengan referensi $targetPlace (Skor: $matchPercent%)"
                )
            } else {
                return@withContext PhotoVerificationResult(
                    isSuccess = false,
                    confidenceScore = combinedSimilarity,
                    message = "❌ Foto belum cocok dengan foto $targetPlace yang didaftarkan (Kemiripan $matchPercent%)",
                    targetType = targetPlace,
                    details = "Dibutuhkan kemiripan minimal ${(requiredThreshold * 100).toInt()}%. Arahkan sudut kamera persis ke spot $targetPlace yang telah kamu daftarkan."
                )
            }
        }

        // 3. Fallback Heuristics when NO reference photo was registered at all
        val porcelainRatio = highContrastCount.toFloat() / totalPixels
        val brightRatio = brightPixelCount.toFloat() / totalPixels

        val isTargetVerified = when (targetPlace.uppercase()) {
            "TOILET", "WC" -> {
                // Must have adequate lighting and room context
                avgLuminance in 30.0..250.0 && (porcelainRatio > 0.15f || brightRatio > 0.15f)
            }
            "SINK", "WASTAFEL" -> {
                avgLuminance in 30.0..250.0 && (porcelainRatio > 0.15f || brightRatio > 0.15f)
            }
            "KITCHEN", "DAPUR" -> {
                avgLuminance in 30.0..250.0 && (porcelainRatio > 0.12f || brightRatio > 0.12f)
            }
            else -> {
                avgLuminance in 30.0..250.0
            }
        }

        val calculatedScore = min(0.95f, max(0.40f, (avgLuminance / 255f) * 0.5f + (porcelainRatio * 0.5f)))

        if (isTargetVerified) {
            PhotoVerificationResult(
                isSuccess = true,
                confidenceScore = calculatedScore,
                message = "✅ Foto area $targetPlace terverifikasi! Hebat, kamu sudah bangun.",
                targetType = targetPlace,
                details = "Pola ruangan & pencahayaan terdeteksi (${(calculatedScore * 100).toInt()}%)"
            )
        } else {
            PhotoVerificationResult(
                isSuccess = false,
                confidenceScore = calculatedScore,
                message = "❌ Foto belum terdeteksi sebagai $targetPlace.",
                targetType = targetPlace,
                details = "Arahkan kamera ke spot $targetPlace dengan jelas dan pencahayaan terang."
            )
        }
    }
}
