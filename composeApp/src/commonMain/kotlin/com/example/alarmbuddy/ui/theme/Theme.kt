package com.example.alarmbuddy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The original Android AlarmBuddyTheme computed a dynamic-color/dark-theme
// scheme but never actually used it -- it always rendered with a hardcoded
// dark `customColorScheme`. This keeps that same (likely intentional, given
// the "annoying alarm at 3am" vibe of the app) always-dark look rather than
// reintroducing dead branching.
@Composable
fun AlarmBuddyTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = HighlightColor,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        secondary = SecondaryColor,
        background = BackgroundColor,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
