package com.example.alarmbuddy.data

import androidx.room.TypeConverter
import com.example.alarmbuddy.data.db.AlarmEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Barcode (
    val id: Int = 0,
    val name: String = "MyBarcode",
    val barcode: String

)

