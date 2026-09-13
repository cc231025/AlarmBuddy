package com.example.alarmbuddy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.AlarmRepository
import com.example.alarmbuddy.data.Barcode
import com.example.alarmbuddy.data.BarcodeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Same shape as the original Android AlarmViewModel: manages both alarms and
// barcodes since a second ViewModel would be overkill for an app this size.
class AlarmViewModel(
    private val alarmRepository: AlarmRepository,
    private val barcodeRepository: BarcodeRepository,
) : ViewModel() {

    val alarmUIState = alarmRepository.alarms
        .map { alarmList -> alarmList.sortedBy { it.hour * 60 + it.minute } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList(),
        )

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch { alarmRepository.addNewAlarm(alarm) }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch { alarmRepository.deleteAlarm(alarm) }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch { alarmRepository.updateAlarm(alarm) }
    }

    val barcodeUIState = barcodeRepository.barcodes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList(),
    )

    fun addBarcode(barcode: Barcode) {
        viewModelScope.launch { barcodeRepository.addBarcode(barcode) }
    }

    fun deleteBarcode(barcode: Barcode) {
        viewModelScope.launch { barcodeRepository.deleteBarcode(barcode) }
    }
}
