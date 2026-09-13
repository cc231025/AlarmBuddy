package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.Alarm

// Desktop stand-in only (never shipped): there is no iOS notification center
// to schedule against outside iosMain, so this just logs. Its only purpose is
// letting the shared UI/viewmodel code compile and run on a normal JVM for
// local smoke-testing without Xcode.
actual class AlarmScheduler {
    actual fun schedule(alarm: Alarm) {
        println("[desktop stub] would schedule alarm ${alarm.id} at ${alarm.hour}:${alarm.minute}")
    }

    actual fun cancel(alarm: Alarm) {
        println("[desktop stub] would cancel alarm ${alarm.id}")
    }
}
