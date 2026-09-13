package com.example.alarmbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.platform.AlarmScheduler
import com.example.alarmbuddy.ui.theme.SecondaryColor
import org.jetbrains.compose.resources.painterResource
import alarmbuddy.composeapp.generated.resources.Res
import alarmbuddy.composeapp.generated.resources.barcode
import alarmbuddy.composeapp.generated.resources.math
import alarmbuddy.composeapp.generated.resources.memory
import alarmbuddy.composeapp.generated.resources.shake

// Home screen: lists all alarms, lets the user add a new one and toggle
// existing ones on/off.
@Composable
fun Home(
    viewModel: AlarmViewModel,
    alarmScheduler: AlarmScheduler,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onAddAlarm) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Alarm")
        }

        LazyColumn {
            itemsIndexed(state) { _, alarm ->
                AlarmItem(alarm, viewModel, alarmScheduler, onEditAlarm)
            }
        }
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    viewModel: AlarmViewModel,
    alarmScheduler: AlarmScheduler,
    onEditAlarm: (Long) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        style = MaterialTheme.typography.headlineMedium,
                        text = "${alarm.hour.toString().padStart(2, '0')}:${
                            alarm.minute.toString().padStart(2, '0')
                        }",
                        color = Color.Gray,
                    )
                }
                Row(horizontalArrangement = Arrangement.Start) {
                    Button(
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 6.dp, minHeight = 6.dp),
                        onClick = {
                            if (alarm.activated) alarmScheduler.cancel(alarm)
                            viewModel.deleteAlarm(alarm)
                        },
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    Button(
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 6.dp, minHeight = 6.dp),
                        onClick = { onEditAlarm(alarm.id) },
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            }

            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                Switch(
                    checked = alarm.activated,
                    onCheckedChange = {
                        if (!alarm.activated) {
                            alarmScheduler.schedule(alarm)
                            viewModel.updateAlarm(alarm.copy(activated = true))
                        } else {
                            alarmScheduler.cancel(alarm)
                            viewModel.updateAlarm(alarm.copy(activated = false))
                        }
                    },
                )

                Text(text = alarm.audioFile)
                Spacer(Modifier.width(6.dp))
                Row {
                    if (alarm.barcodeTask) {
                        Icon(
                            painter = painterResource(Res.drawable.barcode),
                            contentDescription = "Barcode task enabled",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(SecondaryColor)
                                .padding(4.dp),
                            tint = Color.Black,
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    if (alarm.shakeTask) {
                        Icon(
                            painter = painterResource(Res.drawable.shake),
                            contentDescription = "Shake task enabled",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(SecondaryColor)
                                .padding(4.dp),
                            tint = Color.Black,
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    if (alarm.memoryTask) {
                        Icon(
                            painter = painterResource(Res.drawable.memory),
                            contentDescription = "Memory task enabled",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(SecondaryColor)
                                .padding(4.dp),
                            tint = Color.Black,
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    if (alarm.mathTask) {
                        Icon(
                            painter = painterResource(Res.drawable.math),
                            contentDescription = "Math task enabled",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(SecondaryColor)
                                .padding(4.dp),
                            tint = Color.Black,
                        )
                    }
                }
            }
        }
    }
}
