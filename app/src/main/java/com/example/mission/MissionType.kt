package com.example.mission

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.ui.graphics.vector.ImageVector

enum class MissionType(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeColorHex: Long
) {
    PHOTO(
        title = "Misi Foto (Toilet / Tempat)",
        description = "Wajib foto toilet/kamar mandi atau tempat tertentu untuk mematikan alarm.",
        icon = Icons.Default.CameraAlt,
        badgeColorHex = 0xFFFF5722
    ),
    MATH(
        title = "Misi Matematika",
        description = "Selesaikan beberapa soal hitungan untuk mengaktifkan otak pagimu.",
        icon = Icons.Default.Calculate,
        badgeColorHex = 0xFF2196F3
    ),
    SHAKE(
        title = "Misi Goyang HP",
        description = "Goyang HP kuat-kuat sejumlah target sampai energimu terkumpul.",
        icon = Icons.Default.Vibration,
        badgeColorHex = 0xFFFF9800
    ),
    STEPS(
        title = "Misi Jalan Kaki",
        description = "Berdiri dan melangkah beberapa langkah menjauhi kasur.",
        icon = Icons.Default.DirectionsWalk,
        badgeColorHex = 0xFF4CAF50
    ),
    MEMORY(
        title = "Misi Pola Ingatan",
        description = "Ulangi urutan pola warna & angka untuk melatih konsentrasi.",
        icon = Icons.Default.Psychology,
        badgeColorHex = 0xFF9C27B0
    ),
    TYPING(
        title = "Misi Ketik Kata Motivasi",
        description = "Ketik kalimat afirmasi pagi positif tanpa salah ketik.",
        icon = Icons.Default.FormatQuote,
        badgeColorHex = 0xFF00BCD4
    );

    companion object {
        fun fromString(type: String): MissionType {
            return try {
                valueOf(type)
            } catch (e: Exception) {
                PHOTO
            }
        }
    }
}
