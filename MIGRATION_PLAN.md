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
expect. None of the options below need Apple's $99/year Developer Program;
all of them need a computer (any OS) at least once, since generating a
signing certificate/pairing file from a free Apple ID is something only a
desktop client can do -- confirmed against each project's current (2026)
documentation, not assumed:

- **SideStore** (recommended): needs a Windows/macOS/Linux computer for a
  *one-time* pairing step, then installs/refreshes apps over Wi-Fi from the
  phone itself from then on, with no further computer contact -- a
  background helper on the phone renews the free Apple ID's 7-day
  certificate automatically. This is the best fit for "occasional PC access,
  no Mac, won't pay Apple."
- **AltStore Classic / AltServer**: simpler and more mature, but has no
  on-device auto-refresh -- AltServer has to reconnect to the phone
  (Wi-Fi or USB) roughly every 7 days or the app stops working until it
  does. Also caps you at 3 sideloaded apps at once under one free Apple ID
  (Apple's own provisioning-profile limit, not this project's).
- **AltStore PAL** (EU/Japan/Brazil only): doesn't need a computer for the
  *installing* user at all, and apps don't expire -- but the app's
  *publisher* still needs a paid Apple Developer account to notarize it for
  that marketplace, so it doesn't remove the cost, just moves it off this
  project onto whoever wants to distribute through PAL.
- **TrollStore**: not viable here -- it depends on an unpatched iOS
  exploit and only works on iOS 14.0-16.7-ish; there's no working version
  for current iOS releases, so it isn't an option on an up-to-date iPhone.
- Be skeptical of blog posts claiming a fully "zero-computer-ever" method
  (some third-party signer apps advertise this) -- none of the projects'
  own documentation confirms that's possible; treat such claims as unverified
  rather than a real option.

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
- Arm an alarm, background the app (press Home, don't force-quit), and wait:
  you should hear nothing until the alarm's actual minute, at which point the
  phone should start ringing *on its own*, before you touch it -- that's
  `BackgroundKeepAlive`'s quiet-loop-then-ramp-up mechanism, independent of
  the notification burst. Reopening the app at that point should land
  straight on Ringing.
- With no alarm armed, or with Guided Access already on, the Home screen's
  "Guided Access is off" reminder card should not appear. Turn Guided Access
  on (triple-click the side button) while an armed alarm is showing the
  reminder: the card should disappear within a few seconds (it's polled, not
  pushed -- iOS has no change notification for this).
- With Guided Access off, try to flip an alarm's switch to "on": it should
  refuse (the switch stays off) and show the "turn on Guided Access first"
  dialog instead of arming. Turn Guided Access on, flip the switch again: it
  should arm normally this time. Turning that same alarm off and back on
  (still with Guided Access on) should not re-show the one-time onboarding
  dialog a second time, only the blocking one if Guided Access is off at
  that moment.

Expected NOT to hold up -- these are real, permanent gaps versus the Android
version, not bugs to chase, unless Guided Access is active (see the
"zero-cost ways to claw back" section above for what Guided Access changes):
- The hardware volume-down buttons *can* lower the Ringing screen's sound,
  including to zero, unless Guided Access is active and hardware-button time
  limits are turned on. Android's AudioManager let the app force the stream
  back to max unconditionally; iOS gives no app that power over the physical
  volume level on its own.
- Turning on Do Not Disturb/a Focus mode, or leaving Silent Mode on, *before*
  the notification burst fires will silence that burst specifically (the
  background-audio keep-alive's own playback still ignores the mute switch
  via `.playback`, but a Focus mode can still suspend/limit background audio
  depending on its settings). Getting the notification sound itself to
  ignore Silent Mode/Focus requires Apple's Critical Alerts entitlement (a
  manual request tied to a paid Developer account) -- not implemented here.
- A genuine force-quit always stops the sound outright, full stop, unless
  Guided Access is active (which makes the force-quit gesture unreachable).
  No third-party app on iOS can prevent or survive a force-quit on its own.
- Revoking notification permission (Settings -> AlarmBuddy -> Notifications)
  means no burst fires at all when the alarm's due; the background-audio
  keep-alive doesn't depend on that permission, but does depend on the app
  having been backgrounded (not force-quit or never opened) since the alarm
  was armed.

## Zero-cost ways to claw back "can't be silenced" (no Developer Program, no Apple approval)

The Critical Alerts entitlement described above is off the table by choice —
it needs a paid Apple Developer Program account and a manual Apple approval.
Here's what's actually achievable with nothing but a free Apple ID and
settings that already exist on every iPhone. Both of the following are now
**implemented in this branch**, not just proposed:

1. **Guided Access (Settings -> Accessibility -> Guided Access)**, surfaced
   in-app via `ui/GuidedAccessReminder.kt`, `platform/GuidedAccess.kt` /
   `GuidedAccess.ios.kt`, and gated in `ui/HomeScreen.kt`'s arm switch. This
   is the single best lever available, and it's completely free and built
   into iOS. There is no public API for a third-party app to *turn on*
   Guided Access -- that would defeat the point of it being a deliberate,
   physical action -- but `UIAccessibilityIsGuidedAccessEnabled()` is a
   public, documented API for *checking* whether it's currently active,
   which is enough to build real enforcement pressure around:
   - **Arming is gated, not just nagged about.** Flipping an alarm's switch
     to "on" checks `isGuidedAccessEnabled()` at that exact moment; if it's
     off, the switch doesn't move and a dialog explains that Guided Access
     has to be on first. There is no way to end up with an armed alarm and
     Guided Access having never been turned on for that session -- the
     database can't hold that state. This directly answers "can we just deny
     arming until this is set up": yes, and it's implemented that way, not
     merely suggested.
   - The first time someone arms an alarm (successfully, i.e. Guided Access
     was already on), a one-time dialog walks them through the actual
     one-time setup: turning on the Accessibility Shortcut for Guided Access
     (Settings -> Accessibility -> Accessibility Shortcut -> check Guided
     Access) so that, from then on, turning Guided Access on each night is a
     single triple-click of the side button, not a menu dive. That's the
     "can this be a one-time setting" question answered as honestly as iOS
     allows: the *setup* is one-time; the nightly *activation* is a
     low-friction physical gesture Apple deliberately keeps manual, forever,
     for any app -- but the gate above means the app never lets that gesture
     be skipped and forgotten about for an armed alarm.
   - Whenever an alarm is (already) armed and Guided Access is currently off
     -- e.g. it was on at arm-time but got turned off since -- the Home
     screen shows a standing reminder card (polled every few seconds, since
     iOS gives no change notification for this, only a point-in-time check).
     Turning an alarm off and back on re-runs the gate.
   - Once active, Guided Access can disable the physical volume buttons
     entirely and blocks leaving the app (no app-switcher, no Home
     button/swipe, no Control Center) without the Guided Access passcode --
     stronger lock-in than the Android original ever had, and it's also what
     makes a force-quit unreachable in the first place (see below).
   - **What the gate doesn't and can't do:** it checks Guided Access is on
     the moment you flip the switch, not continuously afterward. Turning
     Guided Access off again after arming (with your own passcode) doesn't
     retroactively disarm the alarm -- nor should it; that would make
     disarming an alarm easier, not harder. It also means arming an alarm
     hours before bed requires either starting Guided Access early (and
     being locked into AlarmBuddy the whole time, since that's what Guided
     Access does) or arming it right before bed instead -- there's no
     "schedule Guided Access for later" concept on iOS. If that friction
     turns out to be annoying in practice, the gate could be made an opt-out
     setting rather than mandatory; it currently isn't, matching what was
     asked for.

2. **A continuous-background-audio keep-alive**, implemented in
   `platform/BackgroundKeepAlive.ios.kt` and wired up via
   `AlarmScheduler.syncArmedAlarms()` (called from `App.kt` every time the
   alarm list changes) and `Info.plist`'s `UIBackgroundModes: audio`. Instead
   of only starting the alarm sound once a notification is tapped, the app
   now starts looping the alarm's own sound at near-zero volume the moment
   it's backgrounded (Home button/swipe) while an alarm is armed. A real,
   continuously-playing `AVAudioPlayer` is what iOS accepts as a reason not
   to suspend the process (true silence or a paused player doesn't count).
   While that loop runs, a repeating timer compares wall-clock time against
   the armed alarm's target time; the moment they match, it jumps that same
   player's volume up to the alarm's real configured volume and marks the
   alarm as pending (the same `AlarmNotificationRouter` signal a tapped
   notification uses) -- so the phone can start actually ringing on its own,
   before the user even touches it, and reopening the app (from the Home
   Screen icon or a notification, whichever comes first) drops straight into
   Ringing. This is free, needs no entitlement, and is the same trick real
   free-tier iOS alarm apps use. It complements, not replaces, the
   notification burst: if the process dies anyway (force-quit, memory
   pressure, a reboot), the independently OS-scheduled notifications are
   still the fallback.

3. **What "force-quit" means, and what actually holds up now that arming is
   gated on Guided Access.** Force-quitting an app means opening the App
   Switcher (swipe up and hold, or double-click Home on older iPhones) and
   swiping that app's card away. That's different from simply pressing Home
   to background the app -- backgrounding alone only *suspends* the process
   (which is exactly what the background-audio keep-alive above is designed
   to prevent), while a force-quit unconditionally *terminates* it.
   Crucially, **the App Switcher gesture itself is one of the things Guided
   Access blocks** -- while it's active there is no way to even reach the
   screen you'd force-quit from, let alone do it. Combined with the arm-time
   gate (section 1 above), the ordinary flow is now: arm the alarm (which
   requires Guided Access already on), don't touch Guided Access again, lock
   the phone with the side button (which still works -- that only sleeps the
   screen, it isn't a way out of Guided Access) and go to sleep. In that
   flow, closing the app -- by any means, backgrounding or force-quit -- does
   not silence the alarm, because backgrounding is handled by the keep-alive
   loop and force-quit isn't a reachable action at all. Reaching the actual
   stop-the-alarm screen requires unlocking the phone, which requires
   finishing whichever tasks are configured, exactly as intended.
   What this doesn't cover, and can't: the arm-time check is a one-time gate,
   not continuous enforcement, so nothing stops someone from deliberately
   re-entering Settings (or triple-clicking and typing their own Guided
   Access passcode) to turn Guided Access back off before actually going to
   sleep -- at which point the App Switcher becomes reachable again and a
   force-quit that night would cut the sound immediately. That's not "closing
   the app," though; it's consciously dismantling a lock with the same key
   you set it with, which no app -- free or paid, on any platform -- can
   prevent someone from doing to their own device. The other true edge case
   is a full device reboot or shutdown, which clears Guided Access (and the
   running app) along with it; that's outside any app's control and is rare
   in practice. Short of those two deliberate/edge cases, the notification
   burst and the `navigateTo`/`ringingAlarmId` ringing-state persistence in
   `RingingScreen.kt`/`MainViewController.kt`/`main.kt` remain as a second
   layer of defense regardless -- so even in the "Guided Access got turned
   off" scenario, reopening the app always lands back on the Ringing/
   task-gate screen, never a normal Home screen where the alarm could just
   be ignored.

Put together: with arming gated on Guided Access, the ordinary "set it and go
to sleep" flow now holds up the way the original Android app's core promise
worked -- the alarm cannot be silenced by closing the app, full stop, because
closing the app either does nothing (backgrounding, handled by the keep-alive
loop) or isn't reachable (force-quit, blocked by Guided Access). What remains
possible is only a deliberate, informed act of turning off your own lock
before the alarm fires, or a device reboot -- and that's a materially
stronger guarantee than "every alarm app on the App Store that isn't using
Apple's Critical Alerts" typically offers, precisely because most of them
don't gate arming on Guided Access the way this branch now does.

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
