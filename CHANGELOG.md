# Changelog

All notable changes to the Veritas Reader application will be documented in this file.

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
