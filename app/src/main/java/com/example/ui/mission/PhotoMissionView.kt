package com.example.ui.mission

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mission.PhotoVerificationEngine
import com.example.mission.PhotoVerificationResult
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamGreen
import com.example.ui.theme.GundamWhite
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

data class PhotoTargetInfo(
    val spotKey: String,
    val spotLabel: String,
    val referenceUriString: String? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoMissionView(
    targets: List<PhotoTargetInfo>,
    onMissionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTargets = if (targets.isNotEmpty()) targets else listOf(
        PhotoTargetInfo("TOILET", "Toilet / Kamar Mandi", null)
    )
    var currentTargetIndex by remember { mutableIntStateOf(0) }
    val currentTarget = activeTargets.getOrElse(currentTargetIndex) { activeTargets.first() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<PhotoVerificationResult?>(null) }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
            } catch (e: Exception) {
                Log.e("PhotoMission", "Error unbinding camera", e)
            }
        }
    }

    LaunchedEffect(lensFacing, cameraProvider, previewViewRef, capturedBitmap) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val pv = previewViewRef ?: return@LaunchedEffect
        if (cameraPermissionState.status.isGranted && capturedBitmap == null) {
            try {
                provider.unbindAll()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = pv.surfaceProvider
                }
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("PhotoMission", "Camera rebinding failed", exc)
            }
        } else if (capturedBitmap != null) {
            try {
                provider.unbindAll()
            } catch (exc: Exception) {
                Log.e("PhotoMission", "Camera unbind failed on capture", exc)
            }
        }
    }

    // Load reference bitmap for CURRENT target
    var referenceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(currentTarget.referenceUriString) {
        val refUri = currentTarget.referenceUriString
        if (!refUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(refUri)
                    val bmp = if (uri.scheme == "file" || uri.path?.startsWith("/") == true) {
                        val file = if (uri.path != null) java.io.File(uri.path!!) else java.io.File(refUri)
                        if (file.exists()) {
                            BitmapFactory.decodeFile(file.absolutePath)
                        } else {
                            val input: InputStream? = context.contentResolver.openInputStream(uri)
                            BitmapFactory.decodeStream(input)
                        }
                    } else {
                        val input: InputStream? = context.contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(input)
                    }
                    referenceBitmap = bmp
                } catch (e: Exception) {
                    Log.e("PhotoMission", "Error loading reference image from $refUri", e)
                    referenceBitmap = null
                }
            }
        } else {
            referenceBitmap = null
        }
    }

    fun proceedToNextTargetOrFinish() {
        if (currentTargetIndex < activeTargets.size - 1) {
            currentTargetIndex++
            capturedBitmap = null
            verificationResult = null
        } else {
            onMissionSuccess()
        }
    }

    fun verifyImage(
        bitmap: Bitmap,
        target: String,
        refBmp: Bitmap?,
        onResult: (PhotoVerificationResult) -> Unit
    ) {
        isVerifying = true
        scope.launch {
            val result = PhotoVerificationEngine.verifyPhoto(bitmap, target, refBmp)
            isVerifying = false
            onResult(result)
        }
    }

    // Gallery picker fallback
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val input: InputStream? = context.contentResolver.openInputStream(it)
                    val bmp = BitmapFactory.decodeStream(input)
                    if (bmp != null) {
                        capturedBitmap = bmp
                        verifyImage(bmp, currentTarget.spotKey, referenceBitmap) { res ->
                            verificationResult = res
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PhotoMission", "Error opening gallery image", e)
                }
            }
        }
    }

    fun takePhoto() {
        val capture = imageCapture ?: return
        val executor = ContextCompat.getMainExecutor(context)

        capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageProxy.close()

                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0,
                    originalBitmap.width, originalBitmap.height,
                    matrix, true
                )

                capturedBitmap = rotatedBitmap
                verifyImage(rotatedBitmap, currentTarget.spotKey, referenceBitmap) { res ->
                    verificationResult = res
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("PhotoMission", "Camera capture error", exception)
            }
        })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Target Header Card with Multi-Photo Progress & Reference Thumbnail
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Multi-photo step badge if > 1 target
                if (activeTargets.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = GundamBlue,
                            modifier = Modifier.testTag("photo_target_step_badge")
                        ) {
                            Text(
                                text = "Foto Spot ${currentTargetIndex + 1} dari ${activeTargets.size}",
                                color = GundamWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "${activeTargets.size - currentTargetIndex - 1} spot tersisa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (referenceBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                bitmap = referenceBitmap!!.asImageBitmap(),
                                contentDescription = "Foto Referensi Terdaftar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Wc,
                                    contentDescription = "Target Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Target: ${currentTarget.spotLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (referenceBitmap != null)
                                "Wajib cocok dengan foto ${currentTarget.spotLabel} terdaftar (minimal kemiripan 79%)."
                            else
                                "Arahkan kamera ke area ${currentTarget.spotLabel} untuk verifikasi bangun tidur.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Viewfinder / Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!cameraPermissionState.status.isGranted) {
                // Permission Request View
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Permission",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Izin Kamera Diperlukan",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aplikasi membutuhkan akses kamera untuk mengambil foto spot bangun tidur dan mematikan alarm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        modifier = Modifier.testTag("request_camera_button")
                    ) {
                        Text("Izinkan Akses Kamera")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.testTag("choose_gallery_button")
                    ) {
                        Text("Pilih dari Galeri (Uji Coba)")
                    }
                }
            } else if (capturedBitmap != null) {
                // Show captured image preview with verification result
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Photo",
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isVerifying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Menganalisis foto ${currentTarget.spotLabel}...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                // Live CameraX Preview with COMPATIBLE TextureView
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        previewViewRef = previewView
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            cameraProvider = provider
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    onRelease = {
                        try {
                            cameraProvider?.unbindAll()
                        } catch (e: Exception) {
                            Log.e("PhotoMission", "onRelease unbind failed", e)
                        }
                        previewViewRef = null
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Guide Frame
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                            .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feedback / Result Notice
        AnimatedVisibility(
            visible = verificationResult != null,
            enter = fadeIn() + scaleIn()
        ) {
            verificationResult?.let { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isSuccess) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "Status",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = res.message,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = res.details,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (capturedBitmap != null) {
                // Retry Button
                FilledTonalButton(
                    onClick = {
                        capturedBitmap = null
                        verificationResult = null
                    },
                    modifier = Modifier.testTag("retry_photo_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Ulangi")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ambil Ulang")
                }

                // If success button -> advance to next or finish
                if (verificationResult?.isSuccess == true) {
                    val isLast = currentTargetIndex >= activeTargets.size - 1
                    Button(
                        onClick = { proceedToNextTargetOrFinish() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.testTag("finish_photo_mission_button")
                    ) {
                        Icon(
                            imageVector = if (isLast) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                            contentDescription = if (isLast) "Selesai" else "Lanjut"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLast) "Matikan Alarm" else "Spot Berikutnya (${currentTargetIndex + 2}/${activeTargets.size})")
                    }
                }
            } else {
                // Switch Camera Lens
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("switch_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Ganti Kamera"
                    )
                }

                // Shutter Capture Button
                Surface(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            takePhoto()
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .testTag("capture_shutter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ambil Foto",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Gallery Fallback Button
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .testTag("open_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Pilih Galeri"
                    )
                }
            }
        }
    }
}
