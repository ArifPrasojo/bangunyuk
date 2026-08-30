package com.example.mission

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private val shakeThreshold = 650 // Speed threshold for intense wake-up shake

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val curTime = System.currentTimeMillis()
        // Only check every 100ms
        if ((curTime - lastUpdate) > 100) {
            val diffTime = (curTime - lastUpdate)
            lastUpdate = curTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = absDistance(x, y, z, lastX, lastY, lastZ) / diffTime * 10000

            if (speed > shakeThreshold) {
                onShake()
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    private fun absDistance(x: Float, y: Float, z: Float, lx: Float, ly: Float, lz: Float): Float {
        val dx = x - lx
        val dy = y - ly
        val dz = z - lz
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
