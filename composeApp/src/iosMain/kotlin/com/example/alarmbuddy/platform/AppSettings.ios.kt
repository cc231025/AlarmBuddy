package com.example.alarmbuddy.platform

import platform.Foundation.NSUserDefaults

// Replaces the Android SharedPreferences("AlarmState", ...) usage.
actual class AppSettings actual constructor() {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String): String? = defaults.stringForKey(key)

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun getInt(key: String): Int? =
        if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else null

    actual fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }

    actual fun clear(key: String) {
        defaults.removeObjectForKey(key)
    }
}
