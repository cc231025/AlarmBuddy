package com.example.alarmbuddy.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alarmbuddy.data.Alarm
import java.time.LocalTime

@Entity(tableName = "Barcodes") // Specify table name
data class BarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val name: String = "MyBarcode",
    val barcode: String


)
