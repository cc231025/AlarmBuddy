package com.example.alarmbuddy.platform

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.example.alarmbuddy.db.AlarmBuddyDatabase
import com.example.alarmbuddy.db.AlarmEntity

// One SqlDriver implementation per platform: NativeSqliteDriver on iOS,
// a plain JDBC sqlite driver on desktop/JVM (used only for local dev builds).
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// SQLite has no native boolean column type, so every `INTEGER AS Boolean`
// column in Alarm.sq needs an explicit adapter converting to/from Long.
private val booleanAdapter = object : ColumnAdapter<Boolean, Long> {
    override fun decode(databaseValue: Long): Boolean = databaseValue != 0L
    override fun encode(value: Boolean): Long = if (value) 1L else 0L
}

fun createDatabase(factory: DatabaseDriverFactory): AlarmBuddyDatabase =
    AlarmBuddyDatabase(
        driver = factory.createDriver(),
        AlarmEntityAdapter = AlarmEntity.Adapter(
            activatedAdapter = booleanAdapter,
            barcodeTaskAdapter = booleanAdapter,
            shakeTaskAdapter = booleanAdapter,
            mathTaskAdapter = booleanAdapter,
            memoryTaskAdapter = booleanAdapter,
        ),
    )
