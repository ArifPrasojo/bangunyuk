package com.example.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamGreen
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite
import kotlin.random.Random

@Composable
fun TypingMissionView(
    onMissionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quotes = remember {
        listOf(
            "Hari ini saya bangun pagi dengan energi positif untuk mencapai cita-cita.",
            "Bangun lebih awal memberi saya waktu dan fokus yang lebih baik hari ini.",
            "Saya siap melangkah keluar dari tempat tidur dan menjalani hari dengan hebat."
        )
    }

    val selectedQuote = remember { quotes[Random.nextInt(quotes.size)] }
    var typedText by remember { mutableStateOf("") }

    // Normalize text comparison (ignore excess whitespace, case insensitive)
    fun normalize(s: String) = s.trim().replace("\\s+".toRegex(), " ").lowercase()

    val isMatched = normalize(typedText) == normalize(selectedQuote)
    val progress = (typedText.length.toFloat() / selectedQuote.length.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GundamCanvasBg)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = GundamBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Misi Ketik Motivasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GundamBlue
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ketik ulang kalimat target di bawah ini untuk mengaktifkan fokus pikiranmu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GundamTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quote display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamBlueSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Kalimat Target:",
                    style = MaterialTheme.typography.labelSmall,
                    color = GundamBlue,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"$selectedQuote\"",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp,
                    color = GundamTextPrimary
                )
            }
        }

        // Progress bar & Input field
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kemajuan Ketikan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GundamTextSecondary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMatched) GundamGreen else GundamBlue
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isMatched) GundamGreen else GundamBlue,
                    trackColor = GundamBorder
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = typedText,
                    onValueChange = {
                        typedText = it
                        if (normalize(it) == normalize(selectedQuote)) {
                            onMissionSuccess()
                        }
                    },
                    placeholder = {
                        Text(
                            "Ketik kalimat target di sini...",
                            color = GundamTextSecondary.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GundamBlue,
                        unfocusedBorderColor = GundamBorder,
                        focusedTextColor = GundamTextPrimary,
                        unfocusedTextColor = GundamTextPrimary,
                        focusedContainerColor = GundamCanvasBg,
                        unfocusedContainerColor = GundamCanvasBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("typing_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 4
                )
            }
        }

        // Submit Button
        Button(
            onClick = {
                if (isMatched) {
                    onMissionSuccess()
                }
            },
            enabled = isMatched,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMatched) GundamGreen else GundamBlue,
                contentColor = GundamWhite,
                disabledContainerColor = GundamBorder,
                disabledContentColor = GundamTextSecondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_typing_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isMatched) "Kalimat Cocok! Selesaikan Misi" else "Ketik Kalimat Hingga Lengkap",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
