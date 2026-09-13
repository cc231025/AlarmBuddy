import AlarmKit
import AppIntents
import ComposeApp
import Foundation
import SwiftUI

// Empty metadata: this app doesn't need any custom data carried alongside
// the alarm beyond what AlarmKit already tracks (id, schedule, presentation).
@available(iOS 26.0, *)
private struct AlarmBuddyAlarmMetadata: AlarmMetadata {}

// Runs when the user taps "Stop" on a ringing AlarmKit alarm -- on the Lock
// Screen, in the Dynamic Island, or the full-screen alert. openAppWhenRun is
// true because the whole point is forcing the barcode/shake/math/memory
// task gate in the Ringing screen before the alarm can actually be
// dismissed; a silent, no-launch stop would defeat that.
//
// This can't safely reach into Kotlin/Compose state directly (App Intents
// aren't guaranteed to run in the same process as the rest of the app), so
// it persists the pending navigation as plain NSUserDefaults values instead
// -- the same "navigateTo"/"ringingAlarmId" keys the app already used for
// its cold-launch check (see AlarmNotificationRouter.kt), just written from
// Swift now instead of Kotlin. Deliberately NOT using an App Group: that
// entitlement requires a paid Apple Developer Program membership, which
// this project doesn't have.
@available(iOS 26.0, *)
struct AlarmStopIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "Stop Alarm"
    static var description = IntentDescription("Stops the AlarmBuddy alarm and opens the app.")
    static var openAppWhenRun: Bool = true

    @Parameter(title: "alarmId")
    var alarmId: Int

    init() {}

    init(alarmId: Int) {
        self.alarmId = alarmId
    }

    func perform() async throws -> some IntentResult {
        let defaults = UserDefaults.standard
        defaults.set("Ringing", forKey: "navigateTo")
        defaults.set(alarmId, forKey: "ringingAlarmId")
        return .result()
    }
}

// Installed into AlarmKitBridgeHolder.shared.bridge once at launch (see
// ComposeView.swift) so Kotlin's AlarmScheduler.ios.kt can reach AlarmKit
// without needing cinterop against it directly -- AlarmKit's real API
// (AlarmManager.AlarmConfiguration<Metadata>, SwiftUI's Color, App Intents)
// has no Objective-C-compatible surface for Kotlin/Native to bind against.
@available(iOS 26.0, *)
final class AlarmKitBridge: IosAlarmKitBridge {
    func requestAuthorizationIfNeeded() {
        Task {
            switch AlarmManager.shared.authorizationState {
            case .notDetermined:
                _ = try? await AlarmManager.shared.requestAuthorization()
            case .authorized, .denied:
                break
            @unknown default:
                break
            }
        }
    }

    func scheduleAlarm(alarmId: Int64, hour: Int32, minute: Int32, soundFileName: String) {
        Task {
            do {
                let stopButton = AlarmButton(
                    text: "Stop",
                    textColor: .white,
                    systemImageName: "stop.circle"
                )
                let alert = AlarmPresentation.Alert(
                    title: "Alarm Triggered",
                    stopButton: stopButton
                )
                let attributes = AlarmAttributes<AlarmBuddyAlarmMetadata>(
                    presentation: AlarmPresentation(alert: alert),
                    metadata: AlarmBuddyAlarmMetadata(),
                    tintColor: .blue
                )
                let config = AlarmManager.AlarmConfiguration<AlarmBuddyAlarmMetadata>(
                    schedule: .fixed(nextOccurrence(hour: Int(hour), minute: Int(minute))),
                    attributes: attributes,
                    stopIntent: AlarmStopIntent(alarmId: Int(alarmId)),
                    sound: soundFileName.isEmpty ? .default : .named(soundFileName)
                )
                try await AlarmManager.shared.schedule(id: uuid(for: alarmId), configuration: config)
            } catch {
                print("[AlarmKitBridge] Failed to schedule alarm \(alarmId): \(error)")
            }
        }
    }

    func cancelAlarm(alarmId: Int64) {
        do {
            try AlarmManager.shared.cancel(id: uuid(for: alarmId))
        } catch {
            print("[AlarmKitBridge] Failed to cancel alarm \(alarmId): \(error)")
        }
    }

    // AlarmKit identifies alarms by UUID; this app identifies them by the
    // SQLite row id (a Long/Int64). Deriving the UUID deterministically from
    // the id means scheduleAlarm() and cancelAlarm() always agree on which
    // UUID a given alarm maps to, with nothing to persist separately.
    private func uuid(for alarmId: Int64) -> UUID {
        let hex = String(format: "%016x", alarmId)
        return UUID(uuidString: "00000000-0000-0000-0000-\(hex)")!
    }

    private func nextOccurrence(hour: Int, minute: Int) -> Date {
        let calendar = Calendar.current
        let now = Date()
        var components = calendar.dateComponents([.year, .month, .day], from: now)
        components.hour = hour
        components.minute = minute
        components.second = 0

        guard let candidate = calendar.date(from: components) else { return now }
        if candidate <= now {
            return calendar.date(byAdding: .day, value: 1, to: candidate) ?? candidate
        }
        return candidate
    }
}
