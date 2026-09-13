package com.example.alarmbuddy.platform

import app.cash.sqldelight.db.SqlDriver
import com.example.alarmbuddy.db.AlarmBuddyDatabase

// One SqlDriver implementation per platform: NativeSqliteDriver on iOS,
// a plain JDBC sqlite driver on desktop/JVM (used only for local dev builds).
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(factory: DatabaseDriverFactory): AlarmBuddyDatabase =
    AlarmBuddyDatabase(factory.createDriver())
