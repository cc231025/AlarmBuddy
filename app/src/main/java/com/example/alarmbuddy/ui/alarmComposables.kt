package com.example.alarmbuddy.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarmbuddy.AlarmReceiver
import com.example.alarmbuddy.AlarmService
import com.example.alarmbuddy.R
import com.example.alarmbuddy.data.Alarm
import java.util.Calendar

fun scheduleExactAlarm(context: Context, alarm: Alarm) {

//    if (!Settings.canDrawOverlays(context)) {
//        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
//            data = Uri.parse("package:${context.packageName}")
//        }
////        context.startActivity(intent)
//    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    Log.d("AlarmApp", "Alarm time: ${alarm.time.hour} : ${alarm.time.minute}")


    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, alarm.time.hour)
        set(Calendar.MINUTE, alarm.time.minute)
        set(Calendar.SECOND, 0)

    }

    if (calendar.timeInMillis < System.currentTimeMillis()) {
        // If the alarm time has already passed today, set it for the next day
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

//    Send the alarm object in the intent?

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.ALARM_ACTION"
        putExtra("id", alarm.id)
        putExtra("volume", alarm.volume)
        putExtra("audioFile", alarm.audioFile)
    }

//    intent.putExtra("alarm", alarm)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarm.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    Log.d("AlarmApp", "Alarm set for: ${calendar.time}")

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )


    val nextAlarm = alarmManager.nextAlarmClock
    Log.d("AlarmManager", "Next alarm time: ${nextAlarm?.triggerTime}")


}


fun cancelAlarm(context: Context, alarm: Alarm) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.ALARM_ACTION"
        putExtra("id", alarm.id)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarm.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)

}


fun checkAndRequestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!alarmManager.canScheduleExactAlarms()) {
            // Guide the user to the settings page to allow exact alarms
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        }
    } else {
        // For devices below API 31, exact alarms are allowed by default
        // No action needed
    }
}


val alarmSounds = mapOf(
    "Classic Alarm" to R.raw.classic_alarm,
    "Pain Alarm" to R.raw.pain_alarm,
    "Granular Alarm" to R.raw.granular_alarm
)

fun getAudioResource(audioFileName: String): Int? {
    return alarmSounds[audioFileName] // Returns null if not found
}


@Composable
fun Ringing(alarmId: Int, viewModel: AlarmViewModel, context: Context) {

    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()

    val alarm: Alarm? = state.find { it.id == alarmId }


//    Handle nullpointers here if alarm is 0 - maybe navigate back to home or another error message would be good
    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No alarm found for ID: $alarmId", fontSize = 18.sp, color = Color.Gray)
        }
        return
    }

//        val audioFileId = getAudioResource(alarm.audioFile)
//
//
//        val mediaPlayer =
//            remember { MediaPlayer.create(context, audioFileId ?: R.raw.classic_alarm) }
//
//
//        // Ensure MediaPlayer is set to loop and start it
//        LaunchedEffect(mediaPlayer) {
//            mediaPlayer.apply {
//                isLooping = true
//                start()
//                setVolume(alarm.volume, alarm.volume)
//            }
//        }




        viewModel.updateAlarm(alarm.copy(activated = false))

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            Button(
//                Note create some stopchecks here - since we cant stop the instance twice - might crash the app
                onClick = {
                    stopAlarmService(context)
//                    mediaPlayer.apply {
//                        stop()
//                        release()
//                    }
                }) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Stop Alarm")
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Alarm Triggered! Alarm Id is ${alarmId}", fontSize = 24.sp)
                Text(
                    "Alarm Triggered! Alarm Time is ${alarm.time.hour} : ${alarm.time.minute}",
                    fontSize = 24.sp
                )

            }
        }

}


fun stopAlarmService(context: Context) {
    val stopIntent = Intent(context, AlarmService::class.java).apply {
        action = "STOP_ALARM"
    }
    context.startService(stopIntent)
}
