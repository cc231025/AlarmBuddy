package com.example.alarmbuddy.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.alarmbuddy.db.AlarmBuddyDatabase

// Desktop-only: used for local `./gradlew :composeApp:run` smoke testing, never shipped.
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:alarmbuddy-desktop.db")
        AlarmBuddyDatabase.Schema.create(driver)
        return driver
    }
}
