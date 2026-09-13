package com.example.alarmbuddy

import androidx.compose.ui.window.ComposeUIViewController
import com.example.alarmbuddy.platform.AlarmNotificationRouter
import com.example.alarmbuddy.platform.DatabaseDriverFactory
import com.example.alarmbuddy.platform.NotificationDelegate
import com.example.alarmbuddy.platform.requestNotificationAuthorization
import platform.UIKit.UIViewController

// Entry point called from Swift (see iosApp/iosApp/iosAppApp.swift).
fun MainViewController(): UIViewController {
    NotificationDelegate.install()
    requestNotificationAuthorization { granted ->
        if (!granted) {
            // The permission screen in ui/ isn't built yet (see MIGRATION_PLAN.md
            // task list) -- for now this just means scheduled alarms silently
            // won't produce any notifications until the user flips the
            // permission on in Settings.
        }
    }

    val appContainer = AppContainer(DatabaseDriverFactory())

    // Cold launch (e.g. the user force-quit the app while an alarm was
    // actively ringing, then reopened it from the home screen icon rather
    // than a notification): if we were mid-ring, go straight back to the
    // Ringing/task-gate screen instead of a normal Home screen. This is the
    // one part of "you can't just make it go away" that iOS *does* let us
    // guarantee, even though it can't stop a force-quit from cutting the
    // sound itself. Mirrors the original app's SharedPreferences check in
    // MainActivity.onCreate.
    val pendingId = appContainer.appSettings.getInt("ringingAlarmId")
    if (appContainer.appSettings.getString("navigateTo") == "Ringing" && pendingId != null) {
        AlarmNotificationRouter.pendingAlarmId.value = pendingId.toLong()
    }

    return ComposeUIViewController {
        App(appContainer)
    }
}
