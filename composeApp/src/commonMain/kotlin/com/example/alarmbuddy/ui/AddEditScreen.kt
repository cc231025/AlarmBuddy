package com.example.alarmbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.Barcode
import com.example.alarmbuddy.data.alarmSounds
import com.example.alarmbuddy.platform.AlarmScheduler
import com.example.alarmbuddy.ui.theme.BackgroundColor
import com.example.alarmbuddy.ui.theme.HighlightColor

private val alarmSoundDisplayNames: List<String> = alarmSounds.keys.toList()

// Shared editor UI used by both Add (new alarm) and Edit (existing alarm).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditor(
    initialAlarm: Alarm,
    viewModel: AlarmViewModel,
    onCancel: () -> Unit,
    onSave: (Alarm) -> Unit,
    onOpenCamera: () -> Unit,
    saveButtonLabel: String,
) {
    var dropDownExpanded by remember { mutableStateOf(false) }
    var alarm by remember { mutableStateOf(initialAlarm) }
    var showBarcodePopup by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = true,
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 66.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Button(colors = MainButtonColors(), onClick = onCancel) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Cancel")
                }
            }

            Spacer(Modifier.height(20.dp))
            TimePicker(state = timePickerState)

            OutlinedCard(
                Modifier
                    .fillMaxWidth()
                    .clickable { dropDownExpanded = !dropDownExpanded }
                    .padding(0.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = alarm.audioFile)
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = HighlightColor,
                    )
                }
                DropdownMenu(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false },
                ) {
                    alarmSoundDisplayNames.forEach { sound ->
                        DropdownMenuItem(
                            text = { Text(text = sound) },
                            onClick = {
                                alarm = alarm.copy(audioFile = sound)
                                dropDownExpanded = false
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Column(Modifier.fillMaxWidth().padding(0.dp)) {
                Text(
                    text = "Alarm Volume",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
                    fontSize = 14.sp,
                )
                Slider(
                    modifier = Modifier.padding(vertical = 0.dp),
                    value = alarm.volume,
                    onValueChange = { volume -> alarm = alarm.copy(volume = volume) },
                )
            }

            TaskToggleRow(
                title = "BarcodeTask",
                subtitle = alarm.barcodeName,
                checked = alarm.barcodeTask,
                onOpenPicker = { showBarcodePopup = true },
                onCheckedChange = {
                    if (alarm.barcodeName == "No Barcode Selected") {
                        showBarcodePopup = true
                    } else {
                        alarm = alarm.copy(barcodeTask = !alarm.barcodeTask)
                    }
                },
            )
            Spacer(Modifier.height(16.dp))

            TaskToggleRow(
                title = "Shake Task",
                checked = alarm.shakeTask,
                onCheckedChange = { alarm = alarm.copy(shakeTask = !alarm.shakeTask) },
            )
            Spacer(Modifier.height(16.dp))

            TaskToggleRow(
                title = "Math Task",
                checked = alarm.mathTask,
                onCheckedChange = { alarm = alarm.copy(mathTask = !alarm.mathTask) },
            )
            Spacer(Modifier.height(16.dp))

            TaskToggleRow(
                title = "Memory Task",
                checked = alarm.memoryTask,
                onCheckedChange = { alarm = alarm.copy(memoryTask = !alarm.memoryTask) },
            )
        }

        AddBarcodePopup(
            showPopup = showBarcodePopup,
            onClickOutside = { showBarcodePopup = false },
            onAlarmChange = { newAlarm -> alarm = newAlarm },
            onOpenCamera = onOpenCamera,
            viewModel = viewModel,
            alarm = alarm,
        )

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(color = BackgroundColor)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = MainButtonColors(),
                contentPadding = PaddingValues(vertical = 12.dp),
                onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    onSave(alarm.copy(hour = hour, minute = minute))
                },
            ) { Text(text = saveButtonLabel, fontSize = 14.sp) }
        }
    }
}

@Composable
private fun TaskToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    subtitle: String? = null,
    onOpenPicker: (() -> Unit)? = null,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onOpenPicker?.invoke() },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = title)
                if (subtitle != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(text = subtitle, color = Color.Gray, fontSize = 14.sp)
                }
            }
            Switch(checked = checked, onCheckedChange = { onCheckedChange() })
        }
    }
}

@Composable
private fun AddBarcodePopup(
    showPopup: Boolean,
    onClickOutside: () -> Unit,
    onAlarmChange: (Alarm) -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: AlarmViewModel,
    alarm: Alarm,
) {
    val state by viewModel.barcodeUIState.collectAsStateWithLifecycle()

    if (showPopup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .zIndex(10F)
                .clip(RoundedCornerShape(50.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Popup(
                alignment = Alignment.Center,
                properties = PopupProperties(excludeFromSystemGesture = true),
                onDismissRequest = onClickOutside,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f)
                        .background(color = BackgroundColor)
                        .clip(RoundedCornerShape(10.dp)),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                    ) {
                        Button(onClick = onClickOutside) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Cancel")
                        }
                        Button(onClick = {
                            onClickOutside()
                            onOpenCamera()
                        }) {
                            Text(text = "Add new Barcode")
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    LazyColumn(Modifier.fillMaxHeight()) {
                        itemsIndexed(state) { _, barcode: Barcode ->
                            OutlinedCard(
                                onClick = {
                                    onAlarmChange(
                                        alarm.copy(
                                            barcode = barcode.barcode,
                                            barcodeName = barcode.name,
                                        ),
                                    )
                                    onClickOutside()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = barcode.name, fontSize = 24.sp)
                                    Button(
                                        contentPadding = PaddingValues(4.dp),
                                        onClick = { viewModel.deleteBarcode(barcode) },
                                    ) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Add(
    viewModel: AlarmViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onOpenCamera: () -> Unit,
) {
    val blank = remember {
        Alarm(hour = 7, minute = 0, audioFile = alarmSoundDisplayNames.first())
    }
    AlarmEditor(
        initialAlarm = blank,
        viewModel = viewModel,
        onCancel = onCancel,
        onSave = { viewModel.addAlarm(it); onSave() },
        onOpenCamera = onOpenCamera,
        saveButtonLabel = "Add Alarm",
    )
}

@Composable
fun Edit(
    alarmId: Long,
    viewModel: AlarmViewModel,
    alarmScheduler: AlarmScheduler,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onOpenCamera: () -> Unit,
) {
    val state by viewModel.alarmUIState.collectAsStateWithLifecycle()
    val existing = state.find { it.id == alarmId }

    if (existing == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Can't find that alarm anymore.")
        }
        return
    }

    AlarmEditor(
        initialAlarm = existing,
        viewModel = viewModel,
        onCancel = onCancel,
        onSave = { updated ->
            viewModel.updateAlarm(updated)
            if (existing.activated) {
                alarmScheduler.cancel(existing)
                alarmScheduler.schedule(updated)
            }
            onSave()
        },
        onOpenCamera = onOpenCamera,
        saveButtonLabel = "Save",
    )
}
