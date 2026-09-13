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
// a `class` with a companion-held singleton rather than a Kotlin `object`
// because Kotlin/Native's backend crashes ("Allocation of Obj-C class ...
// should have been lowered") when an `object` directly subclasses an
// Objective-C class like NSObject.
@OptIn(ExperimentalForeignApi::class)
class NotificationDelegate private constructor() : NSObject(), UNUserNotificationCenterDelegateProtocol {

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
        private val instance = NotificationDelegate()

        fun install() {
            UNUserNotificationCenter.currentNotificationCenter().delegate = instance
        }
    }
}
