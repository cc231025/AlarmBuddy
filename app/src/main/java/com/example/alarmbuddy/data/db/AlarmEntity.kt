package com.example.alarmbuddy.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alarmbuddy.data.Alarm
import java.time.LocalTime

@Entity(tableName = "Alarms") // Specify table name
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val name: String = "Classic_Alarm",
    var time: LocalTime,
    val barcode: String = "12345",
    val barcodeName: String = "No Barcode Selected",
    @ColumnInfo("activated")
    val activated: Boolean = false,
    @ColumnInfo("barcodeTask")
    var barcodeTask: Boolean = false,
    @ColumnInfo("shakeTask")
    var shakeTask: Boolean = false,
    var mathTask: Boolean = false,
    var memoryTask: Boolean = false,
    var audioFile: String,
    var volume: Float = 0.5f,
    val snoozes: Int = 3,
    val snoozeTime: Int = 300

)


fun AlarmEntity.toAlarm(): Alarm {
    return Alarm(
        id = this._id,
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