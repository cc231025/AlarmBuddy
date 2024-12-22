package com.example.alarmbuddy.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert
    suspend fun addAlarm(alarmEntity: AlarmEntity)

    @Update
    suspend fun updateAlarm(alarmEntity: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarmEntity: AlarmEntity)

    @Query("SELECT * FROM Alarms")
    fun getALLAlarms(): Flow<List<AlarmEntity>>
    
}

