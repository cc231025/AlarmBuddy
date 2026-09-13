package com.example.alarmbuddy.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.alarmbuddy.db.AlarmQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Repository to handle communication between viewmodel and the SQLDelight queries
// (this mirrors the original AlarmRepository that wrapped Room's AlarmDao).
class AlarmRepository(
    private val queries: AlarmQueries,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    val alarms: Flow<List<Alarm>> =
        queries.selectAllAlarms()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities -> entities.map { it.toAlarm() } }

    suspend fun addNewAlarm(alarm: Alarm) {
        queries.insertAlarm(
            name = alarm.name,
            hour = alarm.hour.toLong(),
            minute = alarm.minute.toLong(),
            activated = alarm.activated,
            barcode = alarm.barcode,
            barcodeName = alarm.barcodeName,
            barcodeTask = alarm.barcodeTask,
            shakeTask = alarm.shakeTask,
            mathTask = alarm.mathTask,
            memoryTask = alarm.memoryTask,
            audioFile = alarm.audioFile,
            volume = alarm.volume.toDouble(),
            snoozes = alarm.snoozes.toLong(),
            snoozeTime = alarm.snoozeTime.toLong(),
        )
    }

    suspend fun updateAlarm(alarm: Alarm) {
        queries.updateAlarm(
            id = alarm.id,
            name = alarm.name,
            hour = alarm.hour.toLong(),
            minute = alarm.minute.toLong(),
            activated = alarm.activated,
            barcode = alarm.barcode,
            barcodeName = alarm.barcodeName,
            barcodeTask = alarm.barcodeTask,
            shakeTask = alarm.shakeTask,
            mathTask = alarm.mathTask,
            memoryTask = alarm.memoryTask,
            audioFile = alarm.audioFile,
            volume = alarm.volume.toDouble(),
            snoozes = alarm.snoozes.toLong(),
            snoozeTime = alarm.snoozeTime.toLong(),
        )
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        queries.deleteAlarm(alarm.id)
    }
}

class BarcodeRepository(
    private val queries: com.example.alarmbuddy.db.BarcodeQueries,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    val barcodes: Flow<List<Barcode>> =
        queries.selectAllBarcodes()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { entities -> entities.map { it.toBarcode() } }

    suspend fun addBarcode(barcode: Barcode) {
        queries.insertBarcode(name = barcode.name, barcode = barcode.barcode)
    }

    suspend fun deleteBarcode(barcode: Barcode) {
        queries.deleteBarcode(barcode.id)
    }
}
