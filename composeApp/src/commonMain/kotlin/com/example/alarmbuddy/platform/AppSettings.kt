package com.example.alarmbuddy.platform

// Replaces Android's SharedPreferences("AlarmState", ...), which AlarmReceiver/
// MainActivity used to remember "an alarm is ringing" across process death.
// iOS actual is backed by NSUserDefaults.
expect class AppSettings() {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun getInt(key: String): Int?
    fun putInt(key: String, value: Int)
    fun clear(key: String)
}
