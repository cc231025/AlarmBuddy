package com.example.alarmbuddy.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

// Android's SensorEvent reports raw acceleration in m/s^2; CoreMotion reports
// it in multiples of g (~9.81 m/s^2). Scaling here means the shake-detection
// threshold constants in ui/taskComposables.kt (tuned against Android's units)
// stay meaningful without change.
private const val G_TO_METERS_PER_SECOND_SQUARED = 9.81

@OptIn(ExperimentalForeignApi::class)
actual class Accelerometer actual constructor(
    private val onReading: (x: Double, y: Double, z: Double) -> Unit,
) {
    private val motionManager = CMMotionManager()

    actual fun start() {
        if (!motionManager.accelerometerAvailable) return
        motionManager.accelerometerUpdateInterval = 1.0 / 30.0 // ~SENSOR_DELAY_UI
        motionManager.startAccelerometerUpdatesToQueue(
            queue = NSOperationQueue.mainQueue,
            withHandler = { data, _ ->
                data?.acceleration?.useContents {
                    onReading(
                        x * G_TO_METERS_PER_SECOND_SQUARED,
                        y * G_TO_METERS_PER_SECOND_SQUARED,
                        z * G_TO_METERS_PER_SECOND_SQUARED,
                    )
                }
            },
        )
    }

    actual fun stop() {
        motionManager.stopAccelerometerUpdates()
    }
}
