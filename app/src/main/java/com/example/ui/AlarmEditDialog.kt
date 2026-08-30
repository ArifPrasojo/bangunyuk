package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlarmEntity
import com.example.data.local.PhotoSpotEntity
import com.example.mission.MissionType
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamBorderStrong
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamGreen
import com.example.ui.theme.GundamRed
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditDialog(
    alarmToEdit: AlarmEntity?,
    photoSpots: List<PhotoSpotEntity> = emptyList(),
    onOpenPhotoSpots: () -> Unit = {},
    onSave: (AlarmEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val registeredSpots = remember(photoSpots) {
        photoSpots.filter { !it.imageUri.isNullOrBlank() }
    }
    val isPhotoAvailable = registeredSpots.isNotEmpty()

    val initialHour = alarmToEdit?.hour ?: 6
    val initialMinute = alarmToEdit?.minute ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    var label by remember { mutableStateOf(alarmToEdit?.label ?: "Bangun Pagi") }
    val selectedMissionTypes = remember {
        mutableStateListOf<String>().apply {
            val raw = alarmToEdit?.missionType
            val initialList = if (!raw.isNullOrBlank()) {
                raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            } else {
                listOf(if (isPhotoAvailable) "PHOTO" else "MATH")
            }
            val sanitized = if (initialList.contains("PHOTO") && !isPhotoAvailable && alarmToEdit == null) {
                initialList.filter { it != "PHOTO" }.ifEmpty { listOf("MATH") }
            } else {
                initialList.ifEmpty { listOf("MATH") }
            }
            addAll(sanitized)
        }
    }
    val selectedPhotoSpots = remember {
        mutableStateListOf<String>().apply {
            val existing = alarmToEdit?.getPhotoTargetPlaceList() ?: emptyList()
            if (existing.isNotEmpty()) {
                addAll(existing)
            } else {
                val defaultKey = registeredSpots.firstOrNull()?.spotKey ?: "TOILET"
                add(defaultKey)
            }
        }
    }
    var showPhotoUnavailableNotice by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf(alarmToEdit?.missionDifficulty ?: "MEDIUM") }
    var shakeTarget by remember { mutableIntStateOf(alarmToEdit?.shakeTarget ?: 30) }
    var stepsTarget by remember { mutableIntStateOf(alarmToEdit?.stepsTarget ?: 25) }
    var mathCount by remember { mutableIntStateOf(alarmToEdit?.mathProblemCount ?: 3) }
    var snoozeMinutes by remember { mutableIntStateOf(alarmToEdit?.snoozeMinutes ?: 5) }
    var volume by remember { mutableFloatStateOf((alarmToEdit?.volume ?: 100).coerceAtLeast(40).toFloat()) }
    var previousVolume by remember { mutableFloatStateOf(volume) }
    var showVolumeAlert by remember { mutableStateOf(false) }
    var vibrate by remember { mutableStateOf(alarmToEdit?.vibrate ?: true) }

    // Day of week repeat selections (1..7 => Mon..Sun)
    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val selectedDays = remember {
        mutableStateListOf<Int>().apply {
            val raw = alarmToEdit?.daysOfWeek ?: "1,2,3,4,5"
            if (raw.isNotBlank()) {
                addAll(raw.split(",").mapNotNull { it.trim().toIntOrNull() })
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GundamCanvasBg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alarmToEdit == null) "Tambah Alarm Baru" else "Edit Alarm",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GundamBlue
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_edit_dialog")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = GundamTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Picker
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = GundamCardBg,
                        selectorColor = GundamBlue,
                        periodSelectorBorderColor = GundamBorder,
                        timeSelectorSelectedContainerColor = GundamBlueSubtle,
                        timeSelectorUnselectedContainerColor = GundamCardBg,
                        timeSelectorSelectedContentColor = GundamBlue,
                        timeSelectorUnselectedContentColor = GundamTextSecondary
                    ),
                    modifier = Modifier.testTag("time_picker")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Days
            Text(
                text = "Hari Pengulangan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GundamTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayNames.forEachIndexed { index, dayName ->
                    val dayId = index + 1
                    val isSelected = selectedDays.contains(dayId)
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) GundamBlue else GundamCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GundamBlue else GundamBorder),
                        modifier = Modifier
                            .size(42.dp)
                            .clickable {
                                if (isSelected) selectedDays.remove(dayId) else selectedDays.add(dayId)
                            }
                            .testTag("day_chip_$dayId")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = dayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GundamWhite else GundamTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = GundamBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Nama / Label Alarm") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GundamBlue,
                    unfocusedBorderColor = GundamBorder,
                    focusedLabelColor = GundamBlue,
                    unfocusedLabelColor = GundamTextSecondary,
                    focusedTextColor = GundamTextPrimary,
                    unfocusedTextColor = GundamTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mission Selection Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilih Misi Bangun (Bisa >1)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GundamBlue
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = GundamBlueSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${selectedMissionTypes.size} Misi Dipilih",
                        color = GundamBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = "Pilih 1 atau lebih misi. Kamu harus menyelesaikan seluruh misi secara berurutan agar alarm berhenti.",
                style = MaterialTheme.typography.bodySmall,
                color = GundamTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mission Type Chips (Multi-selectable)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MissionType.values().forEach { mType ->
                    val isPhoto = mType == MissionType.PHOTO
                    val isLocked = isPhoto && !isPhotoAvailable
                    val isSelected = selectedMissionTypes.contains(mType.name) && !isLocked
                    val orderIndex = if (isSelected) selectedMissionTypes.indexOf(mType.name) + 1 else null

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isLocked) {
                                showPhotoUnavailableNotice = true
                            } else {
                                if (isSelected) {
                                    if (selectedMissionTypes.size > 1) {
                                        selectedMissionTypes.remove(mType.name)
                                    }
                                } else {
                                    selectedMissionTypes.add(mType.name)
                                    showPhotoUnavailableNotice = false
                                }
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = GundamBlue,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$orderIndex",
                                                color = GundamWhite,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    if (isLocked) "Foto (Belum Ada Spot)" else mType.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else mType.icon,
                                contentDescription = mType.title,
                                tint = if (isLocked) GundamRed else if (isSelected) GundamBlue else GundamTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GundamBlueSubtle,
                            selectedLabelColor = GundamBlue,
                            selectedLeadingIconColor = GundamBlue,
                            containerColor = if (isLocked) GundamRed.copy(alpha = 0.05f) else GundamCardBg,
                            labelColor = if (isLocked) GundamRed else GundamTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isLocked) GundamRed.copy(alpha = 0.3f)
                            else if (isSelected) GundamBlue
                            else GundamBorder
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("mission_chip_${mType.name}")
                    )
                }
            }

            if (showPhotoUnavailableNotice && !isPhotoAvailable) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GundamRed.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = GundamRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Spot Foto Belum Terdaftar!",
                                fontWeight = FontWeight.Bold,
                                color = GundamRed,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kamu harus mendaftarkan minimal 1 foto spot (seperti Toilet atau Wastafel) agar misi foto dapat diaktifkan.",
                            fontSize = 12.sp,
                            color = GundamTextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = onOpenPhotoSpots,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = GundamBlue,
                                contentColor = GundamWhite
                            ),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Daftarkan Spot Foto Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Sequence Visualizer if multiple missions selected
            if (selectedMissionTypes.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GundamBlueSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎯 Urutan Eksekusi Misi:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GundamBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedMissionTypes.forEachIndexed { index, mName ->
                                val mission = MissionType.fromString(mName)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GundamCardBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GundamBlue
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = mission.icon,
                                                contentDescription = null,
                                                tint = GundamBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = mission.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = GundamTextPrimary
                                            )
                                        }
                                    }
                                    if (index < selectedMissionTypes.size - 1) {
                                        Text(
                                            text = "➔",
                                            color = GundamBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Render Options for each selected mission in order
            selectedMissionTypes.forEachIndexed { index, mTypeName ->
                val mType = MissionType.fromString(mTypeName)
                val stageNum = if (selectedMissionTypes.size > 1) " (Misi #${index + 1})" else ""

                when (mType) {
                    MissionType.PHOTO -> {
                        if (!isPhotoAvailable) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = GundamRed.copy(alpha = 0.05f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GundamRed.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Terkunci",
                                        tint = GundamRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Misi Foto$stageNum Belum Siap",
                                        fontWeight = FontWeight.Bold,
                                        color = GundamRed,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Silakan daftarkan foto Toilet / Wastafel rumahmu terlebih dahulu.",
                                        color = GundamTextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = GundamBlue, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Pengaturan Misi Foto$stageNum",
                                                fontWeight = FontWeight.Bold,
                                                color = GundamTextPrimary,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(100.dp),
                                            color = GundamBlueSubtle,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "${selectedPhotoSpots.size} Spot Dipilih",
                                                color = GundamBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Pilih 1 atau lebih spot foto bangun tidur (wajib difoto satu per satu saat alarm bunyi):",
                                        fontSize = 12.sp,
                                        color = GundamTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    photoSpots.forEach { spot ->
                                        val isRegistered = !spot.imageUri.isNullOrBlank()
                                        val isSelected = selectedPhotoSpots.contains(spot.spotKey)
                                        val orderIndex = if (isSelected) selectedPhotoSpots.indexOf(spot.spotKey) + 1 else null

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    if (isRegistered) {
                                                        if (isSelected) {
                                                            if (selectedPhotoSpots.size > 1) {
                                                                selectedPhotoSpots.remove(spot.spotKey)
                                                            }
                                                        } else {
                                                            selectedPhotoSpots.add(spot.spotKey)
                                                        }
                                                    } else {
                                                        onOpenPhotoSpots()
                                                    }
                                                }
                                                .background(if (isSelected && isRegistered) GundamBlueSubtle else Color.Transparent)
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelected && isRegistered) GundamBlue else Color.Transparent,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        if (isSelected && isRegistered) GundamBlue
                                                        else if (isRegistered) GundamBorderStrong
                                                        else GundamBorder
                                                    ),
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    if (isSelected && isRegistered) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = "$orderIndex",
                                                                color = GundamWhite,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = spot.spotName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isRegistered) {
                                                            if (isSelected) GundamBlue else GundamTextPrimary
                                                        } else GundamTextSecondary.copy(alpha = 0.6f),
                                                        fontWeight = if (isSelected && isRegistered) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Text(
                                                        text = if (isRegistered) "✅ Foto Siap" else "⚠️ Belum Ada Foto (Ketuk untuk daftar)",
                                                        fontSize = 11.sp,
                                                        color = if (isRegistered) GundamGreen else GundamTextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    MissionType.MATH -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = mType.icon, contentDescription = null, tint = GundamBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pengaturan Matematika$stageNum",
                                        fontWeight = FontWeight.Bold,
                                        color = GundamTextPrimary,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("EASY" to "Mudah", "MEDIUM" to "Sedang", "HARD" to "Sulit").forEach { (diffKey, diffLabel) ->
                                        FilterChip(
                                            selected = difficulty == diffKey,
                                            onClick = { difficulty = diffKey },
                                            label = { Text(diffLabel) },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GundamBlueSubtle,
                                                selectedLabelColor = GundamBlue
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Jumlah Soal: $mathCount soal",
                                    color = GundamTextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Slider(
                                    value = mathCount.toFloat(),
                                    onValueChange = { mathCount = it.toInt() },
                                    valueRange = 1f..7f,
                                    steps = 5,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GundamBlue,
                                        activeTrackColor = GundamBlue,
                                        inactiveTrackColor = GundamBorder
                                    )
                                )
                            }
                        }
                    }
                    MissionType.SHAKE -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = mType.icon, contentDescription = null, tint = GundamBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pengaturan Goyang HP$stageNum",
                                        fontWeight = FontWeight.Bold,
                                        color = GundamTextPrimary,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Target Goyangan HP: $shakeTarget kali",
                                    fontWeight = FontWeight.Medium,
                                    color = GundamTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    value = shakeTarget.toFloat(),
                                    onValueChange = { shakeTarget = it.toInt() },
                                    valueRange = 10f..60f,
                                    steps = 4,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GundamBlue,
                                        activeTrackColor = GundamBlue,
                                        inactiveTrackColor = GundamBorder
                                    )
                                )
                            }
                        }
                    }
                    MissionType.STEPS -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = mType.icon, contentDescription = null, tint = GundamBlue, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pengaturan Jalan Kaki$stageNum",
                                        fontWeight = FontWeight.Bold,
                                        color = GundamTextPrimary,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Target Langkah Kaki: $stepsTarget langkah",
                                    fontWeight = FontWeight.Medium,
                                    color = GundamTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    value = stepsTarget.toFloat(),
                                    onValueChange = { stepsTarget = it.toInt() },
                                    valueRange = 10f..50f,
                                    steps = 3,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GundamBlue,
                                        activeTrackColor = GundamBlue,
                                        inactiveTrackColor = GundamBorder
                                    )
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GundamBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Volume & Vibration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Getar (Vibrate)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = GundamTextPrimary
                )
                Switch(
                    checked = vibrate,
                    onCheckedChange = { vibrate = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GundamWhite,
                        checkedTrackColor = GundamBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Volume Alarm: ${volume.toInt()}%",
                    color = GundamTextSecondary,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Slider(
                value = volume,
                onValueChange = { newVal ->
                    val clamped = newVal.coerceAtLeast(40f)
                    if (newVal < previousVolume && newVal <= 50f) {
                        showVolumeAlert = true
                    }
                    volume = clamped
                    previousVolume = clamped
                },
                valueRange = 40f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = GundamBlue,
                    activeTrackColor = GundamBlue,
                    inactiveTrackColor = GundamBorder
                )
            )

            if (showVolumeAlert) {
                AlertDialog(
                    onDismissRequest = { showVolumeAlert = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Peringatan Volume",
                            tint = GundamRed,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Peringatan Menurunkan Volume",
                            fontWeight = FontWeight.Bold,
                            color = GundamTextPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "Menurunkan volume alarm berisiko membuat kamu tidak terbangun. Batas minimum volume adalah 40%.",
                            color = GundamTextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showVolumeAlert = false },
                            colors = ButtonDefaults.buttonColors(containerColor = GundamBlue)
                        ) {
                            Text("Saya Mengerti", color = GundamWhite)
                        }
                    },
                    containerColor = GundamCardBg,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Snooze
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fitur Tunda (Snooze)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = GundamTextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0 to "Mati", 5 to "5m", 10 to "10m").forEach { (min, text) ->
                        FilterChip(
                            selected = snoozeMinutes == min,
                            onClick = { snoozeMinutes = min },
                            label = { Text(text) },
                            shape = RoundedCornerShape(100.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GundamBlueSubtle,
                                selectedLabelColor = GundamBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    val daysStr = selectedDays.sorted().joinToString(",")
                    val chosenSpots = if (selectedPhotoSpots.isNotEmpty()) selectedPhotoSpots.toList()
                        else listOf(registeredSpots.firstOrNull()?.spotKey ?: "TOILET")
                    val photoTargetPlaceStr = chosenSpots.joinToString(",")
                    val photoTargetLabelStr = chosenSpots.mapNotNull { k ->
                        photoSpots.find { it.spotKey.equals(k, ignoreCase = true) }?.spotName
                    }.ifEmpty { listOf("Spot Foto") }.joinToString(", ")
                    val matchedSpotUri = registeredSpots.find { it.spotKey.equals(chosenSpots.firstOrNull(), ignoreCase = true) }?.imageUri
                        ?: alarmToEdit?.photoReferenceUri

                    val newAlarm = AlarmEntity(
                        id = alarmToEdit?.id ?: 0L,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        isEnabled = true,
                        label = label.ifBlank { "Bangun Pagi" },
                        daysOfWeek = daysStr,
                        missionType = selectedMissionTypes.joinToString(","),
                        missionDifficulty = difficulty,
                        photoTargetPlace = photoTargetPlaceStr,
                        photoReferenceUri = matchedSpotUri,
                        photoTargetLabel = photoTargetLabelStr,
                        shakeTarget = shakeTarget,
                        stepsTarget = stepsTarget,
                        mathProblemCount = mathCount,
                        volume = volume.toInt(),
                        vibrate = vibrate,
                        snoozeMinutes = snoozeMinutes
                    )
                    onSave(newAlarm)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GundamBlue,
                    contentColor = GundamWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_alarm_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Simpan Alarm", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
