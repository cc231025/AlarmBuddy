package com.example.alarmbuddy.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

// Handles taps on the alarm notification burst (see AlarmScheduler.ios.kt).
// Must stay alive for the process lifetime -- UNUserNotificationCenter holds
// its delegate weakly, so anything else would get garbage collected. This is
// a `class` with the singleton instance held in a top-level val (below)
// rather than a Kotlin `object`, because Kotlin/Native's backend rejects
// Objective-C subclasses declared as `object` ("Allocation of Obj-C class
// ... should have been lowered") and also rejects stored fields on the
// companion of an Objective-C subclass ("Fields are not supported for
// Companion of subclass of ObjC type").
@OptIn(ExperimentalForeignApi::class)
class NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {

    // Keep showing the banner+sound even while the app is already in the
    // foreground (otherwise a burst notification firing while the user is
    // looking at the app would be silently swallowed).
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (platform.UserNotifications.UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionSound)
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit,
    ) {
        val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
        (userInfo?.get("alarmId") as? String)?.toLongOrNull()?.let {
            AlarmNotificationRouter.pendingAlarmId.value = it
        }
        withCompletionHandler()
    }

    companion object {
        fun install() {
            UNUserNotificationCenter.currentNotificationCenter().delegate = notificationDelegateInstance
        }
    }
}

private val notificationDelegateInstance = NotificationDelegate()
