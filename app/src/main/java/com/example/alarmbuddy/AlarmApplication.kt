package com.example.alarmbuddy

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarmbuddy.data.AlarmRepository
import com.example.alarmbuddy.data.db.AlarmDatabase
import com.example.alarmbuddy.data.db.BarcodeDao
import com.example.alarmbuddy.data.db.BarcodeDatabase
import com.example.alarmbuddy.ui.AlarmViewModel


//Initialize Daos/Repository for barcode/alarm database + Factory to access them in my composables
class AlarmApplication : Application() {


    val barcodeDao by lazy {
        BarcodeDatabase.getDatabase(this).BarcodeDao()
    }

    val alarmRepository by lazy {
        val alarmDao = AlarmDatabase.getDatabase(this).AlarmDao()
        AlarmRepository(alarmDao)
    }

    val alarmViewModelFactory by lazy {
        AlarmViewModelFactory(alarmRepository, barcodeDao)
    }
}

class AlarmViewModelFactory(
    private val repository: AlarmRepository,
    private val barcodeDao: BarcodeDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(repository, barcodeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

