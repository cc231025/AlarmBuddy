package com.example.alarmbuddy.data

import com.example.alarmbuddy.db.AlarmEntity

// Same shape as the original Android Alarm data class, with one deliberate change:
// `time: java.time.LocalTime` became separate `hour`/`minute` Ints. java.time isn't
// available in commonMain/iosMain, and kotlinx-datetime has no plain "time of day"
// type, so plain Ints are the simplest thing that works on every target.
data class Alarm(
    val id: Long = 0,
    val name: String = "Classic_Alarm",
    val hour: Int,
    val minute: Int,
    val activated: Boolean = false,
    val barcode: String = "12345",
    val barcodeName: String = "No Barcode Selected",
    val barcodeTask: Boolean = false,
    val shakeTask: Boolean = false,
    val mathTask: Boolean = false,
    val memoryTask: Boolean = false,
    val audioFile: String,
    val volume: Float = 0.5f,
    val snoozes: Int = 3,
    val snoozeTime: Int = 300,
)

fun AlarmEntity.toAlarm(): Alarm = Alarm(
    id = id,
    name = name,
    hour = hour.toInt(),
    minute = minute.toInt(),
    activated = activated,
    barcode = barcode,
    barcodeName = barcodeName,
    barcodeTask = barcodeTask,
    shakeTask = shakeTask,
    mathTask = mathTask,
    memoryTask = memoryTask,
    audioFile = audioFile,
    volume = volume.toFloat(),
    snoozes = snoozes.toInt(),
    snoozeTime = snoozeTime.toInt(),
)
