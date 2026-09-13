package com.example.alarmbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmbuddy.platform.AlarmNotificationRouter
import com.example.alarmbuddy.ui.Add
import com.example.alarmbuddy.ui.AlarmViewModel
import com.example.alarmbuddy.ui.CameraScreen
import com.example.alarmbuddy.ui.Edit
import com.example.alarmbuddy.ui.Home
import com.example.alarmbuddy.ui.Ringing
import com.example.alarmbuddy.ui.theme.AlarmBuddyTheme

// Hand-rolled navigation instead of a navigation library: the app only has
// five screens and no deep back-stack behavior beyond "Cancel/Save go back to
// where you came from", so a simple sealed type + one `when` is less risk
// than pulling in (and version-matching) a multiplatform nav library.
sealed interface Screen {
    data object Home : Screen
    data object Add : Screen
    data class Edit(val alarmId: Long) : Screen
    data class Ringing(val alarmId: Long) : Screen
    data class BarcodeCamera(val returnTo: Screen) : Screen
}

@Composable
fun App(appContainer: AppContainer) {
    AlarmBuddyTheme {
        val viewModel: AlarmViewModel = viewModel {
            AlarmViewModel(appContainer.alarmRepository, appContainer.barcodeRepository)
        }

        var screen by remember { mutableStateOf<Screen>(Screen.Home) }

        // Cold-launched (or resumed) from a tapped alarm notification -> jump
        // straight to Ringing. Mirrors the original app's SharedPreferences
        // "navigateTo"/"id" check in MainActivity.onCreate. This same signal
        // is also how BackgroundKeepAlive (iOS-only) routes into Ringing when
        // its own background timer -- not a notification tap -- is what
        // actually fired the alarm.
        val pendingAlarmId by AlarmNotificationRouter.pendingAlarmId.collectAsState()
        LaunchedEffect(pendingAlarmId) {
            pendingAlarmId?.let { id ->
                screen = Screen.Ringing(id)
                AlarmNotificationRouter.pendingAlarmId.value = null
            }
        }

        // Keep the platform scheduler's idea of "what's the next armed alarm"
        // in sync with the database -- on cold launch with whatever's already
        // activated, and every time an alarm is added/edited/toggled/deleted.
        // On iOS this is what BackgroundKeepAlive polls against; it's a no-op
        // on the desktop dev target. See AlarmScheduler.kt / BackgroundKeepAlive.ios.kt.
        val alarmList by viewModel.alarmUIState.collectAsStateWithLifecycle()
        LaunchedEffect(alarmList) {
            appContainer.alarmScheduler.syncArmedAlarms(alarmList)
        }

        when (val current = screen) {
            is Screen.Home -> Home(
                viewModel = viewModel,
                alarmScheduler = appContainer.alarmScheduler,
                appContainer = appContainer,
                onAddAlarm = { screen = Screen.Add },
                onEditAlarm = { id -> screen = Screen.Edit(id) },
            )

            is Screen.Add -> Add(
                viewModel = viewModel,
                onCancel = { screen = Screen.Home },
                onSave = { screen = Screen.Home },
                onOpenCamera = { screen = Screen.BarcodeCamera(returnTo = Screen.Add) },
            )

            is Screen.Edit -> Edit(
                alarmId = current.alarmId,
                viewModel = viewModel,
                alarmScheduler = appContainer.alarmScheduler,
                onCancel = { screen = Screen.Home },
                onSave = { screen = Screen.Home },
                onOpenCamera = { screen = Screen.BarcodeCamera(returnTo = current) },
            )

            is Screen.Ringing -> Ringing(
                alarmId = current.alarmId,
                viewModel = viewModel,
                appContainer = appContainer,
                onFinished = { screen = Screen.Home },
            )

            is Screen.BarcodeCamera -> CameraScreen(
                mode = "setBarcode",
                viewModel = viewModel,
                onBarcodeSaved = { screen = current.returnTo },
            )
        }
    }
}
