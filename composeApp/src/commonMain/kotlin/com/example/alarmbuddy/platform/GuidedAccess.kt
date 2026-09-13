package com.example.alarmbuddy.platform

// Guided Access (Settings -> Accessibility -> Guided Access) is the closest
// thing iOS has to Android's "can't leave the app / can't touch the volume
// buttons" lock, and it's entirely free -- no Developer Program, no Apple
// approval. There is no public API to *turn it on* from inside an app (that
// would defeat the point of it being a deliberate, user-initiated lock), but
// there is a public, documented API to check whether it's *currently active*,
// which is enough to nag the user into turning it on themselves before an
// armed alarm is due. See MIGRATION_PLAN.md.
expect fun isGuidedAccessEnabled(): Boolean

// Opens the Settings app (iOS actual) so the user can find Accessibility ->
// Guided Access, or Accessibility -> Accessibility Shortcut, themselves.
// There is no public deep link straight to a specific Accessibility sub-page
// (only your own app's settings page is linkable), so this intentionally
// just opens the top-level Settings app and relies on the in-app walkthrough
// text to say where to tap next.
expect fun openSystemSettings()
