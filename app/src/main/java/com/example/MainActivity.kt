package com.example

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.AlarmEntity
import com.example.service.AlarmSoundService
import com.example.ui.ActiveAlarmScreen
import com.example.ui.AlarmEditDialog
import com.example.ui.AlarmViewModel
import com.example.ui.HomeScreen
import com.example.ui.MissionExecutionScreen
import com.example.ui.PhotoSpotsScreen
import com.example.ui.theme.MyApplicationTheme

import android.view.KeyEvent
import android.widget.Toast
import android.media.AudioManager

class MainActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val ringing = viewModel.ringingState.value.isRinging || viewModel.isMissionScreenOpen.value
        if (ringing) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (audioManager != null) {
                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                    val minAllowed = (maxVol * 0.40f).toInt().coerceAtLeast(1)
                    if (currentVol <= minAllowed) {
                        Toast.makeText(
                            this,
                            "🔒 Volume alarm terkunci! Tidak bisa diturunkan di bawah 40% saat alarm berbunyi.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return true // Intercept and block lowering
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupScreenWakeAndLockFlags()
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmAppContent(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val isRinging = intent.getBooleanExtra(AlarmSoundService.EXTRA_IS_RINGING, false)
        val alarmId = intent.getLongExtra(AlarmSoundService.EXTRA_ALARM_ID, -1L)
        if (isRinging && alarmId != -1L) {
            val missionType = intent.getStringExtra(AlarmSoundService.EXTRA_ALARM_MISSION_TYPE) ?: "PHOTO"
            val targetPlace = intent.getStringExtra(AlarmSoundService.EXTRA_ALARM_TARGET_PLACE) ?: "TOILET"
            val label = intent.getStringExtra(AlarmSoundService.EXTRA_ALARM_LABEL) ?: "Bangun Pagi"

            val currentAlarm = AlarmEntity(
                id = alarmId,
                hour = 6,
                minute = 0,
                isEnabled = true,
                label = label,
                missionType = missionType,
                photoTargetPlace = targetPlace,
                photoTargetLabel = if (targetPlace == "TOILET") "Toilet / Kamar Mandi" else targetPlace
            )
            viewModel.startMissionForRingingAlarm(currentAlarm)
        }
    }

    private fun setupScreenWakeAndLockFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}

@Composable
fun AlarmAppContent(viewModel: AlarmViewModel) {
    val alarms by viewModel.allAlarms.collectAsState()
    val photoSpots by viewModel.allPhotoSpots.collectAsState()
    val ringingState by viewModel.ringingState.collectAsState()
    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsState()
    val selectedAlarmForEdit by viewModel.selectedAlarmForEdit.collectAsState()
    val isMissionScreenOpen by viewModel.isMissionScreenOpen.collectAsState()
    val activeMissionAlarm by viewModel.activeMissionAlarm.collectAsState()
    val isPhotoSpotsOpen by viewModel.isPhotoSpotsManagerOpen.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    // Determine current screen
    when {
        isMissionScreenOpen && activeMissionAlarm != null -> {
            MissionExecutionScreen(
                alarm = activeMissionAlarm!!,
                photoSpots = photoSpots,
                onFinishMission = { viewModel.onMissionCompleted() },
                onSnooze = { min -> viewModel.snoozeAlarm(min) }
            )
        }
        ringingState.isRinging -> {
            val ringingAlarm = alarms.find { it.id == ringingState.alarmId } ?: AlarmEntity(
                id = ringingState.alarmId,
                hour = 6,
                minute = 0,
                label = ringingState.label,
                missionType = ringingState.missionType,
                photoTargetPlace = ringingState.targetPlace,
                photoTargetLabel = if (ringingState.targetPlace == "TOILET") "Toilet / Kamar Mandi" else ringingState.targetPlace
            )
            ActiveAlarmScreen(
                alarm = ringingAlarm,
                onStartMission = {
                    viewModel.startMissionForRingingAlarm(ringingAlarm)
                },
                onSnooze = { min -> viewModel.snoozeAlarm(min) }
            )
        }
        isPhotoSpotsOpen -> {
            PhotoSpotsScreen(
                photoSpots = photoSpots,
                onSaveSpot = { viewModel.savePhotoSpot(it) },
                onDeleteSpot = { viewModel.deletePhotoSpot(it) },
                onBack = { viewModel.closePhotoSpotsManager() }
            )
        }
        else -> {
            HomeScreen(
                alarms = alarms,
                photoSpots = photoSpots,
                onAddAlarmClick = { viewModel.openCreateAlarmDialog() },
                onEditAlarmClick = { viewModel.openEditAlarmDialog(it) },
                onToggleAlarm = { alarm, enabled -> viewModel.toggleAlarm(alarm, enabled) },
                onDeleteAlarm = { viewModel.deleteAlarm(it) },
                onTestAlarmClick = { viewModel.testAlarmWithMission(it) },
                onOpenPhotoSpots = { viewModel.openPhotoSpotsManager() },
                snackbarMessage = snackbarMessage,
                onClearSnackbar = { viewModel.clearSnackbarMessage() }
            )
        }
    }

    // Modal Sheet for Create / Edit Alarm
    if (isEditDialogOpen) {
        AlarmEditDialog(
            alarmToEdit = selectedAlarmForEdit,
            photoSpots = photoSpots,
            onOpenPhotoSpots = {
                viewModel.closeEditDialog()
                viewModel.openPhotoSpotsManager()
            },
            onSave = { viewModel.saveAlarm(it) },
            onDismiss = { viewModel.closeEditDialog() }
        )
    }
}
