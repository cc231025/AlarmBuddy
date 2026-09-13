package com.example.alarmbuddy.platform

// Desktop dev stand-in only (never shipped): there's no Guided Access concept
// on the JVM target, so this always reports "off" and opening Settings is a
// no-op. Lets the shared UI (the nag banner/onboarding dialog) still compile
// and be smoke-tested with `./gradlew :composeApp:run`.
actual fun isGuidedAccessEnabled(): Boolean = false

actual fun openSystemSettings() {
    println("openSystemSettings() is a no-op on the desktop dev build.")
}
