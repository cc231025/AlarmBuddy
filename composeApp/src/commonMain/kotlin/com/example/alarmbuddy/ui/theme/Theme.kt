package com.example.alarmbuddy.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    ) {
        // MaterialTheme only provides theming tokens -- it doesn't paint
        // anything itself. Without this Surface, any screen that doesn't
        // fillMaxSize() its own root (e.g. HomeScreen's Column, which only
        // fillMaxWidth()s) leaves the rest of the window showing whatever
        // the native view's default background is, which on iOS is black,
        // not this app's intended background color.
        Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
            content()
        }
    }
}
