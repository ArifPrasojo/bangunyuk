package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlarmEntity
import com.example.data.local.AppDatabase
import com.example.data.local.PhotoSpotEntity
import com.example.data.repository.AlarmRepository
import com.example.service.AlarmScheduler
import com.example.service.AlarmSoundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository
    private val scheduler: AlarmScheduler = AlarmScheduler(application)

    val allAlarms: StateFlow<List<AlarmEntity>>
    val allPhotoSpots: StateFlow<List<PhotoSpotEntity>>
    val ringingState: StateFlow<AlarmSoundService.RingingState> = AlarmSoundService.ringingAlarmState

    private val _selectedAlarmForEdit = MutableStateFlow<AlarmEntity?>(null)
    val selectedAlarmForEdit: StateFlow<AlarmEntity?> = _selectedAlarmForEdit.asStateFlow()

    private val _isEditDialogOpen = MutableStateFlow(false)
    val isEditDialogOpen: StateFlow<Boolean> = _isEditDialogOpen.asStateFlow()

    private val _activeMissionAlarm = MutableStateFlow<AlarmEntity?>(null)
    val activeMissionAlarm: StateFlow<AlarmEntity?> = _activeMissionAlarm.asStateFlow()

    private val _isMissionScreenOpen = MutableStateFlow(false)
    val isMissionScreenOpen: StateFlow<Boolean> = _isMissionScreenOpen.asStateFlow()

    private val _isPhotoSpotsManagerOpen = MutableStateFlow(false)
    val isPhotoSpotsManagerOpen: StateFlow<Boolean> = _isPhotoSpotsManagerOpen.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = AlarmRepository(db.alarmDao(), db.photoSpotDao())

        allAlarms = repository.allAlarms
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allPhotoSpots = repository.allPhotoSpots
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun showMessage(message: String) {
        _snackbarMessage.value = message
    }

    fun openCreateAlarmDialog() {
        _selectedAlarmForEdit.value = null
        _isEditDialogOpen.value = true
    }

    fun openEditAlarmDialog(alarm: AlarmEntity) {
        _selectedAlarmForEdit.value = alarm
        _isEditDialogOpen.value = true
    }

    fun closeEditDialog() {
        _isEditDialogOpen.value = false
        _selectedAlarmForEdit.value = null
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            if (alarm.id == 0L) {
                val newId = repository.insertAlarm(alarm)
                val savedAlarm = alarm.copy(id = newId)
                if (savedAlarm.isEnabled) {
                    scheduler.schedule(savedAlarm)
                }
            } else {
                repository.updateAlarm(alarm)
                if (alarm.isEnabled) {
                    if (alarm.isCurrentlySnoozed()) {
                        scheduler.scheduleSnooze(alarm, alarm.snoozedUntil)
                    } else {
                        scheduler.schedule(alarm)
                    }
                } else {
                    scheduler.cancel(alarm.id)
                }
            }
            closeEditDialog()
        }
    }

    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        if (!isEnabled && alarm.isCurrentlySnoozed()) {
            _snackbarMessage.value = "⚠️ Alarm sedang ditunda! Selesaikan misi untuk mematikan atau menghapus alarm."
            return
        }
        viewModelScope.launch {
            val updated = alarm.copy(
                isEnabled = isEnabled,
                snoozedUntil = if (!isEnabled) 0L else alarm.snoozedUntil
            )
            repository.updateAlarm(updated)
            if (isEnabled) {
                if (updated.isCurrentlySnoozed()) {
                    scheduler.scheduleSnooze(updated, updated.snoozedUntil)
                } else {
                    scheduler.schedule(updated)
                }
            } else {
                scheduler.cancel(updated.id)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        if (alarm.isCurrentlySnoozed()) {
            _snackbarMessage.value = "🔒 Alarm sedang ditunda dan TIDAK BISA dihapus! Selesaikan misi bangun terlebih dahulu."
            return
        }
        viewModelScope.launch {
            scheduler.cancel(alarm.id)
            repository.deleteAlarm(alarm)
            _snackbarMessage.value = "Alarm \"${alarm.label}\" berhasil dihapus."
        }
    }

    fun testAlarmWithMission(alarm: AlarmEntity) {
        _activeMissionAlarm.value = alarm
        _isMissionScreenOpen.value = true
        AlarmSoundService.triggerTestAlarm(
            getApplication(),
            label = alarm.label,
            missionType = alarm.missionType,
            targetPlace = alarm.photoTargetPlace
        )
    }

    fun startMissionForRingingAlarm(alarm: AlarmEntity) {
        _activeMissionAlarm.value = alarm
        _isMissionScreenOpen.value = true
    }

    fun onMissionCompleted() {
        val currentRingingId = ringingState.value.alarmId
        val targetAlarm = _activeMissionAlarm.value
            ?: allAlarms.value.find { it.id == currentRingingId }

        AlarmSoundService.stopRinging(getApplication())
        _isMissionScreenOpen.value = false
        _activeMissionAlarm.value = null

        if (targetAlarm != null && targetAlarm.id != 9999L) {
            viewModelScope.launch {
                val updated = targetAlarm.copy(snoozedUntil = 0L)
                repository.updateAlarm(updated)
                if (updated.isEnabled) {
                    scheduler.schedule(updated)
                }
            }
        }
    }

    fun snoozeAlarm(minutes: Int = 5) {
        val currentRingingId = ringingState.value.alarmId
        val targetAlarm = _activeMissionAlarm.value
            ?: allAlarms.value.find { it.id == currentRingingId }

        AlarmSoundService.stopRinging(getApplication())
        _isMissionScreenOpen.value = false
        _activeMissionAlarm.value = null

        if (targetAlarm != null && targetAlarm.id != 9999L) {
            val snoozeUntil = System.currentTimeMillis() + minutes * 60 * 1000L
            viewModelScope.launch {
                val updated = targetAlarm.copy(
                    snoozedUntil = snoozeUntil,
                    isEnabled = true
                )
                repository.updateAlarm(updated)
                scheduler.scheduleSnooze(updated, snoozeUntil)
                _snackbarMessage.value = "⏰ Alarm ditunda $minutes menit. Alarm dikunci dan tidak dapat dihapus selama masa tunda."
            }
        } else {
            _snackbarMessage.value = "Alarm ditunda $minutes menit."
        }
    }

    fun openPhotoSpotsManager() {
        _isPhotoSpotsManagerOpen.value = true
    }

    fun closePhotoSpotsManager() {
        _isPhotoSpotsManagerOpen.value = false
    }

    fun savePhotoSpot(spot: PhotoSpotEntity) {
        viewModelScope.launch {
            repository.savePhotoSpot(spot)
        }
    }

    fun deletePhotoSpot(spot: PhotoSpotEntity) {
        viewModelScope.launch {
            repository.deletePhotoSpot(spot)
            _snackbarMessage.value = "Spot \"${spot.spotName}\" berhasil dihapus."
        }
    }
}
