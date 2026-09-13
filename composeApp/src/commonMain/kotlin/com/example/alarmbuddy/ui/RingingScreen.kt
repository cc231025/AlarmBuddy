package com.example.alarmbuddy.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarmbuddy.AppContainer
import com.example.alarmbuddy.data.soundFileNameFor
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import alarmbuddy.composeapp.generated.resources.Res
import alarmbuddy.composeapp.generated.resources.rise

// Shown when the app is opened from an alarm notification. Walks through
// whichever tasks the alarm has enabled (shake/math/memory/barcode, in that
// order) before letting the user actually stop the alarm.
@Composable
fun Ringing(
    alarmId: Long,
    viewModel: AlarmViewModel,
    appContainer: AppContainer,
    onFinished: () -> Unit,
) {
    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()
    val alarm = state.find { it.id == alarmId }

    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No alarm found for ID: $alarmId", fontSize = 18.sp, color = Color.Gray)
        }
        return
    }

    LaunchedEffect(alarm.id) {
        viewModel.updateAlarm(alarm.copy(activated = false))
        appContainer.alarmSoundPlayer.play(soundFileNameFor(alarm.audioFile), alarm.volume)
        // Persisted (not just in-memory) so that a force-quit-and-reopen during
        // an active alarm lands back on this Ringing/task-gate screen instead
        // of a normal Home screen -- see AppEntryPoint's startup check. This is
        // the one escape path that's actually preventable on iOS; the app being
        // silenced by a force-quit itself is not (see MIGRATION_PLAN.md).
        appContainer.appSettings.putString("navigateTo", "Ringing")
        appContainer.appSettings.putInt("ringingAlarmId", alarm.id.toInt())
    }

    var currentTask by remember { mutableIntStateOf(0) }
    var snoozeState by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableIntStateOf(10) }

    LaunchedEffect(snoozeState) {
        if (snoozeState) {
            appContainer.alarmSoundPlayer.pause()
            remainingTime = 10
            while (remainingTime > 0) {
                delay(1000)
                remainingTime -= 1
            }
            appContainer.alarmSoundPlayer.resume()
            snoozeState = false
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            if (!snoozeState) {
                Button(onClick = { snoozeState = true }) {
                    Text("Don't wake my Spouse")
                }
            } else {
                Text(text = remainingTime.toString())
            }
        }

        when (currentTask) {
            0 -> if (alarm.shakeTask) ShakeTask(onShakeComplete = { currentTask++ }) else currentTask++

            1 -> if (alarm.mathTask) MathTask(onMathComplete = { currentTask++ }) else currentTask++

            2 -> if (alarm.memoryTask) MemoryTask(onMemoryComplete = { currentTask++ }) else currentTask++

            3 -> {
                if (alarm.barcodeTask) {
                    Box(Modifier.fillMaxSize()) {
                        CameraScreen(
                            mode = "confirmBarcode",
                            viewModel = viewModel,
                            barcodeToConfirm = alarm.barcode,
                            onBarcodeConfirmed = { currentTask++ },
                        )
                        Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "Scan the Barcode ", fontSize = 24.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(10.dp))
                            Text(text = alarm.barcodeName, color = Color.Red, fontSize = 30.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(text = " to stop the alarm", fontSize = 24.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else currentTask++
            }

            4 -> {
                FinishScreen(onStopAlarm = {
                    appContainer.alarmSoundPlayer.stop()
                    appContainer.appSettings.clear("navigateTo")
                    appContainer.appSettings.clear("ringingAlarmId")
                    onFinished()
                })
            }
        }
    }
}

@Composable
fun FinishScreen(onStopAlarm: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            modifier = Modifier
                .clip(CircleShape)
                .padding(horizontal = 50.dp),
            painter = painterResource(Res.drawable.rise),
            contentDescription = "Rising sun",
            contentScale = ContentScale.FillWidth,
        )
        Text(
            textAlign = TextAlign.Center,
            text = "Good Morning!\nTime to get up!",
            fontSize = 30.sp,
            lineHeight = 50.sp,
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            onClick = onStopAlarm,
        ) {
            Text(text = "Stop Alarm")
        }
    }
}
