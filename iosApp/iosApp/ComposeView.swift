import SwiftUI
import UIKit
import ComposeApp

// Thin UIKit wrapper around the shared Kotlin/Compose UI (MainViewController()
// in composeApp/src/iosMain/.../MainViewController.kt), plus the one bit of
// glue Compose Multiplatform can't express itself: honoring
// OrientationLock.isPortraitLocked (set by the shake task, see
// ui/taskComposables.kt) by overriding supportedInterfaceOrientations.
final class RootHostingController: UIViewController {
    private let composeChild = MainViewControllerKt.MainViewController()

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(composeChild)
        composeChild.view.frame = view.bounds
        composeChild.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(composeChild.view)
        composeChild.didMove(toParent: self)
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
