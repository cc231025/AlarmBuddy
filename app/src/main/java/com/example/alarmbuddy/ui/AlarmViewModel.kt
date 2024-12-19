package com.example.alarmbuddy.ui

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmbuddy.R
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel (private val repository: AlarmRepository):ViewModel()
{

//    private var alarmPlayer: MediaPlayer? = null
//
//    fun initAlarmPlayer(context: Context, audioFile: String, volume: Float, fileName: Uri){
//
//        if(alarmPlayer == null){
//            val audioFileId = getAudioResource(audioFile)
//            alarmPlayer = MediaPlayer.create(context, audioFileId ?: R.raw.classic_alarm)
//            alarmPlayer = MediaPlayer().apply {
//                setDataSource(context, fileName)
//                isLooping = true
//                setVolume(volume, volume)
//                prepare()
//
//            }
//        }
//
//    }
//
//    fun play() {
//        alarmPlayer?.start()
//    }
//
//    fun pause() {
//        alarmPlayer?.pause()
//    }
//
//    fun stop() {
//        alarmPlayer?.stop()
//        alarmPlayer?.release()
//        alarmPlayer = null
//    }





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