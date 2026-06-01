# APK Sharing Instructions

This project currently produces a debug APK for testing and direct sharing. It is not a Play Store release build.

## Build The APK

From Android Studio:

```text
Build > Build APK(s)
```

From PowerShell in the project folder:

```powershell
.\gradlew.bat :app:assembleDebug
```

The APK will be created here:

```text
app/build/outputs/apk/debug/app-debug.apk
```

In the clean rebundle, the shared APK is placed here:

```text
apk/VeritasReader-android-debug.apk
```

## Share The APK

Send only the APK file if the recipient just wants to install and test the app. Send the full rebundle zip if the recipient also needs the source code.

Good sharing options:

- USB cable transfer.
- Nearby Share or Quick Share.
- Cloud drive link.
- Messaging app that does not rename or compress APK files.

If a messaging app blocks APK files, zip the APK first or share the full rebundle zip.

## Install On Android

1. Transfer the APK to the Android device.
2. Open the APK from Files or Downloads.
3. If Android blocks installation, allow installs from that specific app under `Install unknown apps`.
4. Install `Veritas Reader`.
5. Open the app and grant notification permission if prompted. Notification permission is needed for foreground/background playback controls on modern Android.

## Install With ADB

If Android platform tools are installed:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

For the rebundle APK:

```powershell
adb install -r apk\VeritasReader-android-debug.apk
```

## Before Sharing

- Confirm the APK was built after the latest source changes.
- Confirm the app opens on a physical Android device or emulator.
- Confirm a PDF import opens in the PDF viewer.
- Confirm `Read from here` works from selected PDF text.
- Confirm background playback notification appears and controls playback.
- Confirm the recipient understands this is a debug/testing APK, not a signed Play Store release.

## Release Note

For Play Store or wider public distribution, create a proper release build with a release signing key, final privacy policy, versioning review, and a fresh QA pass.
