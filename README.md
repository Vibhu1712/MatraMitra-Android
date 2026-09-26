# मात्रा मित्र — Android app

A small offline Android app that wraps the Matra Mitra web page in a WebView.
Everything (page, fonts) is bundled in the APK, so it works without internet.
Speech uses the phone's own text-to-speech engine.

## Option A: build in the cloud with GitHub (no Android Studio needed)

1. Create a new GitHub repository and upload everything in this folder
   (keep the hidden `.github` folder).
2. Open the repository's **Actions** tab. The "Build APK" workflow runs on every push
   (or run it by hand with "Run workflow").
3. When it finishes (about 3–5 minutes), open the run and download
   **matra-mitra-apk** under Artifacts. Unzip it to get `app-debug.apk`.

## Option B: build with Android Studio

1. Install Android Studio, then File → Open → choose this folder.
2. Let Gradle sync finish (first time downloads Gradle 8.7 and Android SDK 34).
3. Build → Build App Bundle(s) / APK(s) → Build APK(s).
   The file appears in `app/build/outputs/apk/debug/app-debug.apk`.

From a terminal with the Android SDK installed, `./gradlew assembleDebug` does the same.

## Installing on the phone

Copy `app-debug.apk` to the phone and tap it. Android will ask you to allow
installs from that app (Files, Chrome, WhatsApp, etc.) — allow it once.

## Hindi voice

If letters aren't spoken, the app shows a "Voice settings" button. On most phones:
Settings → Accessibility (or System → Languages) → Text-to-speech output →
Preferred engine: **Speech Services by Google** → ⚙ → Install voice data → **Hindi (India)**.

## Updating the content

Edit `app/src/main/assets/index.html` and rebuild. Raise `versionCode` in
`app/build.gradle` so the phone installs it as an update over the old one.
