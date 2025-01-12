package com.example.alarmbuddy.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.alarmbuddy.AlarmReceiver
import com.example.alarmbuddy.AlarmService
import com.example.alarmbuddy.R
import com.example.alarmbuddy.data.Alarm
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay
import java.util.Calendar





// Schedule an exact Alarm with the AlarmManger Android Inbuilt
// The alarmmanager will run in the background and trigger the Alarmreceiver, which handle the Sound and set intents for the mainactivity...
fun scheduleExactAlarm(context: Context, alarm: Alarm) {

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

//    Create Intent that tells the AlarmReceiver the details about the alarm

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.ALARM_ACTION"
        putExtra("id", alarm.id)
        putExtra("volume", alarm.volume)
        putExtra("audioFile", alarm.audioFile)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

//    Exact Alarm will take more battery and processing in the background, but it is very much necessary for my usecase
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
    )
}


fun cancelAlarm(context: Context, alarm: Alarm) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.ALARM_ACTION"
        putExtra("id", alarm.id)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)

}


// If the App is started while an alarm is ringing the user will be navigated here

@Composable
fun Ringing(
    alarmId: Int, viewModel: AlarmViewModel, context: Context, navController: NavController
) {

    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()

    val alarm: Alarm? = state.find { it.id == alarmId }

    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No alarm found for ID: $alarmId", fontSize = 18.sp, color = Color.Gray)
        }
        return
    }


    viewModel.updateAlarm(alarm.copy(activated = false))



    var currentTask by remember { mutableIntStateOf(0) }

    var snoozeState by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableIntStateOf(10) }

//    Snoozestate to display time left until the alarm resumes

    LaunchedEffect(snoozeState) {
        if (snoozeState) {
            remainingTime = 10
            while (remainingTime > 0) {
                delay(1000)
                remainingTime -= 1
            }
            snoozeState = false
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center
        ) {

//            Persistent SnoozeButton over all tasks
            if (!snoozeState) {

                Button(onClick = {
                    snoozeState = true
                    dontWakeMySpouseButton(context)
                }) {
                    Text("Don't wake my Spouse")
                }
            } else {
                Text(text = remainingTime.toString())
            }


        }

//        Logic to inject composables based on which tasks were activated in order
//        Final Screen wil require the user to press a final button to disable the alarm
        when (currentTask) {

            0 -> {
                if (alarm.shakeTask) {

                    val sensorManager: SensorManager =
                        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

                    ShakeTask(
                        sensorManager,
                        onShakeComplete = {
                            currentTask++
                        })
                } else currentTask++
            }

            1 -> {
                if (alarm.mathTask) {
                    MathTask(onMathComplete = { currentTask++ })

                } else currentTask++

            }

            2 -> {
                if (alarm.memoryTask) {
                    MemoryTask(onMemoryComplete = { currentTask++ })

                } else currentTask++

            }

            3 -> {
                if (alarm.barcodeTask) {
                    Row(Modifier.fillMaxSize(0.80f)) {
                        Camera(context,
                            "confirmBarcode",
                            navController,
                            viewModel,
                            barcodeToConfirm = alarm.barcode,
                            barcodeConfirmed = {
//                                stopAlarmService(context)
                                currentTask++
                            })
                    }
                    Column(
                        Modifier
                            .fillMaxSize(),
//                            .align(Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scan the Barcode ",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = alarm.barcodeName,
                            color = Color.Red,
                            fontSize = 30.sp,
                        )
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = " to stop the alarm",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else currentTask++

            }

            4 -> {

                FinishScreen(context, navController)
            }

        }
    }

}

// FInal Screen where user can deactivate the Alarm
@Composable
fun FinishScreen(
    context: Context,
    navController: NavController
) {


    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {


        Image(
            modifier = Modifier
                .clip(CircleShape)
                .padding(horizontal = 50.dp),
            painter = rememberDrawablePainter(
                drawable = getDrawable(
                    LocalContext.current,
                    R.drawable.rise
                )
            ),
            contentDescription = "Rising Sun Gif",
            contentScale = ContentScale.FillWidth,
        )
        Text(
            textAlign = TextAlign.Center,
            text = "Good Morning!\nTime to get up!",
            fontSize = 30.sp,
            lineHeight = 50.sp
        )
        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
            onClick = {
                stopAlarmService(context)
                navController.navigate("Home")

            }) {
            Text(text = "Stop Alarm")
        }


    }


}


//Send PauseandResume intent to the receiver to time out the alarm for 10 seconds
fun dontWakeMySpouseButton(context: Context) {
    val intent = Intent(context, AlarmService::class.java).apply {
        action = AlarmService.ACTION_PAUSE_RESUME
    }
    context.startService(intent)
}



// To stop the Alarm completely send a stopIntent to the receiver which will trigger onDestroy/stopAlarm() ...
//Also Clear Shared preferences
fun stopAlarmService(context: Context) {


    val sharedPref = context.getSharedPreferences("AlarmState", Context.MODE_PRIVATE)
    sharedPref.edit().clear().apply()


    val stopIntent = Intent(context, AlarmService::class.java).apply {
        action = "STOP_ALARM"
    }

    context.startService(stopIntent)
}
