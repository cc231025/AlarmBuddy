package com.example.alarmbuddy.platform

import kotlinx.coroutines.flow.MutableStateFlow

// Set by the iOS notification delegate (see NotificationDelegate.ios.kt) when
// the user taps a scheduled alarm notification. App.kt observes this to jump
// straight to the Ringing screen -- the iOS equivalent of the original app's
// AlarmReceiver -> MainActivity Intent + SharedPreferences dance.
object AlarmNotificationRouter {
    val pendingAlarmId = MutableStateFlow<Long?>(null)
}
