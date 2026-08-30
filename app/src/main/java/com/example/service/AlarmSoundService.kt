package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmSoundService : Service() {

    private lateinit var soundPlayer: SoundPlayer

    override fun onCreate() {
        super.onCreate()
        soundPlayer = SoundPlayer(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "Bangun Pagi"
        val sound = intent?.getStringExtra(EXTRA_ALARM_SOUND) ?: "Energetic Sunrise"
        val volume = intent?.getIntExtra(EXTRA_ALARM_VOLUME, 100) ?: 100
        val vibrate = intent?.getBooleanExtra(EXTRA_ALARM_VIBRATE, true) ?: true
        val missionType = intent?.getStringExtra(EXTRA_ALARM_MISSION_TYPE) ?: "PHOTO"
        val targetPlace = intent?.getStringExtra(EXTRA_ALARM_TARGET_PLACE) ?: "TOILET"

        _ringingAlarmState.value = RingingState(
            isRinging = true,
            alarmId = alarmId,
            label = label,
            missionType = missionType,
            targetPlace = targetPlace
        )

        val notification = buildForegroundNotification(alarmId, label, missionType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        soundPlayer.playAlarm(sound, volume, vibrate)

        return START_STICKY
    }

    private fun stopAlarm() {
        soundPlayer.stop()
        _ringingAlarmState.value = RingingState(isRinging = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm BangunYuk",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi alarm aktif dengan misi bangun"
                setSound(null, null)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(
        alarmId: Long,
        label: String,
        missionType: String
    ): Notification {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_IS_RINGING, true)
        }

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            pendingFlags
        )

        val missionDesc = when (missionType) {
            "PHOTO" -> "Foto Toilet / Kamar Mandi untuk matikan"
            "MATH" -> "Selesaikan soal matematika untuk matikan"
            "SHAKE" -> "Goyang HP untuk matikan"
            "STEPS" -> "Jalan kaki untuk matikan"
            else -> "Selesaikan misi untuk matikan"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $label - Waktunya Bangun!")
            .setContentText(missionDesc)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    data class RingingState(
        val isRinging: Boolean = false,
        val alarmId: Long = -1L,
        val label: String = "",
        val missionType: String = "PHOTO",
        val targetPlace: String = "TOILET"
    )

    companion object {
        const val CHANNEL_ID = "alarm_ringing_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_ALARM = "com.example.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.ACTION_STOP_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_ALARM_SOUND = "extra_alarm_sound"
        const val EXTRA_ALARM_VOLUME = "extra_alarm_volume"
        const val EXTRA_ALARM_VIBRATE = "extra_alarm_vibrate"
        const val EXTRA_ALARM_MISSION_TYPE = "extra_alarm_mission_type"
        const val EXTRA_ALARM_TARGET_PLACE = "extra_alarm_target_place"
        const val EXTRA_IS_RINGING = "extra_is_ringing"

        private val _ringingAlarmState = MutableStateFlow(RingingState())
        val ringingAlarmState: StateFlow<RingingState> = _ringingAlarmState.asStateFlow()

        fun stopRinging(context: Context) {
            val stopIntent = Intent(context, AlarmSoundService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(stopIntent)
            _ringingAlarmState.value = RingingState(isRinging = false)
        }

        fun triggerTestAlarm(
            context: Context,
            label: String = "Uji Coba Alarm",
            missionType: String = "PHOTO",
            targetPlace: String = "TOILET"
        ) {
            val intent = Intent(context, AlarmSoundService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, 9999L)
                putExtra(EXTRA_ALARM_LABEL, label)
                putExtra(EXTRA_ALARM_MISSION_TYPE, missionType)
                putExtra(EXTRA_ALARM_TARGET_PLACE, targetPlace)
                putExtra(EXTRA_ALARM_SOUND, "Energetic Sunrise")
                putExtra(EXTRA_ALARM_VOLUME, 100)
                putExtra(EXTRA_ALARM_VIBRATE, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
