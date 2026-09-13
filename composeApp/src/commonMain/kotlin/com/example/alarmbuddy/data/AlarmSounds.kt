package com.example.alarmbuddy.data

// Display name -> bundled sound file name (without extension). Kept as the
// single source of truth so the "Add alarm" sound picker, the scheduled
// notification sound, and the in-app looping player all agree on the same
// mapping. This intentionally preserves the original Android app's mapping
// (including "Classic Alarm" pointing at the level_up file) rather than
// silently "fixing" it during the port.
val alarmSounds: Map<String, String> = mapOf(
    "Classic Alarm" to "level_up",
    "Pain Alarm" to "pain_alarm",
    "Granular Alarm" to "granular_alarm",
    "Ambient Scifi" to "ambient_scifi",
    "Increasing Panic" to "increasing_panic",
    "Level Up" to "level_up",
    "Relaxing Piano" to "relaxing_piano",
    "School Clock" to "school_clock",
    "Sunny Morning" to "sunny_morning",
    "Synth Power" to "synth_power",
)

fun soundFileNameFor(displayName: String): String = alarmSounds[displayName] ?: "level_up"

// Two of the bundled tracks were authored as mp3; everything else is wav.
// Notification sounds always use a separately-generated "<name>_notif.wav"
// clip regardless (see iosApp/iosApp/Resources/Sounds) since UNNotificationSound
// only accepts aiff/wav/caf and caps out at 30 seconds.
private val mp3SoundFiles = setOf("level_up", "sunny_morning")

fun soundFileExtension(fileName: String): String = if (fileName in mp3SoundFiles) "mp3" else "wav"
