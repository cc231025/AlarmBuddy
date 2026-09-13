package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.Alarm

// Replaces Android's AlarmManager.setExactAndAllowWhileIdle() + AlarmReceiver
// (BroadcastReceiver) + AlarmService (foreground service).
//
// The iOS actual schedules a real AlarmKit alarm (iOS 26+, see
// AlarmScheduler.ios.kt / AlarmKitBridge.swift) -- the same OS-level
// mechanism the built-in Clock app's alarms use, which rings through Silent
// Mode/Focus and survives the app being fully force-quit, with no paid
// developer account or Critical Alerts approval needed.
expect class AlarmScheduler() {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)

    // Called whenever the full alarm list changes (added/edited/toggled/
    // deleted, and once on cold launch with whatever's in the database).
    // Currently a no-op on every platform -- each alarm is already
    // individually scheduled/canceled via schedule()/cancel() above, and
    // AlarmKit alarms persist on their own -- kept as a hook in case a
    // platform ever needs to react to the full armed-alarm list changing.
    fun syncArmedAlarms(alarms: List<Alarm>)
}
