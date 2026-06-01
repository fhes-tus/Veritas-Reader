# Final APK Readiness And QA

Current version: `0.3.7-beta-release-hardening`
Package: `com.veritas.reader`
Minimum Android: API 28
Target Android: API 36

## Current Readiness

- Project opens as an Android Studio Gradle project.
- Gradle wrapper files are present.
- App source and resources are present under `app/src/main`.
- Dependency declarations are in `app/build.gradle.kts`.
- Java runtime is pinned to Android Studio's embedded JDK on this machine to avoid the old Java 8 PATH runtime.
- The latest code and unit-test check passed with:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --rerun-tasks
```

This pass verifies the sentence/part reader refactor, PDF viewer sync changes, and refreshed debug APK.

## Latest QA Result

- Balanced part planning is covered by unit tests, including `25 -> 9 / 8 / 8` and `987 -> 83 balanced parts`.
- Sentence anchoring is covered by unit tests for selecting text inside a part and resolving it to the containing sentence.
- Debug APK build completed successfully after a forced rebuild.
- Current warnings are non-blocking existing warnings: one unnecessary safe call in `AudioExportManager.kt` and deprecated status/navigation bar color setters in `VeritasPdfViewerActivity.kt`.
- Second-pass reader corrections completed: sentence-follow scrolling inside the current part, local selection toolbar placement, restored lower deck sentence controls, haptic/visual feedback for text actions, safer Smart Outline caps for large books, option-one PDF `Read from here` copy-menu detection, and per-page column-aware PDF extraction.
- PDF import optimization completed: text-based PDFs open after the foreground import window with ready pages, avoid the hard 1-minute failure path, and continue full extraction in the background. OCR-only work still has a responsiveness guard so scanned files cannot trap the app.
- Latest correction pass completed: per-page PDF column detection now uses normal PDFBox spacing for one-column pages and region-based left/right extraction for clear two-column pages, so it fixes column reading order without rebuilding words from raw glyphs.
- Native extracted-text selection completed: selection uses Android text handles, can span multiple sentences, places the action menu near the selected text, actions apply to all touched sentences, extracted-text editing is scoped to selected sentence ranges or the current part, and PDF `Read from here` searches the current PDF page before nearby pages for short/common selections.

## Debug APK Output

Expected debug APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Clean rebundle APK path:

```text
apk/VeritasReader-android-debug.apk
```

## Final QA Checklist

Run this checklist before sharing the APK beyond a quick internal test.

### Install And Launch

- Install the APK on a real Android device.
- Launch Veritas Reader from the app icon.
- Confirm the app opens without crashing.
- Grant notification permission when prompted.

### Import

- Import or share plain text into the app.
- Import a normal text-based PDF.
- Import a scanned/image-based PDF and confirm OCR either extracts text or shows a useful limitation message.
- Import a DOCX file.
- Import an EPUB file.
- Import an image with visible text.
- Import a web article URL.

### Library

- Confirm imported readings appear in the saved library.
- Open a saved reading.
- Add and remove a favorite.
- Add and remove a Listen Later queue item.
- Confirm progress is saved after moving through sentences.
- Clear continue/progress for a saved reading.

### Playback

- Start reading aloud from the main reader.
- Pause and resume playback.
- Use next and previous sentence controls.
- Use previous-part and next-part controls in the extracted text lower deck.
- Confirm the extracted text reader shows one continuous page for the current part.
- Confirm the end-of-part marker `(Part X of Y)` appears at the end of the part and is not read aloud.
- Double-tap a sentence and confirm the reader follows that sentence instead of jumping to the end of the part.
- Select a word and confirm the action toolbar appears near the selected sentence, not at the end of the part.
- Confirm background notification appears.
- Confirm notification controls pause, play, next, previous, and stop.
- Confirm Bluetooth/headset controls work if available.
- Change speed and pitch and confirm playback updates.
- Change TTS voice and confirm the voice setting applies.

### PDF Viewer

- Open a saved PDF in actual PDF viewer mode.
- Confirm the PDF renders.
- Swipe or navigate between pages.
- Toggle sync and confirm checking it jumps back to the page currently being read.
- Scroll away from the synced reading page and confirm sync turns off.
- Tap only in the top area of the PDF viewer and confirm decks show/hide without triggering during normal scrolls.
- Use search.
- Rotate the viewer.
- Select text in the PDF and tap `Read from here`.
- Confirm playback starts from the containing extracted-text sentence while staying in the PDF viewer.
- Confirm clipboard content is restored after the action.
- For this build, confirm option-one PDF `Read from here` works before adding any fallback/custom copy flow.

### Notes And Reader Tools

- Add a bookmark.
- Add a highlight.
- Add a sentence note.
- Use native text selection in the extracted text view and confirm bookmark, highlight, note, AI, share, copy, search, read-aloud, edit selected text, and read-from-here actions work on every touched sentence or the exact selected text as appropriate.
- Double-tap a sentence in the extracted text view and confirm reading continues from that sentence.
- Add a document-level note.
- Copy selected text.
- Share selected text.
- Send selected text to translation or AI handoff if a target app is installed.

### Export And Widgets

- Export a reading to WAV and confirm a playable file is created.
- Add the playback widget if the launcher supports widgets.
- Confirm widget playback controls update with the current reading.
- Add the cover/current-reading widget if supported.

### Settings And Visuals

- Change theme pack.
- Change reader text size.
- Check portrait and landscape behavior.
- Check a small phone viewport if possible.
- Confirm visible text does not overlap major controls.

### Negative Cases

- Try a PDF with no extractable text and confirm the app does not crash.
- Try removing a document while it is active and confirm playback stops or recovers safely.
- Reopen a very large saved PDF, ideally 500+ pages, and confirm the library open path does not crash.
- Open Smart Outline on a very large saved PDF and confirm it opens without crashing.
- Try sharing to an unavailable AI/translation target and confirm the fallback behavior is acceptable.
- Deny notification permission and confirm foreground playback still handles the limitation gracefully.

## Release Blockers Before Public Distribution

- Create a release-signed APK or Android App Bundle.
- Replace this draft privacy policy with final approved policy text.
- Confirm Play Store target SDK, foreground service, and data safety declarations.
- Perform a fresh QA pass on at least one physical phone.
- Decide whether the machine-specific `org.gradle.java.home` should remain for distribution or be removed for portability.

## Notes

The current clean source package intentionally excludes generated/editor folders such as `.gradle`, `.kotlin`, `.idea`, and `app/build`. Android Studio recreates them during sync/build.
