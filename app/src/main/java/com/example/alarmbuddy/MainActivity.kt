package com.example.alarmbuddy

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
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

//    private var notificationMediaPlayer: MediaPlayer? = null
//


//    private val cameraPermissionRequest =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                // Implement camera related  code
//            } else {
//                // Camera permission denied
//            }
//
//        }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        notificationManager.cancelAll();


//        when (PackageManager.PERMISSION_GRANTED) {
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.CAMERA
//            ) -> {
//                // Camera permission already granted
//                // Implement camera related code


        val navigateTo = intent?.getStringExtra("navigateTo") ?: ""
        val alarmId =
            intent.getIntExtra("id", -1)  // -1 is the default value if "id" is not found

        setContent {
            AlarmBuddyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmBuddyApp(
                        Modifier.padding(innerPadding),
                        navigateTo = navigateTo,
                        alarmId = alarmId// Pass the navigation info to the app
                    )
                }
            }
        }
//            }
//
//            else -> {
//                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
//            }


    }
}
