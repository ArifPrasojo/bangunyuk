package com.example.mission

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

class StepDetector(
    private val context: Context,
    private val onStep: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var hasStepSensor = false
    private var lastStepTime = 0L

    // Accelerometer filter values
    private val gravity = FloatArray(3) { 0f }
    private var lastLinearMag = 0f
    private var isPeakWaiting = false

    fun start() {
        if (stepDetectorSensor != null) {
            hasStepSensor = sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val now = System.currentTimeMillis()

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (now - lastStepTime > 250) {
                lastStepTime = now
                onStep()
            }
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Isolate gravity with low-pass filter
            val alpha = 0.8f
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            // Calculate linear acceleration (without gravity)
            val linearX = event.values[0] - gravity[0]
            val linearY = event.values[1] - gravity[1]
            val linearZ = event.values[2] - gravity[2]
            val linearMag = sqrt((linearX * linearX + linearY * linearY + linearZ * linearZ).toDouble()).toFloat()

            // Detect natural walking gait cycle (crest and trough)
            if (linearMag > 2.0f && !isPeakWaiting && (now - lastStepTime) > 300) {
                isPeakWaiting = true
            } else if (isPeakWaiting && linearMag < 1.2f) {
                isPeakWaiting = false
                lastStepTime = now
                onStep()
            }
            lastLinearMag = linearMag
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

