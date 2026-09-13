package com.example.alarmbuddy.data

import com.example.alarmbuddy.db.BarcodeEntity

data class Barcode(
    val id: Long = 0,
    val name: String,
    val barcode: String,
)

fun BarcodeEntity.toBarcode(): Barcode = Barcode(id = id, name = name, barcode = barcode)
