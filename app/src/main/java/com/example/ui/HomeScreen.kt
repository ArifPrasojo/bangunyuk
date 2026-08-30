package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlarmEntity
import com.example.data.local.PhotoSpotEntity
import com.example.mission.MissionType
import com.example.service.AlarmScheduler
import com.example.ui.components.GundamVFinEmblem
import com.example.ui.components.MechaHudTag
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamBorderStrong
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamRed
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite
import com.example.ui.theme.GundamYellow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    alarms: List<AlarmEntity>,
    photoSpots: List<PhotoSpotEntity> = emptyList(),
    onAddAlarmClick: () -> Unit,
    onEditAlarmClick: (AlarmEntity) -> Unit,
    onToggleAlarm: (AlarmEntity, Boolean) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onTestAlarmClick: (AlarmEntity) -> Unit,
    onOpenPhotoSpots: () -> Unit,
    snackbarMessage: String? = null,
    onClearSnackbar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val registeredSpotsCount = remember(photoSpots) {
        photoSpots.count { !it.imageUri.isNullOrBlank() }
    }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onClearSnackbar()
        }
    }

    var snoozedAlarmAlert by remember { mutableStateOf<AlarmEntity?>(null) }

    // Find next upcoming enabled alarm
    val nextAlarm = remember(alarms) {
        alarms.filter { it.isEnabled }
            .minByOrNull {
                AlarmScheduler.getNextAlarmTriggerTime(it)
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BangunYuk",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GundamBlue,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Alarm Anti-Kesiangan",
                            style = MaterialTheme.typography.labelSmall,
                            color = GundamTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (registeredSpotsCount > 0) GundamBlueSubtle else GundamCardBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (registeredSpotsCount > 0) GundamBlue else GundamBorder
                        ),
                        modifier = Modifier.clickable { onOpenPhotoSpots() }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("open_photo_spots_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Spot Foto",
                                tint = if (registeredSpotsCount > 0) GundamBlue else GundamTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (photoSpots.isNotEmpty()) "Spot ($registeredSpotsCount/${photoSpots.size})" else "Spot Foto",
                                color = if (registeredSpotsCount > 0) GundamBlue else GundamTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GundamCanvasBg
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAlarmClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah") },
                text = {
                    Text(
                        text = "Tambah Alarm",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                containerColor = GundamBlue,
                contentColor = GundamWhite,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_alarm_fab")
            )
        },
        containerColor = GundamCanvasBg,
        modifier = modifier.statusBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // Next Alarm Hero Card (Clean white/light aesthetic)
                NextAlarmHeroCard(
                    nextAlarm = nextAlarm
                )
            }

            // Quick Photo Spot Notice (Only if not registered yet)
            if (registeredSpotsCount == 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPhotoSpots() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GundamBlueSubtle
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GundamBlue,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = GundamWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daftarkan Spot Foto Rumah",
                                    fontWeight = FontWeight.Bold,
                                    color = GundamBlue,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Ambil foto toilet/wastafel agar misi foto bangun tidur aktif.",
                                    fontSize = 12.sp,
                                    color = GundamTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 16.dp)
                                .background(GundamBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DAFTAR ALARM (${alarms.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GundamTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            if (alarms.isEmpty()) {
                item {
                    EmptyAlarmState(onAddAlarmClick = onAddAlarmClick)
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmItemCard(
                        alarm = alarm,
                        onEditClick = { onEditAlarmClick(alarm) },
                        onToggle = { isEnabled -> onToggleAlarm(alarm, isEnabled) },
                        onDeleteClick = {
                            if (alarm.isCurrentlySnoozed()) {
                                snoozedAlarmAlert = alarm
                            } else {
                                onDeleteAlarm(alarm)
                            }
                        },
                        onTestClick = { onTestAlarmClick(alarm) },
                        onShowSnoozedAlert = { snoozedAlarmAlert = alarm }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }

    // Alert dialog when user tries to delete/interact with snoozed alarm
    if (snoozedAlarmAlert != null) {
        val alertAlarm = snoozedAlarmAlert!!
        val remaining = AlarmScheduler.getSnoozeRemainingString(alertAlarm.snoozedUntil)
        AlertDialog(
            onDismissRequest = { snoozedAlarmAlert = null },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = GundamRed.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamRed),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Terkunci",
                            tint = GundamRed,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Alarm Sedang Ditunda (Snooze)",
                    fontWeight = FontWeight.Bold,
                    color = GundamTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Alarm \"${alertAlarm.label}\" sedang dalam masa tunda ($remaining).",
                        color = GundamTextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "🔒 Alarm tidak dapat dihapus/dimatikan hingga kamu menyelesaikan misi verifikasi bangun tidur!",
                        color = GundamRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        val toTest = alertAlarm
                        snoozedAlarmAlert = null
                        onTestAlarmClick(toTest)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GundamBlue,
                        contentColor = GundamWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Selesaikan Misi Sekarang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { snoozedAlarmAlert = null }) {
                    Text("Tutup", color = GundamTextSecondary)
                }
            },
            containerColor = GundamCanvasBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun NextAlarmHeroCard(
    nextAlarm: AlarmEntity?
) {
    val isSnoozed = nextAlarm?.isCurrentlySnoozed() == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSnoozed) GundamRed.copy(alpha = 0.5f) else GundamBorderStrong,
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSnoozed) GundamRed.copy(alpha = 0.05f) else GundamCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSnoozed) Icons.Default.Snooze else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isSnoozed) GundamRed else GundamBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSnoozed) "STATUS: SNOOZE AKTIF" else "ALARM BERIKUTNYA",
                        color = if (isSnoozed) GundamRed else GundamBlue,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                if (nextAlarm != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSnoozed) GundamRed.copy(alpha = 0.1f) else GundamBlueSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSnoozed) GundamRed.copy(alpha = 0.3f) else GundamBlue.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${nextAlarm.getMissionTypeList().size} MISI",
                            color = if (isSnoozed) GundamRed else GundamBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (nextAlarm != null) {
                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", nextAlarm.hour, nextAlarm.minute)
                val remainingStr = if (isSnoozed) {
                    AlarmScheduler.getSnoozeRemainingString(nextAlarm.snoozedUntil)
                } else {
                    AlarmScheduler.getRemainingTimeString(nextAlarm.hour, nextAlarm.minute, nextAlarm.daysOfWeek)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = timeStr,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSnoozed) GundamRed else GundamBlue,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = nextAlarm.label,
                            color = GundamTextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isSnoozed) "🚨 Berbunyi lagi dalam $remainingStr" else "⏳ Berbunyi dalam: $remainingStr",
                    color = if (isSnoozed) GundamRed else GundamTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Belum Ada Alarm Aktif",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GundamTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Buat alarm baru dengan misi verifikasi agar bangun tepat waktu.",
                    color = GundamTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AlarmItemCard(
    alarm: AlarmEntity,
    onEditClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onTestClick: () -> Unit,
    onShowSnoozedAlert: () -> Unit
) {
    val isSnoozed = alarm.isCurrentlySnoozed()
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)

    val dayNamesMap = mapOf(
        1 to "Sen", 2 to "Sel", 3 to "Rab", 4 to "Kam", 5 to "Jum", 6 to "Sab", 7 to "Min"
    )
    val activeDaysText = if (alarm.daysOfWeek.isBlank()) {
        "Sekali Saja"
    } else {
        val daysList = alarm.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (daysList.size == 7) {
            "Setiap Hari"
        } else if (daysList == listOf(1, 2, 3, 4, 5)) {
            "Hari Kerja (Sen-Jum)"
        } else {
            daysList.joinToString(" ") { dayNamesMap[it] ?: "" }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSnoozed) GundamRed.copy(alpha = 0.6f)
                else if (alarm.isEnabled) GundamBorderStrong
                else GundamBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable {
                if (isSnoozed) {
                    onShowSnoozedAlert()
                } else {
                    onEditClick()
                }
            }
            .testTag("alarm_card_${alarm.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSnoozed) GundamRed.copy(alpha = 0.04f)
            else if (alarm.isEnabled) GundamCardBg
            else GundamCanvasBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main row: Time, Label, and Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 26.dp)
                                .background(
                                    if (isSnoozed) GundamRed
                                    else if (alarm.isEnabled) GundamBlue
                                    else GundamBorderStrong,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeFormatted,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSnoozed) GundamRed
                            else if (alarm.isEnabled) GundamTextPrimary
                            else GundamTextSecondary.copy(alpha = 0.6f),
                            letterSpacing = (-1).sp
                        )
                    }
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) GundamTextPrimary else GundamTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { isChecked ->
                        if (isSnoozed) {
                            onShowSnoozedAlert()
                        } else {
                            onToggle(isChecked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GundamWhite,
                        checkedTrackColor = if (isSnoozed) GundamRed else GundamBlue,
                        uncheckedThumbColor = GundamTextSecondary,
                        uncheckedTrackColor = GundamBorder
                    ),
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Repeat Days & Mission Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeDaysText,
                    style = MaterialTheme.typography.bodySmall,
                    color = GundamTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )

                // Mission Badges
                val missionList = alarm.getMissionTypeList()
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GundamBlueSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        missionList.forEach { m ->
                            Icon(
                                imageVector = m.icon,
                                contentDescription = m.title,
                                tint = GundamBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (missionList.size > 1) "${missionList.size} Misi" else (if (missionList.firstOrNull() == MissionType.PHOTO) "Foto ${alarm.photoTargetLabel}" else missionList.firstOrNull()?.title ?: "Math"),
                            color = GundamBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick actions: Test Alarm Mission & Edit/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onTestClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSnoozed) GundamRed.copy(alpha = 0.1f) else GundamBlueSubtle,
                        contentColor = if (isSnoozed) GundamRed else GundamBlue
                    ),
                    modifier = Modifier.testTag("test_alarm_button_${alarm.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSnoozed) "Selesaikan Misi" else "Uji Coba Misi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            if (isSnoozed) {
                                onShowSnoozedAlert()
                            } else {
                                onEditClick()
                            }
                        },
                        modifier = Modifier.testTag("edit_alarm_btn_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = GundamTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("delete_alarm_btn_${alarm.id}")
                    ) {
                        if (isSnoozed) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Terkunci",
                                tint = GundamRed,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = GundamRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlarmState(onAddAlarmClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = GundamCardBg),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder)
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
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GundamBlue),
                modifier = Modifier.size(68.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Belum Ada Alarm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GundamTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Buat alarm bangun tidur dengan misi verifikasi (Foto Toilet, Matematika, Sensor Goyang) agar kamu tidak ketiduran lagi!",
                style = MaterialTheme.typography.bodyMedium,
                color = GundamTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onAddAlarmClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GundamBlue,
                    contentColor = GundamWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Alarm Baru", fontWeight = FontWeight.Bold)
            }
        }
    }
}
