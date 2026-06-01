# Veritas Reader Android

Veritas Reader is an Android reading and listening app for imported documents. It turns text, PDFs, DOCX files, EPUBs, images, scanned PDFs, web articles, and shared text into a saved reading library with text-to-speech playback, notes, reader tools, and an actual PDF/document view.

Current version: `0.3.7-beta`
Package: `com.veritas.reader`
Minimum Android: API 28
Target Android: API 36
Compile SDK: API 36.1

## Current App State

- **Universal Document Import:** Import TXT, PDFs, DOCX, EPUB, image files, scanned PDFs, and web links.
- **Smart Text Extraction:** Extract readable document text with PDFBox, Android PDF rendering, and ML Kit OCR fallback where available. Mixed one-column/two-column books keep readable order and spacing.
- **Text-to-Speech Playback (TTS):** Read aloud with Android TTS, background media notification, headset/Bluetooth controls, media session support, queue playback, speed/pitch controls, and deep Voice Management.
- **Granular Annotations & Study Assistant:** Save readings locally with favorites, Listen Later queue, collections, recent progress, and sentence-level notes, bookmarks, and highlights.
- **Native PDF Viewer:** Read your PDFs exactly as they look, equipped with page sync, highlighting, search, and native playback controls.
- **Daily Streak Tracking:** Keep your reading habits strong with built-in daily tracking that monitors and records your longest and current reading streaks.
- **Audio Export:** Convert and export your readings into standard WAV audio files to listen to anywhere.
- **Sleep Timer:** Set an automated sleep timer to stop playback automatically.
- **Reading Lists:** Organize your library by creating custom reading lists and listen-later queues.
- **Home Screen Widgets:** Control playback and view your current reading cover directly from your Android home screen.
- **AI & Translation Integration:** Send selected text or generated prompts to AI/share targets and translation apps. External apps receive only the text the user explicitly sends.
- **Dynamic Theming:** Deep UI customization including Material You, Liquid Glass, One UI-style, and High Contrast themes.

## Build

Open this folder in Android Studio and let Gradle sync. Then use:

```text
Build > Build APK(s)
```

Command-line debug build:

```powershell
.\gradlew.bat :app:assembleDebug
```

Full verification used for the current APK:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --rerun-tasks
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

This project pins Gradle to Android Studio's embedded JDK on this machine in `gradle.properties` to avoid the old Java 8 runtime on PATH. If the project is moved to another Windows PC and Android Studio is installed somewhere else, update or remove `org.gradle.java.home` in `gradle.properties` and let Android Studio choose its embedded JDK.

## Project Layout

- `app/src/main/java/com/veritas/reader/` - Kotlin app source.
- `app/src/main/res/` - Android resources, widgets, icons, themes, and XML config.
- `app/build.gradle.kts` - app module build and dependency declarations.
- `gradle/wrapper/` plus `gradlew.bat` - Gradle wrapper files needed for command-line builds.
- `KNOWN_LIMITATIONS.md` - current technical and product limits.
- `PRIVACY_POLICY.md` - current privacy policy.

Generated folders such as `.gradle`, `.kotlin`, `.idea`, and `app/build` are intentionally not part of the clean source bundle. Android Studio and Gradle recreate them automatically.
