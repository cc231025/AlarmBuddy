package com.example.alarmbuddy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.alarmbuddy.ui.getAudioResource


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ALARM_ACTION") {

            val alarmId =
                intent.getIntExtra("id", -1)  // -1 is the default value if "id" is not found
            val alarmVolume = intent.getFloatExtra("volume", -1f)
            val audioFile = intent.getStringExtra("audioFile") ?: R.string.classic_alarm.toString()


            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigateTo", "Ringing")
                putExtra("id", alarmId)
            }

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtras(intent.extras ?: Bundle())
                putExtra("navigateTo", "Ringing")
                putExtra("id", alarmId)
            }

            context.startForegroundService(serviceIntent)


            // Optionally, show a notification
//            showNotification(context, alarmVolume, audioFile, launchIntent)

            context.startActivity(launchIntent)
        }
    }

//    private fun showNotification(context: Context, alarmVolume: Float, audioFile: String, launchIntent: Intent) {
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channelId = "alarm_channel"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Alarm Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
//
//
//        val notification = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
//            .setContentTitle("Alarm Triggered")
//            .setContentText("Your alarm is ringing!")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(pendingIntent)
//            .build()
//
//
////        Prepare Alarm Sound
////        val audioFileId = getAudioResource(audioFile)
////
////
////        val mediaPlayer = MediaPlayer.create(context, audioFileId ?: R.raw.classic_alarm)
////
////        mediaPlayer.apply {
////            isLooping = true
//////            start()
////            setVolume(alarmVolume, alarmVolume)
////        }
//
//
//
//        notificationManager.notify(1, notification)
////            .also{ mediaPlayer.start() }
//
//    }
}


class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        if (intent?.action == "STOP_ALARM") {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val audioFile = intent?.getStringExtra("audioFile") ?: "Classic Alarm"
        val alarmVolume = intent?.getFloatExtra("volume", 1.0f)
        val alarmId = intent?.getIntExtra("id", -1)  // -1 is the default value if "id" is not found
        val navigateTo = intent?.getStringExtra("navigateTo") ?: "Ringing"


        showNotification(alarmId, navigateTo)


        // Start alarm playback
        startAlarm(audioFile, alarmVolume)

        // Show a notification for the foreground service
//        showForegroundNotification()

        return START_NOT_STICKY
    }



    private fun showNotification(alarmId: Int?, navigateTo:String) {

        val channelId = "alarm_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", navigateTo)
            putExtra("id", alarmId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Your app's icon
            .setContentTitle("Alarm Triggered")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Attach the PendingIntent to handle clicks
            .build()




        startForeground(1, notification)

    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    private fun startAlarm(audioFile: String, volume: Float?) {
        val audioFileRes = getAudioResource(audioFile)
        mediaPlayer = MediaPlayer.create(this, audioFileRes ?: R.raw.classic_alarm).apply {
            isLooping = true
            setVolume(volume ?: 0.75f, volume ?: 0.75f)
            start()
        }
    }


    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}



