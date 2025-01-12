package com.example.alarmbuddy.data

import com.example.alarmbuddy.data.db.AlarmDao
import com.example.alarmbuddy.data.db.AlarmEntity
import com.example.alarmbuddy.data.db.toAlarm
import kotlinx.coroutines.flow.map

// Repository to handle communication between viewmodel and Dao
// Not really necessary for the scope of my project, but still for good measure
class AlarmRepository(private val alarmDao: AlarmDao) {


    val Alarms = alarmDao.getALLAlarms().map { alarmList ->
        alarmList.map { entity ->
            entity.toAlarm()
        }
    }

    suspend fun addNewAlarm(alarm:Alarm) {
            alarmDao.addAlarm(alarm.toEntity())

    }

    suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }

    suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }
}