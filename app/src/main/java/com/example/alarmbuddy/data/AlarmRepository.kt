package com.example.alarmbuddy.data

import android.nfc.tech.NfcBarcode
import com.example.alarmbuddy.data.db.AlarmDao
import com.example.alarmbuddy.data.db.AlarmEntity
import kotlinx.coroutines.flow.map
import java.sql.Timestamp

class AlarmRepository(private val alarmDao: AlarmDao) {


    val Alarms = alarmDao.getALLAlarms().map { alarmList ->
        alarmList.map { entity ->
            Alarm(entity._id, entity.name, entity.time, entity.activated, entity.barcode, entity.barcodeTask, entity.shakeTask, entity.audioFile, entity.volume, entity.snoozes, entity.snoozeTime)
        }
    }


    suspend fun addNewAlarm(alarm:Alarm) {
        alarmDao.addAlarm(AlarmEntity(0, alarm.name,  alarm.time, alarm.barcode, alarm.activated, alarm.barcodeTask, alarm.shakeTask, alarm.audioFile, alarm.volume, alarm.snoozes, alarm.snoozeTime))
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }





}