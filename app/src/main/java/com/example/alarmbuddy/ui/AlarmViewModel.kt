package com.example.alarmbuddy.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel (private val repository: AlarmRepository):ViewModel()
{



    val alarmUIState = repository.Alarms.stateIn(
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


}