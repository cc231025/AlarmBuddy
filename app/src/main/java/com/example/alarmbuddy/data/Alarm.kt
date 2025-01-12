package com.example.alarmbuddy.data

import androidx.room.TypeConverter
import com.example.alarmbuddy.data.db.AlarmEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter


//Here we define the attributes of my Alarm class with integrate conversion function to Entity

data class Alarm (
    val id: Int = 0,
    val name: String = "Classic_Alarm",
    var time: LocalTime,
    val activated: Boolean = false,
    val barcode: String = "12345",
    val barcodeName: String = "No Barcode Selected",
    var barcodeTask: Boolean = false,
    var shakeTask: Boolean = false,
    var mathTask: Boolean = false,
    var memoryTask: Boolean = false,
    var audioFile: String,
    var volume: Float = 0.5f,
    val snoozes: Int = 3,
    val snoozeTime: Int = 300


)

fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        _id = this.id,
        name = this.name,
        time = this.time,
        activated = this.activated,
        barcode = this.barcode,
        barcodeName = this.barcodeName,
        barcodeTask = this.barcodeTask,
        shakeTask = this.shakeTask,
        mathTask = this.mathTask,
        memoryTask = this.memoryTask,
        audioFile = this.audioFile,
        volume = this.volume,
        snoozes = this.snoozes,
        snoozeTime = this.snoozeTime,

    )
}





class Converters {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(timeFormatter) // Convert LocalTime to String
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) } // Convert String to LocalTime
    }
}