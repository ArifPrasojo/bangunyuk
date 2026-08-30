package com.example.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mission.StepDetector
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite

@Composable
fun StepMissionView(
    targetSteps: Int,
    onMissionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSteps by remember { mutableIntStateOf(0) }

    fun registerStep() {
        currentSteps++
        if (currentSteps >= targetSteps) {
            onMissionSuccess()
        }
    }

    DisposableEffect(Unit) {
        val detector = StepDetector(context) {
            registerStep()
        }
        detector.start()
        onDispose {
            detector.stop()
        }
    }

    val progress = (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GundamCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = GundamBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ayo Berdiri & Berjalan!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GundamBlue
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Berjalanlah sejauh $targetSteps langkah keluar dari tempat tidur untuk menyegarkan badan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GundamTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Circular Gauge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = GundamBlueSubtle,
                        strokeWidth = 12.dp
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = GundamBlue,
                        strokeWidth = 12.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GundamBlueSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GundamBlue),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = "Jalan",
                                    tint = GundamBlue,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$currentSteps / $targetSteps",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = GundamTextPrimary
                        )
                        Text(
                            text = "Langkah Selesai",
                            style = MaterialTheme.typography.labelSmall,
                            color = GundamTextSecondary
                        )
                    }
                }
            }
        }

        // Real Sensor Active Status Indicator
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = GundamBlueSubtle,
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = GundamBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sensor Langkah Aktif — Pegang HP & Berjalan Nyata",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GundamBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
