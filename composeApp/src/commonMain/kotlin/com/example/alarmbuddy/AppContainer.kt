package com.example.alarmbuddy

import com.example.alarmbuddy.data.AlarmRepository
import com.example.alarmbuddy.data.BarcodeRepository
import com.example.alarmbuddy.platform.AlarmScheduler
import com.example.alarmbuddy.platform.AlarmSoundPlayer
import com.example.alarmbuddy.platform.AppSettings
import com.example.alarmbuddy.platform.DatabaseDriverFactory
import com.example.alarmbuddy.platform.createDatabase

// Replaces the Android AlarmApplication class: wires up the database,
// repositories, and the platform services every screen needs. Constructed
// once at process start on each platform (MainViewController.kt on iOS,
// main.kt on the desktop dev target) and threaded down through App().
class AppContainer(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = createDatabase(databaseDriverFactory)

    val alarmRepository = AlarmRepository(database.alarmQueries)
    val barcodeRepository = BarcodeRepository(database.barcodeQueries)

    val alarmScheduler = AlarmScheduler()
    val alarmSoundPlayer = AlarmSoundPlayer()
    val appSettings = AppSettings()
}
