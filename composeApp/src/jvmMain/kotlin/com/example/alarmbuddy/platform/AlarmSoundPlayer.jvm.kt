package com.example.alarmbuddy.platform

// Desktop stand-in only (never shipped): playback isn't wired up on desktop,
// this module exists to let shared code compile/run outside Xcode.
actual class AlarmSoundPlayer actual constructor() {
    actual fun play(soundFileName: String, volume: Float) {
        println("[desktop stub] would play $soundFileName at volume $volume")
    }

    actual fun pause() {
        println("[desktop stub] would pause alarm sound")
    }

    actual fun resume() {
        println("[desktop stub] would resume alarm sound")
    }

    actual fun stop() {
        println("[desktop stub] would stop alarm sound")
    }
}
