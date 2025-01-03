package com.example.alarmbuddy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.alarmbuddy.ui.AlarmBuddyApp
import com.example.alarmbuddy.ui.theme.AlarmBuddyTheme

class MainActivity : ComponentActivity() {



    fun startAlarmBuddy(){

//      Safety final permission check if something went wrong in the permission process
        if(!(Settings.System.canWrite(this) && Settings.canDrawOverlays(this) && allPermisionsGranted())) {

            setContent {
                AlarmBuddyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ManualPermissionUi(
                            Modifier.padding(innerPadding)
                        )
                    }
                }
            }

        }else {


            // Get intent from AlarmReceiver when it restarts the activity on alarm activation
            var navigateTo = intent?.getStringExtra("navigateTo") ?: ""
            var alarmId =
                intent.getIntExtra("id", -1)

//       If the App is launched we observe the alarmstate via the sharedpreferences
//        Since on launch multible launches and terminations of the the Intent is not reliable when opening the app normally
//        Therefore sharedPreferences persist through this
            if (navigateTo == "") {
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


//        Check write setting permission to override volume change
//        Note wrap this with a warning for the user before we let it pop up

//        CHeck all permissions on app start
//        Overwrite system settings for alarm volume
//        Camera Access
//        Notifications - see Notes



        if(Settings.System.canWrite(this) && Settings.canDrawOverlays(this) && allPermisionsGranted())
        {
            startAlarmBuddy()
        }else {


            setContent {
                AlarmBuddyTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        PermissionUi(
                            Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }




    }

    private fun requestPermissions(){
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermisionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){ permissions ->
            //HandlePermission Granted or rejected
            var permissionGranted = true
            permissions.entries.forEach{
                if(it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if(!permissionGranted){
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
                    setContent {
                        AlarmBuddyTheme {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                ManualPermissionUi(
                                    Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
            }else{
                    startAlarmBuddy()

            }

        }


    companion object {

        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                }
            }.toTypedArray()

    }

    fun permissionManager(){

        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            this.startActivity(intent)
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            this.startActivity(intent)

        }



        if(allPermisionsGranted()){
            startAlarmBuddy()
        }else{
            requestPermissions()
        }

//        if(!Settings.System.canWrite(this) || !Settings.canDrawOverlays(this) || !allPermisionsGranted()) {
//            Toast.makeText(this, "GIve all necessary permissions to continue", Toast.LENGTH_LONG).show()
//
//            permissionManager()
//        }

    }


    @Composable
    fun PermissionUi(
        modifier: Modifier = Modifier,
    ){
//        var canWrite by remember { mutableStateOf(Settings.System.canWrite(this)) }
//        var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(this)) }
//        var cameraAndNotifications by remember { mutableStateOf( REQUIRED_PERMISSIONS.all {
//            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//        }) }




        Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text="Please allow all following permissions, they are needed to properly run the app")
            Button(onClick = {permissionManager()} ) {
                Text(text = "Give Permissions")
            }
        }

    }

    @Composable
    fun ManualPermissionUi(
        modifier: Modifier = Modifier,
    ){

        Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text="It seems like some permissions have been denied, please go to app settings and enable ... \n Then restart the app")

        }

    }
}
