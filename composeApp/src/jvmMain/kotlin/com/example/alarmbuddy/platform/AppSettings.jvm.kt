package com.example.alarmbuddy.platform

import java.util.prefs.Preferences

// Desktop stand-in only (never shipped): backed by java.util.prefs instead of
// NSUserDefaults.
actual class AppSettings actual constructor() {
    private val prefs = Preferences.userRoot().node("com.example.alarmbuddy.desktop")

    actual fun getString(key: String): String? = prefs.get(key, null)

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    actual fun getInt(key: String): Int? =
        if (prefs.get(key, null) != null) prefs.getInt(key, 0) else null

    actual fun putInt(key: String, value: Int) {
        prefs.putInt(key, value)
    }

    actual fun getBoolean(key: String): Boolean = prefs.getBoolean(key, false)

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }

    actual fun getFloat(key: String): Float? =
        if (prefs.get(key, null) != null) prefs.getFloat(key, 0f) else null

    actual fun putFloat(key: String, value: Float) {
        prefs.putFloat(key, value)
    }

    actual fun clear(key: String) {
        prefs.remove(key)
    }
}
