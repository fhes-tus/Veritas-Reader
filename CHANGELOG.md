# Changelog

All notable changes to the Veritas Reader application will be documented in this file.

---

## [2.3.0] - 2026-09-07

### Added
*   **In-App Free Book Repositories & Downloader:** Full in-app browsing and automatic download interception for 5 global digital book repositories: *Project Gutenberg*, *Standard Ebooks*, *Open Library*, *ManyBooks*, and *Ocean of PDF*. Downloads (EPUB, PDF, TXT) are automatically sandboxed and imported directly into the Veritas reading library without leaving the app.
*   **Study Guide PDF Exporter:** Automatic generation and export of styled PDF study guides from flashcard sets, study notes, and document highlights.
*   **PDF Table of Contents (TOC) Extraction:** Interactive outline drawer allowing instant chapter and section navigation within PDF documents.
*   **Glance AppWidget Android 16 Redesign:** Rebuilt Flashcard, Study Dashboard, Quick Capture, and Player widgets with responsive Glance Column layouts, zero-latency touch callbacks, and multi-deck review support.

### Changed
*   **Sandboxed Web Security:** Enforced strict enterprise-grade sandbox policies across all in-app browsers (`allowFileAccess = false`, `allowContentAccess = false`, `safeBrowsingEnabled = true`, blocking non-HTTP/HTTPS schemes).
*   **Study Hub Navigation Pipeline:** Refactored home tab routing with dedicated `targetHomeTab` navigation, eliminating race conditions when jumping from AI Study Studio or home widgets directly into Study Hub.

### Fixed
*   **Library Tab Swipe Stabilization:** Eliminated circular state feedback oscillations between page state and navigation tab selections, and removed top bar height animations during horizontal swiping for smooth, glitch-free swiping.
*   **Flashcard Widget Touch Unresponsiveness:** Resolved `Null RemoteViews` errors on Android 16 / One UI 8 by eliminating AdapterView/RemoteViewsFactory dependencies, guaranteeing immediate touch responses for deck selection, card flipping, and grading.

---

## [2.2.0] - 2026-08-22

### Added
*   **Device-Wide File Discovery:** A MediaStore-backed index now finds compatible documents anywhere on shared storage in a single query, across every mounted volume including SD cards and USB OTG, with a deeper filesystem sweep behind it for files the media index does not cover.
*   **File Deletion from the Browser:** Long-press selection now offers deletion for one file or many, with a confirmation that names the files and states plainly that this removes them from phone storage rather than from Veritas alone.
*   **Delete Previews:** The confirmation shows a downsampled thumbnail and folder path for each image, since two photos can look alike and the path is often what identifies the right one.
*   **Broader Format Recognition:** Legacy PowerPoint (`.ppt`), macro-enabled Office documents (`.pptm`, `.docm`), `.xhtml`, HEIC/HEIF/AVIF photos, and a fuller plain-text set (`.log`, `.json`, `.xml`, `.yaml`, `.srt`, `.vtt`, and others) are now recognised by the browser, the system picker, and the launcher's "Open with" list.
*   **Idle Chrome Collapse:** The original-document view retires its toolbar and player bars after five seconds without a touch, and restores them on a tap.

### Changed
*   **Exact Page and Sentence Mapping:** Synchronisation between the reading position and the original-document view no longer scales reading progress across the page count. Both directions resolve through the document model, so the page shown is the page the sentence is actually on.
*   **Unified EPUB and DOCX Extraction:** Reading text for these formats is now produced by the same parsers the original view renders, so page markers and displayed pages derive from a single parse and cannot drift apart.
*   **Document Loading Split by Scope:** Parsing is performed once per document rather than repeated on every page turn, with page rendering and slide-image extraction handled separately.
*   **Rotation Preserves State:** Rotating no longer rebuilds the reader, so the current page, bar visibility, zoom, and view rotation survive the change.
*   **DOCX Table Reading:** Table cells are separated when read aloud instead of being run together into a single compound word.

### Fixed
*   **Neural Speech Engine Crash:** Stopping, seeking, or switching voice could free the speech engine while it was still generating audio, terminating the app from inside its native layer where the in-app crash reporter cannot observe it. Teardown now waits for generation to finish.
*   **Slide, Chapter and Page Highlighting:** The original-document view highlighted only the first line of every slide, chapter or page regardless of what was being spoken, and drifted onto the wrong page the further into a document playback ran.
*   **Original View Opening Page:** Switching to the original document opened on page 1 instead of the page being read.
*   **Page Swiping During Playback:** Swiping pages in the extracted-text view could hijack narration, and a page change arriving mid-scroll was discarded and never retried.
*   **Blank Pages:** Pages with no text of their own no longer move the reading position to a different page's sentence.
*   **Continue Reading From Here:** Selecting a sentence in the original view now begins from that sentence on that page, rather than restarting the document when no match was found.
*   **Sideways Pages After Rotating:** The rotate controls applied a page rotation on top of the screen rotation.
*   **Unrecoverable Full Screen:** Hiding the bars removed the only control that could bring them back on the PDF view; tapping the page now restores them.
*   **Slide Images Disappearing:** Embedded PPTX images were rebuilt on every page turn and vanished between slides.

### Removed
*   **Duplicate Document Parsing:** The separate EPUB and DOCX parsing paths inside the extractor have been retired in favour of the shared parsers, removing the possibility of the two views disagreeing.

---

## [2.1.0] - 2026-08-21

### Added
*   **Comprehensive Multi-Format Document Parsers:** Native offline parsing and text extraction for Microsoft Word (`.docx`), EPUB e-books (`.epub` 2 & 3 with chapter hierarchies), and legacy PowerPoint presentations (`.ppt`).
*   **RSVP Speed Reading Engine:** High-performance Rapid Serial Visual Presentation mode with customizable WPM (100–1000 WPM), dynamic punctuation pauses, and optimal recognition point (ORP) centering.
*   **Smart Resume Catch-Up:** Intelligent contextual re-orientation dialog summarizing preceding context when returning to a document after a hiatus.
*   **Study Guide PDF Exporter:** Instant generation of beautifully formatted PDF study guides compiling document highlights, tags, vocabulary notes, and annotations.
*   **Material You Home Screen Widgets:** Glance-powered and AppWidgetProvider widgets including Pinned Notes, Quick Audio Capture, Quick Note, Reading Streak/Progress, and Study Dashboard.
*   **Classics & Public Domain Catalog:** In-app browser for curated classic literature and direct reading list import.
*   **Voice Notes Studio:** Built-in voice note recording and dictation tools directly integrated within the notes editor.

### Changed
*   **Web Article Extractor Hardening:** Realistic browser request headers (`User-Agent`, `Accept`, `Accept-Language`), dynamic charset detection from HTTP headers and `<meta>` tags, and rich HTML sanitization.
*   **Background Playback Resilience:** Added OEM battery optimization whitelist settings helper to protect background TTS playback from aggressive task killers.

### Fixed
*   **PDF Password & Encryption Handling:** Wrapped PDFBox and Android `PdfRenderer` with graceful error handling and informative notifications for password-protected documents.
*   **Kokoro Neural TTS Memory Guard:** Added low-memory pre-checks preventing native OOM crashes on memory-constrained devices by gracefully falling back to lightweight TTS engines.
*   **R8 / ProGuard Obfuscation Hardening:** Comprehensive keep rules safeguarding all serialized models, WorkManager Workers, CrashReporter, and Glance widget receivers in release builds.
*   **Neural Playback Teardown Race:** Pausing, stopping, seeking, or switching voice mid-sentence could release the `AudioTrack` while the synthesis thread was still writing into it — a crash inside the native audio layer that the app's own exception handler could never report. Teardown now parks the track with `pause()`/`flush()` and defers the release to the writer, so a stop is always safe no matter where playback is.
*   **Playback Stop Responsiveness:** The neural playback wait loop read its stop flag without a memory barrier, so a stop issued from the UI thread could go unseen and leave the sentence running to its timeout. The flag is now `@Volatile`.
*   **Audio Engine Scope Leak:** Each voice change and every service teardown left the previous audio buffer's coroutine scope alive for the rest of the process. `shutdown()` now cancels the scope instead of only its children.
*   **Look-Ahead Window Bounds:** The pre-buffer window could be computed with negative indices while reading a text selection, wasting synthesis passes on positions that do not exist in the document.
*   **Stale Version Fallbacks:** The update checker and the Settings "About" panel fell back to a hardcoded version string that had to be edited by hand each release. Both now read `BuildConfig.VERSION_NAME`.
*   **Unused Room Dependency:** Removed the declared-but-unreferenced `androidx.room:room-ktx` dependency and its ProGuard keep rules; persistence is entirely SharedPreferences and JSON.

### Removed
*   **Ambient Soundscapes:** Removed the ambient sound engine and its five bundled loop recordings. The feature had no reachable entry point in the interface, and the uncompressed WAV loops accounted for 23% of the release APK.
*   **Bouncy Castle Post-Quantum Tables:** Excluded the unused Picnic post-quantum signature parameter resources pulled in transitively by PDFBox's certificate handling.

---

## [2.0.0] - 2026-08-19

### Added
*   **Preloaded Classic Library:** Bundled the complete, formatted story and lessons of *"Who Moved My Cheese?"* by Spencer Johnson, M.D., with high-resolution cover artwork as the default book on install.
*   **Revamped Onboarding & Guided App Tour:** Interactive onboarding carousel with voice-assisted descriptions, celebratory animations, and a guided app tour that navigates and opens the Notes Studio, AI Study Hub, and Settings Hub.
*   **Persistent Onboarding Quest Checklist:** Floating mission checklist tracking core app milestones (guided tour, import, voice customization, bookmarks) with touch passthrough and persistent progress across app restarts.
*   **Intelligent Cover Extraction Engine:** Multi-page cover candidate scoring with aspect-ratio validation, text-presence verification, and automatic cover image repair for PDFs, EPUBs, DOCX, and PPTX documents.
*   **On-Device Neural TTS & Sherpa-ONNX Engine:** Integrated high-fidelity neural speech synthesis powered by Sherpa-ONNX, Kokoro, and Piper with streaming audio buffers and background media session sync.
*   **Custom Typography & OFL Fonts:** Bundled Atkinson Hyperlegible, Bitter Variable, Literata Variable, and Lora Variable fonts for dyslexia-friendly and long-form reading comfort.
*   **ABI Split Packaging:** Configured architecture-specific release APKs (`arm64-v8a`, `armeabi-v7a`, and `universal`), reducing the standard 64-bit release APK down to ~54 MB.

### Changed
*   **Library Home Architecture:** Refactored hero card continue-reading carousel and recent documents grid with dynamic cover aspect ratios and golden glow focus rings.
*   **Navigation & Spotlight Lifecycle:** Enhanced spotlight overlays to seamlessly coordinate across root composables and separate Android window dialogs (Settings and Insights).
*   **Speaker Announcement Polishing:** Streamlined and refined voice-guided tutorial speech prompts for clear, non-repetitive walkthroughs.

### Fixed
*   **Quest Touch Interception:** Fixed gesture interceptor bug preventing clicks from reaching the floating mission checklist.
*   **Guided Tour Condition:** Fixed lifecycle bug where the interactive tour would fail to launch when onboarding was previously completed.
*   **ProGuard / R8 JNI Stripping:** Added comprehensive keep rules for Sherpa-ONNX, Kokoro, and native TTS classes in release builds.

---

## [1.1.0] - 2026-07-07

### Added
*   **PPTX Document Import & Extraction:** Added full support for importing and extracting text from PowerPoint presentations (`.pptx`) in both the Android app and KMP desktop client.
*   **Background Auto-Backups:** Integrated a scheduled background service using WorkManager to auto-backup library documents and notes.
*   **Daily Streak Reminders:** Added local notifications to remind users to maintain their reading habits.
*   **In-App Self-Updater:** Added automatic check-for-updates and post-update release notes popup.
*   **Background TTS Battery Exemption:** Added a request/settings hook for battery optimization exemptions to prevent Android from killing background TTS playback.
*   **Highlight Customization & AI Prompts:** Highlight coloring and grouping, selection sharing scopes, and advanced AI assistant prompts with page markers.

### Changed
*   **Vibrant Hero Card Toggle:** Added a toggle to settings to switch between subtle and vibrant accent gradient styles on the main dashboard.
*   **Overhaul & Reliability:** General performance improvements in rendering, theming, and layout animations.

### Fixed
*   **Share-Import NPE:** Resolved a crash occurring when importing from specific external share providers.
*   **Version Comparison Logic:** Corrected issues with parsing and comparing version strings for update prompts.

---

## [1.0.1] - 2026-06-16

### Added
*   **Resilient JSON Storage:** Integrated automatic backup files (`__bak` copies) for saved documents and notes. If a write operation is interrupted or corrupted, the app auto-recovers from the last-known-good backup instead of data vanishing.
*   **Inline Markdown Transforming:** Hidden markdown syntax characters are styled dynamically inline (Bold, Italic, Monospace, Headers).
*   **Remap Annotations on Edit:** An alignment algorithm that re-anchors bookmarks, highlights, and notes by matching sentence text when editing a document.
*   **General Note Reminders:** Exact alarm scheduling for general notes (using `SCHEDULE_EXACT_ALARM`) with automatic runtime fallbacks to inexact alarms when permission is restricted.
*   **Habit Tracker History:** Swipeable weekly reading statistics displaying an 8-week history of daily reading totals.
*   **Dark PDF Reading:** Custom color-matrix inversion filter for comfortable document viewing in dark mode.

### Changed
*   **UI Assets:** Replaced hardcoded text-based icons (`✕`, `⋮`, `✓`, `★`) with native Material 3 vector drawables.
*   **IME Keyboard Padding:** Added soft keyboard offset buffers to note editing sheets to prevent inputs from being obscured.

### Fixed
*   **Editor Caret Alignment:** Implemented bidirectional offset mapping (fixes cursor positioning jumps when navigating formatted rich text).
*   **Intent Restart Loop:** Cleared incoming stream intents after parsing to prevent infinite import loops during layout rotations or activity restarts.
*   **Spotlight Overlay Ordering:** Fixed onboarding spotlight overlays displaying behind popup dialogs on the insights page.
*   **Thread Safety:** Added `LIBRARY_WRITE_LOCK` to synchronize shared preference library updates between the UI thread and background media session thread.

---

## [1.0.0] - 2026-06-15

### Added
*   Initial stable release.
*   **Universal Document Import:** Support for TXT, EPUB, DOCX, PDFs, scanned pages, and web articles.
*   **Smart Text Extraction:** Text parsing via PDFBox, Android PDF rendering, and ML Kit OCR fallbacks.
*   **Text-to-Speech Playback:** Background media session integration, media buttons, Bluetooth/headset hook bindings, and custom pitch/rate controls.
*   **Native PDF Viewer:** Jetpack PDF fragment integration for rendering original page styles.
*   **Reading Habits Tracker:** Daily streak tracking and monthly session analysis.
*   **Dynamic Theming:** Deep theme sets including Material You, Liquid Glass, One UI style, and High Contrast.
*   **Home Widgets:** Playback control and cover widgets.
*   **ProGuard Optimizations:** Configured optimization rules for production builds.
