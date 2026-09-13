package com.example.alarmbuddy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmbuddy.AppContainer
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.platform.isGuidedAccessEnabled
import com.example.alarmbuddy.platform.openSystemSettings
import kotlinx.coroutines.delay

private const val GUIDED_ACCESS_ONBOARDING_SEEN_KEY = "guidedAccessOnboardingSeen"
private const val POLL_INTERVAL_MS = 3000L

/**
 * Guided Access (Settings -> Accessibility -> Guided Access) is the free,
 * built-in lever that gets AlarmBuddy closest to the original Android app's
 * "can't leave it, can't turn down the volume" guarantee -- see
 * MIGRATION_PLAN.md. There's no API to turn it on for the user (that would
 * defeat its own point), so instead of a one-time permission this shows up
 * as: a one-time in-app walkthrough the first time someone arms an alarm, and
 * after that a live reminder on Home whenever an alarm is armed but Guided
 * Access isn't currently on.
 */
@Composable
fun GuidedAccessReminder(
    alarms: List<Alarm>,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    var guidedAccessOn by remember { mutableStateOf(isGuidedAccessEnabled()) }
    var showOnboarding by remember { mutableStateOf(false) }
    var showHowTo by remember { mutableStateOf(false) }

    val hasArmedAlarm = alarms.any { it.activated }

    // iOS gives no callback for "Guided Access turned on/off" -- the only
    // API is a plain state check -- so a slow poll while Home is visible is
    // the only way to notice it happened.
    LaunchedEffect(Unit) {
        while (true) {
            guidedAccessOn = isGuidedAccessEnabled()
            delay(POLL_INTERVAL_MS)
        }
    }

    LaunchedEffect(hasArmedAlarm) {
        if (hasArmedAlarm && !appContainer.appSettings.getBoolean(GUIDED_ACCESS_ONBOARDING_SEEN_KEY)) {
            showOnboarding = true
        }
    }

    if (hasArmedAlarm && !guidedAccessOn) {
        Card(
            modifier = modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(text = "Guided Access is off", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "For the strongest lock tonight -- no volume buttons, can't leave the " +
                        "app until the tasks are done -- triple-click the side button before bed.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    TextButton(onClick = { showHowTo = true }) {
                        Text("How do I set this up?")
                    }
                }
            }
        }
    }

    if (showOnboarding) {
        GuidedAccessOnboardingDialog(
            onDismiss = {
                showOnboarding = false
                appContainer.appSettings.putBoolean(GUIDED_ACCESS_ONBOARDING_SEEN_KEY, true)
            },
        )
    }

    if (showHowTo) {
        GuidedAccessOnboardingDialog(onDismiss = { showHowTo = false })
    }
}

@Composable
private fun GuidedAccessOnboardingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lock the alarm down with Guided Access") },
        text = {
            Column {
                Text(
                    "Free, built into every iPhone -- no App Store, no account. Set the shortcut " +
                        "up once:",
                )
                Spacer(Modifier.height(8.dp))
                Text("1. Settings -> Accessibility -> Accessibility Shortcut -> check Guided Access.")
                Spacer(Modifier.height(4.dp))
                Text(
                    "2. Settings -> Accessibility -> Guided Access -> Passcode Settings (set a " +
                        "passcode), and turn on \"Time Limits: Hardware Buttons\" if you also want " +
                        "the volume buttons disabled.",
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "That's the one-time part. After that, every night: open AlarmBuddy, triple-" +
                        "click the side button, and Guided Access starts -- the phone won't let you " +
                        "leave the app or touch the volume until you triple-click again and enter " +
                        "that passcode, which this app's Ringing screen only reaches after the " +
                        "tasks are solved.",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                openSystemSettings()
                onDismiss()
            }) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        },
    )
}
