package com.example.alarmbuddy.platform

import kotlinx.coroutines.flow.MutableStateFlow

// The original Android ShakeTask locked the Activity to portrait (rotating
// mid-shake would reset the count). There's no Compose Multiplatform API for
// that, so this is a plain flag the hosting iOS UIViewController reads from
// its `supportedInterfaceOrientations` override (see iosApp/iosApp/ComposeViewController.swift).
object OrientationLock {
    val isPortraitLocked = MutableStateFlow(false)
}
