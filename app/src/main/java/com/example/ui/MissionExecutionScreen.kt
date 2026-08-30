package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlarmEntity
import com.example.data.local.PhotoSpotEntity
import com.example.mission.MissionType
import com.example.ui.components.GundamVFinEmblem
import com.example.ui.mission.MathMissionView
import com.example.ui.mission.MemoryMissionView
import com.example.ui.mission.PhotoMissionView
import com.example.ui.mission.ShakeMissionView
import com.example.ui.mission.StepMissionView
import com.example.ui.mission.TypingMissionView
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamGreen
import com.example.ui.theme.GundamRed
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite
import com.example.ui.theme.GundamYellow

@Composable
fun MissionExecutionScreen(
    alarm: AlarmEntity,
    photoSpots: List<PhotoSpotEntity> = emptyList(),
    onFinishMission: () -> Unit,
    onSnooze: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val missionList = remember(alarm) { alarm.getMissionTypeList().ifEmpty { listOf(MissionType.MATH) } }
    var currentStageIndex by remember { mutableIntStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }

    val currentMissionType = missionList.getOrElse(currentStageIndex) { missionList.first() }

    val photoTargetsList = remember(alarm, photoSpots) {
        val keys = alarm.getPhotoTargetPlaceList()
        keys.map { key ->
            val spot = photoSpots.find { it.spotKey.equals(key, ignoreCase = true) }
            val label = spot?.spotName ?: if (key == "TOILET") "Toilet / Kamar Mandi" else key
            val refUri = spot?.imageUri ?: if (key == alarm.photoTargetPlace) alarm.photoReferenceUri else null
            com.example.ui.mission.PhotoTargetInfo(
                spotKey = key,
                spotLabel = label,
                referenceUriString = refUri
            )
        }
    }

    fun handleStageSuccess() {
        if (currentStageIndex < missionList.size - 1) {
            currentStageIndex++
        } else {
            isCompleted = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GundamCanvasBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar (Clean White Card Top Bar)
            Surface(
                color = GundamCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GundamBlueSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = currentMissionType.icon,
                                    contentDescription = currentMissionType.title,
                                    tint = GundamBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = alarm.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GundamTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = GundamBlueSubtle
                                ) {
                                    Text(
                                        text = "Tahap ${currentStageIndex + 1}/${missionList.size}",
                                        color = GundamBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Misi: ${currentMissionType.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = GundamTextSecondary
                            )
                        }
                    }

                    if (alarm.snoozeMinutes > 0 && !isCompleted) {
                        OutlinedButton(
                            onClick = { onSnooze(alarm.snoozeMinutes) },
                            modifier = Modifier.testTag("snooze_mission_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GundamBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = "Tunda",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${alarm.snoozeMinutes}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Mission Body container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentMissionType) {
                    MissionType.PHOTO -> {
                        PhotoMissionView(
                            targets = photoTargetsList,
                            onMissionSuccess = { handleStageSuccess() }
                        )
                    }
                    MissionType.MATH -> {
                        MathMissionView(
                            problemCount = alarm.mathProblemCount,
                            difficulty = alarm.missionDifficulty,
                            onMissionSuccess = { handleStageSuccess() }
                        )
                    }
                    MissionType.SHAKE -> {
                        ShakeMissionView(
                            targetCount = alarm.shakeTarget,
                            onMissionSuccess = { handleStageSuccess() }
                        )
                    }
                    MissionType.STEPS -> {
                        StepMissionView(
                            targetSteps = alarm.stepsTarget,
                            onMissionSuccess = { handleStageSuccess() }
                        )
                    }
                    MissionType.MEMORY -> {
                        MemoryMissionView(
                            sequenceLength = 4,
                            onMissionSuccess = { handleStageSuccess() }
                        )
                    }
                    MissionType.TYPING -> {
                        TypingMissionView(
                            onMissionSuccess = { handleStageSuccess() }
                        )
                    }
                }
            }
        }

        // Mission Success Overlay Dialog
        AnimatedVisibility(
            visible = isCompleted,
            enter = fadeIn() + scaleIn(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GundamBorder, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GundamCardBg
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GundamBlueSubtle,
                            border = androidx.compose.foundation.BorderStroke(2.dp, GundamYellow),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                GundamVFinEmblem(
                                    modifier = Modifier.size(46.dp),
                                    finColor = GundamYellow,
                                    coreColor = GundamBlue,
                                    opticColor = GundamRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Misi Berhasil Diselesaikan!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GundamBlue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Semua tantangan (${missionList.size} tahap) berhasil diverifikasi. Selamat pagi dan selamat beraktivitas!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GundamTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onFinishMission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GundamBlue,
                                contentColor = GundamWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("dismiss_alarm_done_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Matikan Alarm",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
