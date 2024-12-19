package com.example.alarmbuddy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.alarmbuddy.ui.getAudioResource


//My beautiful AlarmReceiver - this is where the magic happens

//AlarmReceiver listens to before scheduled alarms and acts appropriately given on the intent
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ALARM_ACTION") {

//          Get the alarm Id so we can pass it on to the startActivity function along with the navigateTo intent
            val alarmId =
                intent.getIntExtra("id", -1)  // -1 is the default value if "id" is not found

//          Create a launchintent which is sent to  the startactivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigateTo", "Ringing")
                putExtra("id", alarmId)
            }


//          Create a serviceIntent to start the foreground alarm service - add the previous intent extras
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtras(intent.extras ?: Bundle())

            }

//            Shared Preferences is used to check if an alarm is currently ringing, since intents arent always reliable in certain scenarios
            val sharedPref = context.getSharedPreferences("AlarmState", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("navigateTo", "Ringing")
            editor.putInt("id", alarmId)
            editor.apply()


//            Start Alarmservice as foreground
            context.startForegroundService(serviceIntent)

//          Just relaunch the App immediately with the launchintent
            context.startActivity(launchIntent)
        }
    }

}


//The AlarmService is a foreground service that manages the alarm sound and notification
class AlarmService : Service() {

    private var volumeChangeReceiver: BroadcastReceiver? = null
    private var mediaPlayer: MediaPlayer? = null


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//      If intent sent by my composables has this action stop the alarm and cancel service
        if (intent?.action == "STOP_ALARM") {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val audioFile = intent?.getStringExtra("audioFile") ?: "Classic Alarm"
        val alarmVolume = intent?.getFloatExtra("volume", 1.0f)
        val alarmId = intent?.getIntExtra("id", -1)  // -1 is the default value if "id" is not found
        val navigateTo = intent?.getStringExtra("navigateTo") ?: "Ringing"

//      Start Notification
        showNotification(alarmId, navigateTo)

        // Start alarm playback
        startAlarm(audioFile, alarmVolume)

//      Start the volumechange broadcastreceiver
        registerVolumeChangeReceiver()


        return START_NOT_STICKY
    }


    //    This extra receiver checks if the user tries to change the alarm volume and resets it to max - max annoyance level achieved
    private fun registerVolumeChangeReceiver() {

//      Manage the whole audio of the system on the STREAM_ALARM channel
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

//      If the received action is to change volume, permit it and change it to max
        volumeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                if (action == "android.media.VOLUME_CHANGED_ACTION") {
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                    if (currentVolume != maxVolume) {
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_ALARM,
                            maxVolume,
                            AudioManager.FLAG_SHOW_UI
                        )
                        AudioManager.FLAG_PLAY_SOUND
                    }
                }
            }
        }

        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeChangeReceiver, filter)
    }


    private fun showNotification(alarmId: Int?, navigateTo: String) {

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

//      Create a launch and pending intent to open the app when the notification is tapped
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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

    //    make sure to stop the alarm on destroy and disconnect the volumechangereceiver
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        volumeChangeReceiver?.let { unregisterReceiver(it) }

    }


    private fun startAlarm(audioFile: String, volume: Float?) {
        val audioFileRes = getAudioResource(audioFile)
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        audioManager.mode = AudioManager.MODE_NORMAL
//        Set stream volume to max always - this does not affect the volume set by the user
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        mediaPlayer = MediaPlayer()

        mediaPlayer!!.setDataSource(
            this,
            Uri.parse("android.resource://com.example.alarmbuddy/" + audioFileRes)
        )

//        AudioAttributes allow us set the usage which is needed for the alarm to actually use the alarm volume/not the media volume
        mediaPlayer!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // Or CONTENT_TYPE_MUSIC
                .build()
        )
        mediaPlayer!!.isLooping = true
        mediaPlayer!!.setVolume(volume ?: 0.75f, volume ?: 0.75f) // Overrides internal volume only

        mediaPlayer!!.prepare()

        mediaPlayer!!.start()


    }

//    Release the alarm and reset mediaplyer to make sure
    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}



