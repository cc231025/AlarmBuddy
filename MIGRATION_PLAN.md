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

This branch produces a **deliberately unsigned** `.ipa` via CI (see
`.github/workflows/ios-build.yml`) — no Apple Developer account or signing
secrets are configured, and that's intentional, not an oversight. AltStore,
AltServer, and SideStore all re-sign an app themselves using the *installing*
user's own free Apple ID, so an unsigned `.ipa` is exactly the input they
expect. Getting that file onto a physical iPhone still requires a computer
somewhere in the process on the installing side (AltServer running on any
Mac/Windows/Linux machine, or a one-time SideStore bootstrap) — see the
conversation history for the full breakdown of why a fully phone-only,
zero-computer path requires paying for Apple's Developer Program and using
TestFlight instead.

## The Xcode project is generated, not hand-committed

`iosApp/` contains a `project.yml` (an [XcodeGen](https://github.com/yonaskolb/XcodeGen)
spec) instead of a checked-in `.xcodeproj`. Hand-editing Xcode's binary/plist
`.pbxproj` format correctly, from a Linux sandbox with no Xcode available to
verify it, is exactly the kind of thing that silently produces a broken
project — a readable YAML spec that CI regenerates into a real Xcode project
right before every build is a lot more robust. Locally on a Mac: `brew
install xcodegen && cd iosApp && xcodegen generate` produces `iosApp.xcodeproj`
before opening it in Xcode.

## Testing the "can't be silenced" behavior

This is the actual point of the app, so it deserves an explicit test matrix
rather than just "try it and see." Test these on a real device (not just the
Simulator -- background audio suspension in particular behaves differently
on real hardware):

Expected to hold up (these are what iOS actually lets an app guarantee):
- Set a short test alarm, lock the phone, wait for it to fire: a burst of
  notifications should appear on the lock screen, one every ~3s, each with
  sound.
- Swipe away/dismiss one notification from the lock screen: the *rest* of
  the burst should keep firing -- dismissing one doesn't cancel the others,
  since they were all scheduled independently up front.
- Tapping any one of them should open the app straight into Ringing (not
  Home), and the full alarm sound should start looping.
- While Ringing's sound is playing in the foreground, flipping the physical
  Silent switch should NOT mute it (AVAudioSession category `.playback`
  ignores the switch). This is the iOS equivalent of the original app's
  WRITE_SETTINGS-based volume lock.
- Force-quitting the app mid-ring (swipe up in the app switcher) *will* cut
  the sound immediately -- then reopening the app (from the home screen icon,
  not a notification) should drop you straight back into the same
  Ringing/task-gate screen, not a normal Home screen where you could just
  ignore it. (This is what the `navigateTo`/`ringingAlarmId` persistence in
  AppSettings is for.)

Expected NOT to hold up -- these are real, permanent gaps versus the Android
version, not bugs to chase:
- The hardware volume-down buttons *can* lower the Ringing screen's sound,
  including to zero. Android's AudioManager let the app force the stream
  back to max; iOS gives no app that power over the physical volume level.
- Turning on Do Not Disturb/a Focus mode, or leaving Silent Mode on, *before*
  the notification burst fires will silence it. Getting the notification
  sound itself to ignore Silent Mode/Focus requires Apple's Critical Alerts
  entitlement (a manual request tied to a paid Developer account) -- not
  implemented here.
- Force-quitting the app always stops the sound outright, full stop. No
  third-party app on iOS can prevent a force-quit or survive it with audio
  still playing.
- Revoking notification permission (Settings -> AlarmBuddy -> Notifications)
  means no burst fires at all when the alarm's due. There's no fallback path
  for this in the current design.

## A note on how this was built, and what's still unverified

This port was written in a Linux sandbox with no macOS/Xcode available, and
(separately) no network access to Maven Central / the Gradle plugin portal
either, which means **none of this has been compiled, not even the shared
Kotlin/Compose code on a desktop JVM target**. The `composeApp` module
includes a `jvm` target specifically so that once this reaches an environment
with normal network access, running `./gradlew :composeApp:run` gives a fast,
Xcode-free way to smoke-test the shared UI/business logic layer (data models,
ViewModel, navigation, all the Compose screens) before ever touching the iOS
side.

The highest-risk, least-verified code is the iOS-specific Kotlin/Native
interop in `composeApp/src/iosMain/kotlin/.../platform/`
(`AlarmScheduler.ios.kt`, `Accelerometer.ios.kt`, `AlarmSoundPlayer.ios.kt`,
`BarcodeScannerView.ios.kt`, `NotificationDelegate.ios.kt`) and the Swift
glue in `iosApp/iosApp/ComposeView.swift`. These follow well-established
patterns from the Kotlin Multiplatform/Compose Multiplatform ecosystem, but
were written without a compiler to check them against. Treat the first
`.github/workflows/ios-build.yml` run as the real first compile of this
branch, and expect it to take a couple of rounds of reading CI logs and
fixing small interop mistakes (wrong parameter label, wrong import, etc.)
before it goes green — that's normal for a port of this size done this way,
not a sign something is fundamentally wrong with the approach.
