package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.Alarm

// Replaces Android's AlarmManager.setExactAndAllowWhileIdle() + AlarmReceiver
// (BroadcastReceiver) + AlarmService (foreground service).
//
// iOS has no equivalent of "wake the app and start playing audio with zero
// user interaction, even if the app was killed." The iOS actual instead
// schedules a burst of local notifications clustered around the alarm time
// (a loud custom sound on each), which is the same technique real iOS alarm
// apps (Alarmy, etc.) use. Tapping any one of them opens the app straight
// into the Ringing screen. See MIGRATION_PLAN.md for the full rationale,
// including the Critical Alerts entitlement needed to make the sound ignore
// Silent Mode / Focus while the notification fires in the background.
expect class AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}
