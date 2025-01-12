package com.example.alarmbuddy.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDao {

    @Insert
    suspend fun addBarcode(barcodeEntity: BarcodeEntity)

    @Delete
    suspend fun deleteBarcode(barcodeEntity: BarcodeEntity)

    @Query("SELECT * FROM Barcodes")
    fun getALLBarcodes(): Flow<List<BarcodeEntity>>

}

