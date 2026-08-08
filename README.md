# VoicePilot — Native Android App

## Fastest way to get the .apk — no computer software needed

You can build the real `.apk` in the cloud using GitHub, entirely from your
phone's browser:

1. Go to **github.com** and make a free account (if you don't have one).
2. Tap **+** → **New repository**. Name it `VoicePilot`. Create it.
3. Tap **Add file → Upload files**, and upload every file/folder from this
   project (keep the folder structure — the easiest way is uploading the
   whole unzipped `VoicePilot` folder).
4. Commit the upload. GitHub will automatically start the build (the file
   `.github/workflows/build.yml` in this project tells it what to do).
5. Tap the **Actions** tab at the top of your repository. You'll see a
   build running (a yellow dot) — wait for it to turn into a green
   checkmark, usually 3–5 minutes.
6. Tap into that finished build, scroll down to **Artifacts**, and tap
   **VoicePilot-apk** to download it. That's your real, installable `.apk`.
7. Open the downloaded file on your phone and tap **Install** (Android will
   ask you to allow "install from this source" once — allow it).

No Android Studio, no computer required for this path.

## Alternative: build it yourself in Android Studio

This is a real, working Android Studio project. It uses your phone's actual
speech recognizer and actually launches other installed apps (WhatsApp,
Instagram, Chrome, etc.) — no backend, no server, 100% on-device.

## How to turn this into an installed APK on your phone (free)

1. **Install Android Studio** (free): https://developer.android.com/studio
2. Open Android Studio → **Open** → select this `VoicePilot` folder.
3. Let Gradle finish syncing (first time takes a few minutes — it downloads
   build tools, not app data; still no backend involved).
4. Plug your Android phone in via USB (enable *Developer Options* →
   *USB debugging* on the phone), or use an emulator.
5. Click the green **Run ▶** button. Android Studio installs the app
   straight onto your phone.
6. To get a shareable `.apk` file instead: **Build → Build Bundle(s) / APK(s)
   → Build APK(s)**. The `.apk` appears in
   `app/build/outputs/apk/debug/app-debug.apk` — copy that file to your
   phone and tap it to install (you'll need to allow "install from unknown
   sources" once).

## How it works

- `MainActivity.kt` — asks for microphone permission, starts Android's
  built-in `SpeechRecognizer`, and shows what it heard.
- `AppMatcher.kt` — matches what you said ("open whatsapp", "insta") against
  a small built-in list of common apps, and falls back to searching every
  app actually installed on your phone.
- When a match is found, it calls
  `packageManager.getLaunchIntentForPackage(...)` and `startActivity(...)` —
  this is the real Android API for opening another app.

## Adding more apps

Open `AppMatcher.kt` and add a line to `AppDatabase.apps`, e.g.:

```kotlin
AppEntry("Amazon", "com.amazon.mShop.android.shopping", listOf("amazon shopping"))
```

You don't strictly need to — if an app isn't in the list, VoicePilot already
searches your phone's installed apps by name automatically.

## Notes

- `RECORD_AUDIO` permission is required and requested at runtime.
- `INTERNET` permission is declared only because Android's default speech
  engine may use an online model to transcribe speech — VoicePilot itself
  has no server, no analytics, no account system.
- This targets Android 7.0+ (minSdk 24), which covers the vast majority of
  phones in use today.
