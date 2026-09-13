# AlarmBuddy: Android → iOS migration plan

This branch (`ios-port`) is a from-scratch iOS port of AlarmBuddy using **Kotlin
Multiplatform + Compose Multiplatform**, so the existing Compose UI and Kotlin
business logic are reused as much as possible instead of rewriting the whole
app in Swift. This branch targets **iOS only** — the Android `app` module's
Android-specific plumbing (AlarmManager, Services, CameraX, Room) is being
replaced outright, not kept side by side.

## Why this isn't a 1:1 port

A few things AlarmBuddy relies on are Android-specific OS behavior that iOS
does not expose to third-party apps at all. These are the parts worth reading
before diving into the code:

1. **No `SYSTEM_ALERT_WINDOW` equivalent.** iOS apps cannot draw over other
   apps or the lock screen UI. The "always shows the ringing screen no matter
   what" behavior has to become "a notification arrives, the user taps it (or
   it auto-opens via a Live Activity / critical alert banner), the app comes
   to the foreground and shows Ringing."

2. **No `WRITE_SETTINGS` / forced system volume equivalent.** iOS never lets
   an app override the physical volume the user has set, nor unmute Silent
   Mode/Focus, *unless* the app has been granted the **Critical Alerts**
   entitlement by Apple. That's a special capability you request from Apple
   (via a form on the developer portal, tied to a paid Apple Developer
   Program account) explaining the alarm-clock use case; Apple reviews and
   grants it manually. This is the realistic equivalent of the "max annoying,
   can't be silenced" requirement, but it is not automatic and not
   guaranteed — budget time for that approval process separately from the
   code port. Without it, the alarm behaves like a normal notification sound:
   it respects Silent Mode/Do Not Disturb and is capped at ~30 seconds of
   audio per notification.

3. **No arbitrary background wake-and-play.** Android's `AlarmManager` +
   `BroadcastReceiver` + foreground `Service` combo can launch your UI and
   start looping `MediaPlayer` audio with the phone locked and the app fully
   killed, no user interaction required. iOS cannot launch an app's UI or
   start fresh audio playback from a background state without either a user
   tap on a notification, or the app already being open. The standard
   workaround used by every iOS alarm-clone app (Alarmy, Sleep Cycle, etc.)
   is what this branch implements: schedule a *burst* of local notifications
   a few seconds apart around the alarm time (iOS caps an app at 64 pending
   notifications, so e.g. one every 3s for ~3 minutes), each with a loud
   custom sound, so the phone effectively "keeps ringing" until the user
   taps one — at which point the app opens straight into the same
   barcode/shake/math/memory Ringing flow you already built.

4. **Volume-change interception is gone.** The `AudioManager`
   `VOLUME_CHANGED_ACTION` broadcast receiver that snaps the alarm stream back
   to max has no iOS analog; apps cannot observe or override the hardware
   volume buttons.

None of this is a shortcoming in the port — it's Apple's sandboxing model.
Where Android trades battery/security for letting alarm apps do whatever they
want in the background, iOS does not offer that trade at all.

## Target module layout

```
composeApp/
  src/
    commonMain/kotlin/com/example/alarmbuddy/
      data/            // Alarm, Barcode models, repository, SQLDelight-backed DAOs
      ui/               // all Compose screens (mostly unchanged from Android)
      platform/          // expect declarations: AlarmScheduler, ShakeDetector,
                          // BarcodeScanner, Settings, AlarmSoundPlayer
    iosMain/kotlin/com/example/alarmbuddy/platform/
      AlarmScheduler.ios.kt   // UNUserNotificationCenter-based actual
      ShakeDetector.ios.kt    // CMMotionManager-based actual
      BarcodeScanner.ios.kt   // AVFoundation + Vision-based actual
      Settings.ios.kt         // NSUserDefaults-based actual
      AlarmSoundPlayer.ios.kt // AVAudioPlayer-based actual
iosApp/
  iosApp.xcodeproj/         // thin Xcode wrapper, hosts ComposeUIViewController
  iosApp/
    iosAppApp.swift
    ContentView.swift
    Info.plist               // camera/motion usage strings, background audio mode
    AppDelegate.swift         // UNUserNotificationCenterDelegate → routes taps
```

## Mapping table

| Android piece | File(s) | iOS replacement |
|---|---|---|
| `AlarmManager.setExactAndAllowWhileIdle` | `alarmComposables.kt` | `UNUserNotificationCenter` + `UNCalendarNotificationTrigger`, burst-scheduled |
| `AlarmReceiver` (BroadcastReceiver) | `AlarmReceiver.kt` | `UNUserNotificationCenterDelegate` + app-launch routing via `userInfo` |
| `AlarmService` (foreground service, MediaPlayer, wake lock) | `AlarmReceiver.kt` | In-app `AVAudioPlayer` loop, started when the user opens the app from a notification; `audio` UIBackgroundMode keeps it playing if backgrounded mid-ring |
| `WRITE_SETTINGS` / max volume lock | `MainActivity.kt` | Critical Alerts entitlement (Apple-approved) + `AVAudioSession` category `.playback` |
| `SYSTEM_ALERT_WINDOW` | `MainActivity.kt` | Not possible; replaced by notification-tap-to-foreground |
| CameraX + ML Kit barcode scan | `cameraComposables.kt` | `AVCaptureSession` + `AVCaptureMetadataOutput` (or Vision `VNDetectBarcodesRequest`) |
| `SensorManager` accelerometer shake | `taskComposables.kt` | `CMMotionManager` accelerometer updates |
| `SharedPreferences` | `AlarmReceiver.kt`, `MainActivity.kt` | `NSUserDefaults` |
| Room database | `data/db/*` | SQLDelight (mature Kotlin/Native support, unlike Room's newer and less battle-tested iOS target) |
| `MediaPlayer` | `AlarmService` | `AVAudioPlayer` |
| Notification channel + `NotificationCompat` | `AlarmService` | `UNNotificationCategory` + `UNMutableNotificationContent` |

## Distribution note

This branch produces a plain `.ipa` via CI (see `.github/workflows/ios-build.yml`).
Getting that file onto a physical iPhone without an Apple Developer Program
membership requires the installing device to sideload it via AltStore/AltServer
or SideStore (free, but needs a computer at least once on the installing
side) — see the conversation history for the full breakdown of options.
