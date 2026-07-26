plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.prefixcallblocker.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.prefixcallblocker.app"
        minSdk = 29          // Android 10 — first version with the call-screening role
        targetSdk = 35       // Android 15
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        // Convenience signing key committed to the repo (release.keystore) so
        // CI-built release APKs are signed consistently and users can update
        // across versions without uninstalling. This is NOT a secret production
        // key — for a sideloaded FOSS utility that trade-off is intentional.
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "callblocker"
            keyAlias = "callblocker"
            keyPassword = "callblocker"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // No runtime dependencies. This app uses only the Android framework —
    // no AndroidX, no Jetpack Compose, no third-party SDKs (see SPEC §2, §3, §9).

    // Test-only: JUnit for the pure-JVM unit tests (not shipped in the APK).
    testImplementation("junit:junit:4.13.2")
}
