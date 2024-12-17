package com.example.alarmbuddy.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import com.example.alarmbuddy.AlarmReceiver
import com.example.alarmbuddy.data.Alarm
import java.util.Calendar

fun scheduleExactAlarm(context: Context, alarm: Alarm){

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar = Calendar.getInstance().apply {
        set (Calendar.HOUR_OF_DAY, alarm.time.hour)
        set (Calendar.MINUTE, alarm.time.minute)
        set(Calendar.SECOND, 0)

    }

//    Send the alarm object in the intent?

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.ALARM_ACTION"
    }

//    intent.putExtra("alarm", alarm)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    Log.d("AlarmApp", "Alarm set for: ${calendar.time}")

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent)


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