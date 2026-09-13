import SwiftUI
import UIKit
import ComposeApp

// Thin UIKit wrapper around the shared Kotlin/Compose UI (MainViewController()
// in composeApp/src/iosMain/.../MainViewController.kt), plus the one bit of
// glue Compose Multiplatform can't express itself: honoring
// OrientationLock.isPortraitLocked (set by the shake task, see
// ui/taskComposables.kt) by overriding supportedInterfaceOrientations.
final class RootHostingController: UIViewController {
    private let composeChild: UIViewController = {
        // Installed before MainViewController() runs, since AppContainer
        // (built inside it) hands out an AlarmScheduler that reaches through
        // this bridge to actually schedule anything. See AlarmKitBridge.swift.
        let bridge = AlarmKitBridge()
        bridge.requestAuthorizationIfNeeded()
        AlarmKitBridgeHolder.shared.bridge = bridge
        return MainViewControllerKt.MainViewController()
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(composeChild)
        composeChild.view.frame = view.bounds
        composeChild.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(composeChild.view)
        composeChild.didMove(toParent: self)

        // "Stop" on a ringing AlarmKit alarm always foregrounds the app
        // (AlarmStopIntent.openAppWhenRun = true) rather than relying on the
        // app already being alive to observe anything in real time, so this
        // re-checks the pending-navigation NSUserDefaults keys every time the
        // app becomes active, not just on cold launch. See
        // AlarmNotificationRouter.kt / AlarmKitBridge.swift.
        NotificationCenter.default.addObserver(
            forName: UIApplication.didBecomeActiveNotification,
            object: nil,
            queue: .main
        ) { _ in
            AlarmNotificationRouterKt.checkPendingAlarmNavigation()
        }
    }

    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        let isLocked = (OrientationLock.shared.isPortraitLocked.value as? Bool) ?? false
        return isLocked ? .portrait : .all
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        RootHostingController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
