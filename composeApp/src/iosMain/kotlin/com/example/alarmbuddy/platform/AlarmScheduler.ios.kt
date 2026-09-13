package com.example.alarmbuddy.platform

import com.example.alarmbuddy.data.Alarm
import com.example.alarmbuddy.data.soundFileNameFor
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSinceNow
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Replaces AlarmManager.setExactAndAllowWhileIdle() + AlarmReceiver + AlarmService.
 *
 * iOS cannot silently wake the app and start playing audio in the background
 * the way Android's exact alarms can. The standard substitute (used by every
 * iOS alarm-clone app) is a *burst* of local notifications clustered around
 * the target time, each with a loud sound, so the phone keeps making noise
 * every few seconds until the user taps one and the app opens straight into
 * the Ringing screen. See MIGRATION_PLAN.md for the full rationale, including
 * why getting this to bypass Silent Mode/Focus reliably needs Apple's
 * Critical Alerts entitlement.
 */
@OptIn(ExperimentalForeignApi::class)
actual class AlarmScheduler {

    actual fun schedule(alarm: Alarm) {
        cancel(alarm) // clear any previous burst for this alarm id first

        val center = UNUserNotificationCenter.currentNotificationCenter()
        val secondsUntilFirstFire = secondsUntilNext(alarm.hour, alarm.minute)
        val soundName = soundFileNameFor(alarm.audioFile)

        for (index in 0 until BURST_COUNT) {
            val content = UNMutableNotificationContent().apply {
                setValue("Alarm Triggered", forKey = "title")
                setValue("Your alarm is ringing! Open AlarmBuddy to stop it.", forKey = "body")
                setValue(UNNotificationSound.soundNamed("${soundName}_notif.wav"), forKey = "sound")
                setValue(mapOf("alarmId" to alarm.id.toString()), forKey = "userInfo")
                setValue(ALARM_CATEGORY, forKey = "categoryIdentifier")
            }

            val fireDelay = (secondsUntilFirstFire + index * BURST_INTERVAL_SECONDS)
                .coerceAtLeast(1.0)

            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                timeInterval = fireDelay,
                repeats = false,
            )

            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifierFor(alarm.id, index),
                content = content,
                trigger = trigger,
            )

            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    actual fun cancel(alarm: Alarm) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val identifiers = (0 until BURST_COUNT).map { identifierFor(alarm.id, it) }
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    actual fun syncArmedAlarms(alarms: List<Alarm>) {
        val next = alarms
            .filter { it.activated }
            .minByOrNull { secondsUntilNext(it.hour, it.minute) }
        BackgroundKeepAlive.setNextArmedAlarm(next)
    }

    private fun identifierFor(alarmId: Long, index: Int) = "alarm-$alarmId-$index"

    private fun secondsUntilNext(hour: Int, minute: Int): Double {
        val calendar = NSCalendar.currentCalendar
        val now = NSDate()
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
                NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
            fromDate = now,
        )
        components.setValue(hour.toLong(), forKey = "hour")
        components.setValue(minute.toLong(), forKey = "minute")
        components.setValue(0L, forKey = "second")

        var target = calendar.dateFromComponents(components) ?: now
        if (target.timeIntervalSinceNow < 0) {
            target = calendar.dateByAddingUnit(
                unit = NSCalendarUnitDay,
                value = 1,
                toDate = target,
                options = 0u,
            ) ?: target
        }
        return target.timeIntervalSinceNow
    }

    companion object {
        const val ALARM_CATEGORY = "ALARM_CATEGORY"

        // 40 notifications, 3s apart == 2 minutes of "ringing" before it goes
        // quiet. Tune to taste, but remember the app has a shared 64-pending
        // notification budget across every scheduled+active alarm.
        private const val BURST_COUNT = 40
        private const val BURST_INTERVAL_SECONDS = 3.0
    }
}

/** Call once at app startup (see MainViewController.kt). */
@OptIn(ExperimentalForeignApi::class)
fun requestNotificationAuthorization(onResult: (Boolean) -> Unit) {
    UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
    ) { granted, _ ->
        onResult(granted)
    }
}
