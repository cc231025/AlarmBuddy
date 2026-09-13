package com.example.alarmbuddy.platform

import kotlinx.coroutines.flow.MutableStateFlow

// Set when the user taps "Stop" on a ringing AlarmKit alarm (see
// AlarmKitBridge.swift's AlarmStopIntent) or on cold launch via
// checkPendingAlarmNavigation() below. App.kt observes this to jump straight
// to the Ringing screen -- the iOS equivalent of the original app's
// AlarmReceiver -> MainActivity Intent + SharedPreferences dance.
object AlarmNotificationRouter {
    val pendingAlarmId = MutableStateFlow<Long?>(null)
}

// AlarmStopIntent (Swift, running as an App Intent -- possibly in a
// different process from the rest of the app) can't safely touch Kotlin
// state directly, so it persists the pending navigation as plain
// NSUserDefaults values instead (no App Group needed, unlike the sample
// AlarmKit integrations -- App Groups require a paid Apple Developer
// account). This reads those values back into pendingAlarmId above.
//
// Called once at cold launch (MainViewController.kt) and every time the app
// becomes active again (ComposeView.swift), since "Stop" always foregrounds
// the app (AlarmStopIntent.openAppWhenRun = true) rather than relying on the
// app already being alive to observe anything in real time.
fun checkPendingAlarmNavigation() {
    val appSettings = AppSettings()
    val pendingId = appSettings.getInt("ringingAlarmId")
    if (appSettings.getString("navigateTo") == "Ringing" && pendingId != null) {
        AlarmNotificationRouter.pendingAlarmId.value = pendingId.toLong()
        appSettings.clear("navigateTo")
        appSettings.clear("ringingAlarmId")
    }
}
