package com.example.alarmbuddy

import androidx.compose.ui.window.ComposeUIViewController
import com.example.alarmbuddy.platform.DatabaseDriverFactory
import com.example.alarmbuddy.platform.checkPendingAlarmNavigation
import platform.UIKit.UIViewController

// Entry point called from Swift (see iosApp/iosApp/ComposeView.swift). By
// this point ComposeView.swift has already installed the AlarmKit bridge
// (see AlarmKitBridge.swift/.kt) and requested AlarmKit authorization.
fun MainViewController(): UIViewController {
    val appContainer = AppContainer(DatabaseDriverFactory())

    // Cold launch (e.g. the user tapped "Stop" on a ringing AlarmKit alarm,
    // or force-quit the app while an alarm was actively ringing, then
    // reopened it from the Home Screen icon): if we were mid-ring, go
    // straight back to the Ringing/task-gate screen instead of a normal Home
    // screen. Mirrors the original app's SharedPreferences check in
    // MainActivity.onCreate.
    checkPendingAlarmNavigation()

    return ComposeUIViewController {
        App(appContainer)
    }
}
