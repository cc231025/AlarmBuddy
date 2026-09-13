package com.example.alarmbuddy

import androidx.compose.ui.window.ComposeUIViewController
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

    return ComposeUIViewController {
        App(appContainer)
    }
}
