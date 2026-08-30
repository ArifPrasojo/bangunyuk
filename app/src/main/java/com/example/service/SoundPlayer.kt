package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null
    private var vibrationJob: Job? = null
    private var audioJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playAlarm(soundName: String, volumeLevel: Int = 100, shouldVibrate: Boolean = true) {
        stop()

        // 1. Start Audio
        audioJob = scope.launch {
            try {
                playSynthesizedOrRingtone(soundName, volumeLevel)
            } catch (e: Exception) {
                // Fallback to tone generation
                playToneLoop(440.0, 880.0)
            }
        }

        // 2. Start Vibration
        if (shouldVibrate) {
            startVibration()
        }
    }

    private fun playSynthesizedOrRingtone(soundName: String, volumePercent: Int) {
        val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        // Enforce strict minimum 40% volume playback during alarm ringing
        val effectiveVolume = volumePercent.coerceIn(40, 100)

        // Also ensure system alarm stream volume is at least 40%
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val minVol = (maxVol * 0.40f).toInt().coerceAtLeast(1)
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                if (currentVol < minVol) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, minVol, 0)
                }
            }
        } catch (e: Exception) {
            // Ignore if permission denied
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, defaultRingtoneUri)
                isLooping = true
                val vol = (effectiveVolume / 100f).coerceIn(0.4f, 1.0f)
                setVolume(vol, vol)
                prepare()
                start()
            }
        } catch (e: Exception) {
            // If system ringtone fails, use dynamic PCM audio synthesizer
            playToneLoop(659.25, 880.0)
        }
    }

    private fun playToneLoop(freq1: Double, freq2: Double) {
        val sampleRate = 44100
        val numSamples = sampleRate / 2 // 0.5 sec
        val buffer = ShortArray(numSamples)

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBufferSize, numSamples * 2))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        audioJob = scope.launch {
            var toggle = false
            while (isActive) {
                val currentFreq = if (toggle) freq1 else freq2
                toggle = !toggle

                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / currentFreq)
                    buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.85).toInt().toShort()
                }

                audioTrack?.write(buffer, 0, buffer.size)
                delay(250)
            }
        }
    }

    private fun startVibration() {
        vibrationJob = scope.launch {
            val pattern = longArrayOf(0, 600, 300, 600, 300, 800, 500)
            while (isActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
                delay(3100)
            }
        }
    }

    fun stop() {
        audioJob?.cancel()
        audioJob = null
        vibrationJob?.cancel()
        vibrationJob = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null

        try {
            audioTrack?.let {
                it.stop()
                it.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null

        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
