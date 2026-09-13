package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.soundFileNameFor

/**
 * Replaces AlarmManager.setExactAndAllowWhileIdle() + AlarmReceiver + AlarmService.
 *
 * Scheduling itself happens in Swift via Apple's AlarmKit (see
 * AlarmKitBridge.swift and AlarmKitBridge.kt) -- unlike the old local-
 * notification-burst approach this replaced, an AlarmKit alarm rings through
 * Silent Mode and Focus, and survives the app being fully force-quit, because
 * it's scheduled at the OS level the same way the built-in Clock app's
 * alarms are.
 */
actual class AlarmScheduler actual constructor() {

    actual fun schedule(alarm: Alarm) {
        AlarmKitBridgeHolder.bridge?.scheduleAlarm(
            alarmId = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            soundFileName = "${soundFileNameFor(alarm.audioFile)}_notif.wav",
        )
    }

    actual fun cancel(alarm: Alarm) {
        AlarmKitBridgeHolder.bridge?.cancelAlarm(alarm.id)
    }

    actual fun syncArmedAlarms(alarms: List<Alarm>) {
        // No-op on iOS now: each alarm is scheduled/canceled individually via
        // schedule()/cancel() above, and AlarmKit alarms persist on their own
        // (backed by the OS, not this process), so there's no "keep the
        // process alive to track what's next" bookkeeping to do anymore.
    }
}
