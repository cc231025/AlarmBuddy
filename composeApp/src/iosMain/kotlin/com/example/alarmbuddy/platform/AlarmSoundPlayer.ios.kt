package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.soundFileExtension
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.Foundation.NSBundle

// Replaces Android's MediaPlayer usage inside AlarmService. AVAudioSession's
// `.playback` category plays through the physical mute switch automatically
// (no special entitlement needed) as long as the app is in the foreground --
// which is the case once the user has tapped into the Ringing screen from a
// notification. See MIGRATION_PLAN.md for the background/killed-app case.
@OptIn(ExperimentalForeignApi::class)
actual class AlarmSoundPlayer actual constructor() {
    private var player: AVAudioPlayer? = null

    actual fun play(soundFileName: String, volume: Float) {
        // Setting the category is enough to route through the mute switch;
        // explicit session activation isn't exposed by this SDK's
        // AVAudioSession binding, but starting playback below activates the
        // session implicitly anyway.
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)

        val extension = soundFileExtension(soundFileName)
        val url = NSBundle.mainBundle.URLForResource(soundFileName, withExtension = extension)
            ?: return

        val newPlayer = AVAudioPlayer(contentsOfURL = url, error = null) ?: return
        newPlayer.numberOfLoops = -1 // loop forever, matches the original MediaPlayer.isLooping = true
        newPlayer.volume = volume
        newPlayer.prepareToPlay()
        newPlayer.play()
        player = newPlayer
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun resume() {
        player?.play()
    }

    actual fun stop() {
        player?.stop()
        player = null
    }
}
