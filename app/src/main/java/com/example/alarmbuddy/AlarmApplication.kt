package com.example.alarmbuddy

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarmbuddy.data.AlarmRepository
import com.example.alarmbuddy.data.db.AlarmDatabase
import com.example.alarmbuddy.ui.AlarmViewModel

class AlarmApplication : Application() {



    val alarmRepository by lazy {
        val alarmDao = AlarmDatabase.getDatabase(this).AlarmDao()
        AlarmRepository(alarmDao)
    }

    val alarmViewModelFactory by lazy {
        AlarmViewModelFactory(alarmRepository)
    }
}

class AlarmViewModelFactory(
    private val repository: AlarmRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}