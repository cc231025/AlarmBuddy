package com.example.alarmbuddy.platform

// AlarmKit (iOS 26+) is the real fix for the thing Guided Access never
// actually delivered: an alarm that rings -- through Silent Mode and Focus,
// even if the app was fully force-quit -- without a paid Apple Developer
// account. But its API is unreachable from Kotlin/Native directly: it's
// built entirely on Swift generics (AlarmManager.AlarmConfiguration<Metadata>),
// SwiftUI types (Color), and the App Intents framework, none of which have
// an Objective-C-compatible surface for cinterop to bind against.
//
// So the real scheduling work lives in Swift (see AlarmKitBridge.swift in
// the iosApp Xcode project), and this interface is the seam: Swift installs
// an implementation into AlarmKitBridgeHolder.bridge once at launch (see
// ComposeView.swift), and AlarmScheduler.ios.kt calls through it.
interface IosAlarmKitBridge {
    fun scheduleAlarm(alarmId: Long, hour: Int, minute: Int, soundFileName: String)
    fun cancelAlarm(alarmId: Long)
}

object AlarmKitBridgeHolder {
    var bridge: IosAlarmKitBridge? = null
}
