package com.example.alarmbuddy.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSObject
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationCenter
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol

// Handles taps on the alarm notification burst (see AlarmScheduler.ios.kt).
// Must be a singleton `object`, not a local instance -- UNUserNotificationCenter
// holds its delegate weakly, so anything else would get garbage collected.
@OptIn(ExperimentalForeignApi::class)
object NotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {

    fun install() {
        UNUserNotificationCenter.currentNotificationCenter().delegate = this
    }

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
}
