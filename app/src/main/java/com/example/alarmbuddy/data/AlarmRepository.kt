package com.example.alarmbuddy.data

import com.example.alarmbuddy.data.db.AlarmDao
import com.example.alarmbuddy.data.db.AlarmEntity
import com.example.alarmbuddy.data.db.toAlarm
import kotlinx.coroutines.flow.map

class AlarmRepository(private val alarmDao: AlarmDao) {


    val Alarms = alarmDao.getALLAlarms().map { alarmList ->
        alarmList.map { entity ->
//            Alarm(entity._id, entity.name, entity.time, entity.activated, entity.barcode, entity.barcodeTask, entity.shakeTask, entity.mathTask, entity.memoryTask, entity.audioFile, entity.volume, entity.snoozes, entity.snoozeTime)
            entity.toAlarm()
        }
    }


    suspend fun addNewAlarm(alarm:Alarm) {
//        alarmDao.addAlarm(AlarmEntity(0, alarm.name,  alarm.time, alarm.barcode, alarm.activated, alarm.barcodeTask, alarm.shakeTask, alarm.audioFile, alarm.volume, alarm.snoozes, alarm.snoozeTime))
            alarmDao.addAlarm(alarm.toEntity())

    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }






}