# Veritas Reader Android

Veritas Reader is an Android reading and listening app for imported documents. It turns text, PDFs, DOCX files, EPUBs, images, scanned PDFs, web articles, and shared text into a saved reading library with text-to-speech playback, notes, reader tools, and an actual PDF/document view.

Current version: `0.3.7-beta-release-hardening`
Package: `com.veritas.reader`
Minimum Android: API 28
Target Android: API 36
Compile SDK: API 36.1

## Current App State

- Import TXT/text shares, PDF, DOCX, EPUB, image files, scanned PDFs, and web links.
- Extract readable document text with PDFBox, Android PDF rendering, and ML Kit OCR fallback where available. Text-based PDFs open after the foreground import window with ready pages, finish longer extraction in the background, and use per-page column detection so mixed one-column/two-column books keep readable order and spacing.
- Save readings locally with favorites, Listen Later queue, collections, recent progress, sentence notes, sentence bookmarks, sentence highlights, and document-level notes.
- Read aloud with Android Text-to-Speech, background media notification, headset/Bluetooth controls, media session support, queue playback, speed/pitch controls, voice selection, and pronunciation rules.
- Read extracted text as balanced document parts. Parts are page-based, native text selection can span multiple sentences, actions anchor notes/highlights/bookmarks/read-from-here to touched sentences, and double-tapping a sentence starts reading there.
- Use the actual PDF viewer for stored PDFs, including page sync, sentence highlight, search, rotation, playback controls, voice menu, top-zone deck toggling, and `Read from here` from selected PDF text.
- Use the legacy actual document canvas for fallback PDF/image viewing with paging, zoom, rotation, and playback controls.
- Send selected text or generated prompts to AI/share targets and translation apps. External apps receive only the text the user explicitly sends.
- Export readings to WAV through the installed Android TTS engine.
- Use Veritas home-screen widgets for playback and current-reading cover controls.
- Theme the reader with Material You, Veritas, Liquid Glass, One UI-style, and high-contrast options.

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
