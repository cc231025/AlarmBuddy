import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(17)

    // Desktop/JVM target: NOT shipped to users. It exists purely so the shared
    // Kotlin/Compose code can be compiled and run headlessly in any normal
    // Linux/Windows/Mac dev environment (no Xcode required) to catch mistakes
    // before they show up in the iOS-only CI build. See MIGRATION_PLAN.md.
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // Intel iOS Simulator (iosX64) deliberately left out: Compose Multiplatform
    // 1.12.0 no longer publishes an ios_x64 variant for compose.runtime/compose.ui
    // (Intel Macs are long discontinued, and GitHub's macos-15 runners -- like any
    // Mac sold since 2020 -- are Apple Silicon, so the simulator they build for is
    // ios_simulator_arm64, never ios_x64). Declaring iosX64() here made Gradle try
    // to resolve dependency variants for a target these libraries don't ship
    // anymore, which failed the *entire* build's dependency resolution -- not just
    // that target -- with a wall of "No matching variant" errors. iosArm64 (real
    // devices) + iosSimulatorArm64 (Apple Silicon simulator, including CI) are the
    // only targets actually needed.
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.alarmbuddy.MainKt"
    }
}

sqldelight {
    databases {
        create("AlarmBuddyDatabase") {
            packageName.set("com.example.alarmbuddy.db")
        }
    }
}
