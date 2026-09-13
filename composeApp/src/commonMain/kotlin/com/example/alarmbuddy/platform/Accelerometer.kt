package com.example.alarmbuddy.platform

// Raw accelerometer feed. Replaces Android's SensorManager/SensorEventListener.
// All the shake-detection *logic* (smoothing, threshold, counting) stays in
// commonMain (see ui/taskComposables.kt) and just consumes these readings, so
// only the raw sensor plumbing is platform-specific.
expect class Accelerometer(onReading: (x: Double, y: Double, z: Double) -> Unit) {
    fun start()
    fun stop()
}
