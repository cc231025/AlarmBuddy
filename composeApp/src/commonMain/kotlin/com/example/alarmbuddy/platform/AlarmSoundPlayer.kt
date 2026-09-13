package com.example.alarmbuddy.platform

// Replaces Android's MediaPlayer usage inside AlarmService. iOS actual uses
// AVAudioPlayer with an AVAudioSession configured for category `.playback`,
// which plays through the physical mute/silent switch automatically (no
// special entitlement needed) as long as the app is in the foreground. Getting
// the same behavior while the app is backgrounded/killed is the harder problem
// covered in MIGRATION_PLAN.md (Critical Alerts entitlement).
expect class AlarmSoundPlayer() {
    fun play(soundFileName: String, volume: Float)
    fun pause()
    fun resume()
    fun stop()
}
