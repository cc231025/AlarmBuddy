package com.example.alarmbuddy

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.alarmbuddy.ui.AlarmBuddyApp
import com.example.alarmbuddy.ui.theme.AlarmBuddyTheme

class MainActivity : ComponentActivity() {









    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        Check write setting permission to override volume change
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            this.startActivity(intent)
        }


//        Get intent from AlarmReceiver when it restarts the activity on alarm activation
        var navigateTo = intent?.getStringExtra("navigateTo") ?: ""
        var alarmId =
            intent.getIntExtra("id", -1)

//       If the App is launched we observe the alarmstate via the sharedpreferences
//        Since on launch multible launches and terminations of the the Intent is not reliable when opening the app normally
//        Therefore sharedPreferences persist through this
        if (navigateTo == ""){
            val sharedPref = getSharedPreferences("AlarmState", Context.MODE_PRIVATE)
            navigateTo = sharedPref.getString("navigateTo", "") ?: ""
            alarmId = sharedPref.getInt("id", -1)
        }


        setContent {
            AlarmBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmBuddyApp(
                        Modifier.padding(innerPadding),
                        navigateTo = navigateTo,
                        alarmId = alarmId
                    )
                }
            }
        }
    }
}
