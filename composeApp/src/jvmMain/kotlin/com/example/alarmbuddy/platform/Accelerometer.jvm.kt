package com.example.alarmbuddy.platform

// Desktop stand-in only (never shipped): no accelerometer hardware to read.
actual class Accelerometer actual constructor(
    private val onReading: (x: Double, y: Double, z: Double) -> Unit,
) {
    actual fun start() {
        // no-op on desktop
    }

    actual fun stop() {
        // no-op on desktop
    }
}
