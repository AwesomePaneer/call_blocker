# Prefix Call Blocker

A tiny Android app that **silently rejects incoming calls whose number starts
with a prefix you choose** (for example `+1800` or `+91140`) — **without
replacing your dialer**. Your existing phone/dialer app stays exactly as it is;
this app only decides whether to reject an incoming call.

- No ring and no notification for blocked calls — but they still appear in your
  call log, so you can see what was blocked.
- **Saved contacts are never blocked**, even if their number matches a prefix.
- No ads, no analytics, no network access, no dangerous permissions.
- Event-driven: nothing runs in the background. Android invokes the app only for
  the moment it decides on an incoming call, and it keeps working after a reboot
  with no need to open the app.

---

## Download and install (prebuilt APK)

The easiest way to install — no building required:

1. Open the [**Releases**](../../releases) page and download the latest
   `prefix-call-blocker-vX.Y.apk`.
2. On your Android phone, tap the downloaded APK. The first time, Android asks
   you to allow the app you're installing *from* (Files / browser) to install
   unknown apps — allow it, then tap the APK again.
3. Open **Prefix Call Blocker**, tap **Enable call screening**, and grant the
   role. The status at the top should read **Call screening: Active**.
4. Type a prefix (for example `+91140`) and tap **Add**. Done.

> The release APK is signed with a project convenience key (not a secret
> production key), so it installs by sideloading and updates cleanly across
> versions. Prefer to build it yourself? See [Build](#build) below.

---

## How it works

Android has a **call-screening role**. Whichever app holds it receives a
callback on each incoming call and can allow or reject it. This app implements
that callback (`CallScreeningService`): it normalizes the incoming number,
checks it against your saved prefixes, and rejects on a match — otherwise it
does nothing and the call proceeds normally. Prefixes are stored locally in
`SharedPreferences`; there is no server and no account.

> **Note:** the call-screening role can be held by **only one app at a time**.
> Enabling this app makes it the active call-screening app, replacing whichever
> app currently holds that role. If your device or carrier provided a call
> screening / spam-filtering app, that app's screening will be inactive while
> this app holds the role. You can switch back at any time from
> **Settings → Apps → Default apps → Caller ID & spam app** (the exact wording
> varies by device).

---

## Matching rules

- A call is blocked if the incoming number **starts with** any prefix you saved.
  `+91140` blocks every number beginning with `+91140`. There are no wildcards
  inside the number — leading-prefix matching only, which keeps it predictable.
- Numbers and prefixes are normalized the same way before comparison: a single
  leading `+` is kept, digits are kept, and spaces / dashes / parentheses are
  removed. No country-code guessing is performed.
- If your carrier delivers a number **without** the leading `+`/country code, a
  prefix stored as `+91140` will not match it — add the alternate form as a
  second prefix if you observe that.
- Empty prefix list → nothing is blocked.
- **Fail-open by design:** if anything is uncertain (unknown number, error), the
  call is allowed. The app will not block a call because of an internal error.

---

## Permissions

**None requested from the user.** The app declares no `<uses-permission>` entries:

- The screening service is guarded by the system permission
  `BIND_SCREENING_SERVICE` (declared on the service, never requested from the
  user).
- The screening **role** is obtained via `RoleManager`, which is a role grant,
  not a runtime permission.
- No internet, contacts, call-log, or phone-state permissions.

Android only invokes the screening callback for numbers that are **not** in the
user's contacts, which is why saved contacts are exempt without the app ever
reading contacts.

---

## Build

The project builds from the command line with the Gradle wrapper — no Android
Studio required.

**Requirements**

- **JDK 17**
- **Android SDK** with `platform-tools`, `platforms;android-35`, and
  `build-tools;35.0.0`
- Targets: `minSdk 29` (Android 10, first version with the call-screening role),
  `compileSdk` / `targetSdk 35` (Android 15)

**Point the build at your SDK** — either export environment variables:

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
```

…or create a `local.properties` file in the project root (git-ignored):

```properties
sdk.dir=/path/to/android-sdk
```

**Build the debug APK**

```bash
./gradlew assembleDebug
```

Output:

```
app/build/outputs/apk/debug/app-debug.apk
```

The debug APK is auto-signed with the debug keystore, which is fine for personal
sideloading. No release keystore is needed unless you want store distribution.

**Run the unit tests**

```bash
./gradlew testDebugUnitTest
```

---

## Install and use

1. Copy `app-debug.apk` to an Android phone and open it. The first time, Android
   asks you to allow the installing app (Files / browser) to install unknown
   apps — allow it, then tap the APK again.
2. Open **Prefix Call Blocker**.
3. Tap **Enable call screening** and grant the role when prompted. The status at
   the top changes to **Call screening: Active**.
4. Type a prefix (for example `+91140`) and tap **Add**.

Calls whose number starts with a saved prefix are now silently rejected. Tap
**Delete** next to a prefix to stop blocking it. To hand the screening role back
to another app, use **Settings → Apps → Default apps → Caller ID & spam app**.

---

## Project structure

```
├── settings.gradle.kts
├── build.gradle.kts            # root
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/…
└── app/
    ├── build.gradle.kts        # android app module (no runtime dependencies)
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/prefixcallblocker/app/
        │   │   ├── PrefixCallScreeningService.kt   # the screening callback
        │   │   ├── BlockDecision.kt                # pure, unit-tested logic
        │   │   ├── PrefixStore.kt                  # SharedPreferences + normalize
        │   │   └── MainActivity.kt                 # minimal UI
        │   └── res/…
        └── test/…                                  # JVM unit tests
```

---

## Scope

This is a companion screening app, not a dialer. It does not provide a dialpad,
caller ID, spam database, cloud lookups, SMS blocking, accounts, ads, or
analytics. It does one thing: reject incoming calls that start with a prefix you
define.
