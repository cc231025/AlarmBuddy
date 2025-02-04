package com.example.alarmbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.AlarmRepository
import com.example.alarmbuddy.data.Barcode
import com.example.alarmbuddy.data.db.BarcodeDao
import com.example.alarmbuddy.data.db.BarcodeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


//I use the Alarm Viewmodel not only for the Alarms but also to manage the Barcode Dao,
// as a second Viewmodel would be overkill for this project and more confusing than helpful
class AlarmViewModel (private val repository: AlarmRepository, private val barcodeDao: BarcodeDao):ViewModel()
{

        val alarmUIState = repository.Alarms.map { alarmList -> alarmList.sortedBy { it.time } }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )



    fun addAlarm(alarm : Alarm){
        viewModelScope.launch {
            repository.addNewAlarm(alarm)

        }
    }

    fun deleteAlarm(alarm : Alarm){
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun updateAlarm(alarm:Alarm){
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }


    val barcodeUIState = barcodeDao.getALLBarcodes()
        .map { barcodeList ->
            barcodeList.map { entity ->
                Barcode(id = entity._id, name = entity.name, barcode = entity.barcode)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )


    fun addBarcode(barcode: Barcode) {
        viewModelScope.launch {
            barcodeDao.addBarcode(
                BarcodeEntity(
                    _id = barcode.id,
                    name = barcode.name,
                    barcode = barcode.barcode
                )
            )

        }
    }

    fun deleteBarcode(barcode: Barcode) {
        viewModelScope.launch {
            barcodeDao.deleteBarcode(
                BarcodeEntity(
                    _id = barcode.id,
                    name = barcode.name,
                    barcode = barcode.barcode
                )
            )
        }
    }


}