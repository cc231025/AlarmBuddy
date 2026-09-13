package com.example.alarmbuddy.platform

import platform.UIKit.UIAccessibilityIsGuidedAccessEnabled
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSURL

actual fun isGuidedAccessEnabled(): Boolean = UIAccessibilityIsGuidedAccessEnabled()

actual fun openSystemSettings() {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    val app = UIApplication.sharedApplication
    if (app.canOpenURL(url)) {
        app.openURL(url)
    }
}
