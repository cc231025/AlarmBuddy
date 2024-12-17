package com.example.alarmbuddy.data.db

import androidx.compose.runtime.Composable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.toEntity
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalTime

@Entity(tableName = "Alarms") // Specify table name
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val name: String = "myAlarm",
    val time: LocalTime,
    val barcode: String,
    @ColumnInfo("activated")
    val activated: Boolean = false,
   @ColumnInfo("barcodeTask")
    val barcodeTask: Boolean = false,
    @ColumnInfo("shakeTask")
    val shakeTask: Boolean = false,
    val audioFile: String,
    val volume: Int = 50,
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
        barcodeTask = this.barcodeTask,
        shakeTask = this.shakeTask,
        audioFile = this.audioFile,
        volume = this.volume,
        snoozes = this.snoozes,
        snoozeTime = this.snoozeTime,

        )
}