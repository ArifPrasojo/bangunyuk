package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity
import com.example.data.local.AlarmEntity
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancel(alarm.id)
            return
        }

        val triggerTime = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.daysOfWeek)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_MISSION_TYPE, alarm.missionType)
            putExtra(AlarmReceiver.EXTRA_ALARM_TARGET_PLACE, alarm.photoTargetPlace)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND, alarm.soundName)
            putExtra(AlarmReceiver.EXTRA_ALARM_VOLUME, alarm.volume)
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, alarm.vibrate)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            flags
        )

        // Show on lockscreen clock
        val showIntent = Intent(context, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (alarm.id + 10000).toInt(),
            showIntent,
            flags
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun scheduleSnooze(alarm: AlarmEntity, triggerTime: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_ALARM_MISSION_TYPE, alarm.missionType)
            putExtra(AlarmReceiver.EXTRA_ALARM_TARGET_PLACE, alarm.photoTargetPlace)
            putExtra(AlarmReceiver.EXTRA_ALARM_SOUND, alarm.soundName)
            putExtra(AlarmReceiver.EXTRA_ALARM_VOLUME, alarm.volume)
            putExtra(AlarmReceiver.EXTRA_ALARM_VIBRATE, alarm.vibrate)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            flags
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (alarm.id + 10000).toInt(),
            showIntent,
            flags
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancel(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_ALARM
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            flags
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        fun calculateNextTriggerTime(hour: Int, minute: Int, daysOfWeek: String): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val activeDays = if (daysOfWeek.isBlank()) {
                emptySet()
            } else {
                daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            }

            if (activeDays.isEmpty()) {
                // One-time alarm
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            // Map Calendar day of week (Sunday=1, Monday=2..Saturday=7) to 1..7 (Mon=1..Sun=7)
            fun calDayToCustomDay(calDay: Int): Int {
                return when (calDay) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }
            }

            for (offset in 0..7) {
                val checkCal = (target.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                }
                val checkDay = calDayToCustomDay(checkCal.get(Calendar.DAY_OF_WEEK))
                if (activeDays.contains(checkDay)) {
                    if (checkCal.timeInMillis > now.timeInMillis) {
                        return checkCal.timeInMillis
                    }
                }
            }

            // Fallback next occurrence
            target.add(Calendar.DAY_OF_YEAR, 1)
            return target.timeInMillis
        }

        fun getRemainingTimeString(hour: Int, minute: Int, daysOfWeek: String): String {
            val nextTime = calculateNextTriggerTime(hour, minute, daysOfWeek)
            val diffMs = nextTime - System.currentTimeMillis()
            if (diffMs <= 0) return "Sebentar lagi"

            val totalMinutes = diffMs / (1000 * 60)
            val days = totalMinutes / (60 * 24)
            val hours = (totalMinutes % (60 * 24)) / 60
            val minutes = totalMinutes % 60

            return when {
                days > 0 -> "$days hari $hours jam lagi"
                hours > 0 -> "$hours jam $minutes mnt lagi"
                minutes > 0 -> "$minutes menit lagi"
                else -> "Kurang dari 1 menit"
            }
        }

        fun getNextAlarmTriggerTime(alarm: AlarmEntity): Long {
            if (alarm.snoozedUntil > System.currentTimeMillis()) {
                return alarm.snoozedUntil
            }
            return calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.daysOfWeek)
        }

        fun getSnoozeRemainingString(snoozedUntil: Long): String {
            val diffMs = snoozedUntil - System.currentTimeMillis()
            if (diffMs <= 0) return "Sebentar lagi"
            val totalMinutes = (diffMs + 59999) / (1000 * 60)
            return if (totalMinutes <= 1) "Kurang dari 1 menit" else "$totalMinutes menit"
        }
    }
}
