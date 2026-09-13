package com.example.alarmbuddy

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.alarmbuddy.platform.AlarmNotificationRouter
import com.example.alarmbuddy.platform.DatabaseDriverFactory

// Desktop dev entry point only (never shipped) -- lets the shared Compose UI
// be smoke-tested with `./gradlew :composeApp:run` on any machine, no Xcode
// required. See MIGRATION_PLAN.md.
fun main() = application {
    val appContainer = AppContainer(DatabaseDriverFactory())

    val pendingId = appContainer.appSettings.getInt("ringingAlarmId")
    if (appContainer.appSettings.getString("navigateTo") == "Ringing" && pendingId != null) {
        AlarmNotificationRouter.pendingAlarmId.value = pendingId.toLong()
    }

    Window(onCloseRequest = ::exitApplication, title = "AlarmBuddy (desktop dev build)") {
        App(appContainer)
    }
}
