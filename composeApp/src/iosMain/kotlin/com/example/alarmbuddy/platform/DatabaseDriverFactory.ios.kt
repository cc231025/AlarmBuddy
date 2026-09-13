package com.example.alarmbuddy.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.alarmbuddy.db.AlarmBuddyDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(AlarmBuddyDatabase.Schema, "AlarmBuddy.db")
}
