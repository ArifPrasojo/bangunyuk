package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import com.example.MainActivity
import com.example.service.AlarmSoundService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "BangunYuk:AlarmWakeLock"
        )
        wakeLock.acquire(3 * 60 * 1000L) // 3 minutes timeout

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Bangun Pagi"
        val sound = intent.getStringExtra(EXTRA_ALARM_SOUND) ?: "Energetic Sunrise"
        val volume = intent.getIntExtra(EXTRA_ALARM_VOLUME, 100)
        val vibrate = intent.getBooleanExtra(EXTRA_ALARM_VIBRATE, true)
        val missionType = intent.getStringExtra(EXTRA_ALARM_MISSION_TYPE) ?: "PHOTO"
        val targetPlace = intent.getStringExtra(EXTRA_ALARM_TARGET_PLACE) ?: "TOILET"

        // 1. Start foreground alarm sound service
        val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_START_ALARM
            putExtra(AlarmSoundService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmSoundService.EXTRA_ALARM_SOUND, sound)
            putExtra(AlarmSoundService.EXTRA_ALARM_VOLUME, volume)
            putExtra(AlarmSoundService.EXTRA_ALARM_VIBRATE, vibrate)
            putExtra(AlarmSoundService.EXTRA_ALARM_MISSION_TYPE, missionType)
            putExtra(AlarmSoundService.EXTRA_ALARM_TARGET_PLACE, targetPlace)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Launch Main Activity directly to show mission & alarm screen
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra(AlarmSoundService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmSoundService.EXTRA_IS_RINGING, true)
            putExtra(AlarmSoundService.EXTRA_ALARM_MISSION_TYPE, missionType)
            putExtra(AlarmSoundService.EXTRA_ALARM_TARGET_PLACE, targetPlace)
            putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, label)
        }
        context.startActivity(activityIntent)
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.example.ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_SOUND = "extra_alarm_sound"
        const val EXTRA_ALARM_VOLUME = "extra_alarm_volume"
        const val EXTRA_ALARM_VIBRATE = "extra_alarm_vibrate"
        const val EXTRA_ALARM_MISSION_TYPE = "extra_alarm_mission_type"
        const val EXTRA_ALARM_TARGET_PLACE = "extra_alarm_target_place"
    }
}
