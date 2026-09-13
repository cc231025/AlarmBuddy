package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.soundFileExtension
import com.example.alarmbuddy.data.soundFileNameFor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.Foundation.NSBundle
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDate
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSTimer
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification

/**
 * The notification burst in AlarmScheduler.ios.kt works even if the app's
 * process is dead, but it depends on the user noticing and tapping a
 * notification. This object is a second, complementary mechanism: the
 * standard trick every free-tier iOS alarm app uses to keep its own process
 * *alive* in the background so it can ring on its own, without waiting for a
 * tap.
 *
 * How it works: the moment the app is backgrounded (Home button/swipe-up --
 * NOT a force-quit, see below) while an alarm is armed, this starts looping
 * the alarm's own sound file at a near-inaudible volume. A real, continuously
 * playing `AVAudioPlayer` under the `audio` UIBackgroundMode (see
 * iosApp/iosApp/Info.plist) is what iOS accepts as a legitimate reason not to
 * suspend the process -- silence or a paused player doesn't count. While that
 * loop runs, a repeating timer compares wall-clock time against the armed
 * alarm's target time; the moment they match, it jumps the *same* player's
 * volume up to the alarm's real configured volume and marks the alarm as
 * pending, so:
 *   - if the phone is just locked/backgrounded, the phone starts actually
 *     ringing on its own, before the user even touches it;
 *   - whenever the user next opens the app (Home Screen icon or a
 *     notification tap, whichever happens first), AlarmNotificationRouter's
 *     pendingAlarmId routes straight into the same Ringing/task-gate screen
 *     the notification-tap path uses -- see App.kt.
 *
 * What this does NOT survive: a genuine force-quit (swipe-away in the App
 * Switcher). That is SpringBoard, not this app, tearing the process down, and
 * no third-party app on iOS -- free or paid, with or without Apple's
 * cooperation -- can prevent or survive that. The notification burst is what
 * still gets the user's attention if that happens; the ringing-state
 * persistence in RingingScreen.kt/MainViewController.kt is what stops a
 * reopen after a force-quit from landing on a normal Home screen. Guided
 * Access (see GuidedAccessReminder.kt) is the only free lever that actually
 * prevents the force-quit gesture from being reachable in the first place.
 */
@OptIn(ExperimentalForeignApi::class)
object BackgroundKeepAlive {
    private const val QUIET_VOLUME = 0.01f
    private const val CHECK_INTERVAL_SECONDS = 1.0

    private var player: AVAudioPlayer? = null
    private var checkTimer: NSTimer? = null
    private var armedAlarm: Alarm? = null

    init {
        val center = NSNotificationCenter.defaultCenter
        center.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null,
        ) { onEnterBackground() }
        center.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null,
        ) { onEnterForeground() }
    }

    /**
     * Call whenever the set of activated alarms changes -- on cold launch
     * with whatever's already in the database, and every time an alarm is
     * added, edited, toggled, or deleted. Pass the single soonest-upcoming
     * activated alarm, or null if none are armed.
     */
    fun setNextArmedAlarm(alarm: Alarm?) {
        armedAlarm = alarm
        if (alarm == null) {
            stopQuietLoop()
        }
    }

    private fun onEnterBackground() {
        armedAlarm?.let { startQuietLoop(it) }
    }

    private fun onEnterForeground() {
        // The app itself being alive in the foreground is already enough to
        // not get suspended, so the keep-alive trick isn't needed until the
        // next time it's backgrounded. This also hands off cleanly to
        // AlarmSoundPlayer once RingingScreen starts its own playback.
        stopQuietLoop()
    }

    private fun startQuietLoop(alarm: Alarm) {
        if (player != null) return // already running for this session

        // Setting the category is enough to route through the mute switch;
        // explicit session activation isn't exposed by this SDK's
        // AVAudioSession binding, but starting playback below activates the
        // session implicitly anyway.
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)

        val soundName = soundFileNameFor(alarm.audioFile)
        val extension = soundFileExtension(soundName)
        val url = NSBundle.mainBundle.URLForResource(soundName, withExtension = extension) ?: return
        val newPlayer = AVAudioPlayer(contentsOfURL = url, error = null) ?: return

        newPlayer.numberOfLoops = -1
        newPlayer.volume = QUIET_VOLUME
        newPlayer.prepareToPlay()
        newPlayer.play()
        player = newPlayer

        checkTimer = NSTimer.scheduledTimerWithTimeInterval(
            interval = CHECK_INTERVAL_SECONDS,
            repeats = true,
        ) { checkIfDue(alarm) }
    }

    private fun checkIfDue(alarm: Alarm) {
        val components = NSCalendar.currentCalendar.components(
            NSCalendarUnitHour or NSCalendarUnitMinute,
            fromDate = NSDate(),
        )
        if (components.hour.toInt() == alarm.hour && components.minute.toInt() == alarm.minute) {
            fireNow(alarm)
        }
    }

    private fun fireNow(alarm: Alarm) {
        player?.volume = alarm.volume
        // Same routing mechanism the tapped-notification path uses (see
        // App.kt) -- whichever screen is on top next reads this and jumps
        // straight to Ringing.
        AlarmNotificationRouter.pendingAlarmId.value = alarm.id
        // Stop polling once fired; RingingScreen (once opened) or the next
        // backgrounding of a *different* upcoming alarm will restart this
        // cleanly via setNextArmedAlarm.
        checkTimer?.invalidate()
        checkTimer = null
    }

    private fun stopQuietLoop() {
        checkTimer?.invalidate()
        checkTimer = null
        player?.stop()
        player = null
    }
}
