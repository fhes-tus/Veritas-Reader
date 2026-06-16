# Veritas Reader Android

[![Android API Target](https://img.shields.io/badge/Target-Android%2016%20(API%2036)-green.svg)](https://developer.android.com/)
[![Android API Min](https://img.shields.io/badge/Min%20SDK-API%2028-blue.svg)](https://developer.android.com/)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.4.0-purple.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#)

Veritas Reader is a modern, high-fidelity Android reading and text-to-speech (TTS) application. It transforms imported documents—including TXT, PDFs, DOCX, EPUBs, images, scanned pages, and web articles—into a centralized, personal reading library equipped with background media session playback, markdown-enabled notes, study tools, and habit metrics.

---

## 📱 Core Features

### 1. Document Import & Text Extraction
*   **Multi-Format Support:** Import local EPUB, DOCX, PDF, and TXT files, or extract content from web links.
*   **Hybrid OCR & Text Extraction:** Uses a hybrid extraction system powered by PDFBox, native Android PDF rendering, and Google ML Kit OCR to parse content, preserve formatting, and identify column structures.
*   **Text Encoding Options:** Choose auto-detect, Unicode (UTF-8), or legacy encodings (e.g. windows-1256, Shift_JIS) before saving imported text.
*   **Asynchronous Import Queue:** Background file parsing is handled via Android WorkManager to prevent main-thread UI blockages during large file imports.

### 2. Audio Playback & Text-to-Speech Engine
*   **Media3 Session Service:** Integrates Jetpack Media3 (`MediaSessionService`) to provide standard system notifications, lock screen widgets, and Bluetooth/headset media controls.
*   **Voice Management:** Deep voice configurations allowing speed, pitch, engine preference, and accent selections.
*   **Audio Output Safety:** Auto-pauses or ducks speech during audio focus changes (e.g., incoming phone calls) and headphone disconnects.
*   **Headset Control Mapping:** Map wired, Bluetooth, and media-button actions to reading controls.
*   **Audio Export:** Convert processed texts into standard WAV audio files for offline listening.
*   **Sleep Timer:** Auto-stop playback with section-end boundaries so audio never halts mid-sentence.

### 3. Study Tools & Markdown Annotations
*   **Inline Markdown Notes:** Write notes using standard Markdown. The editor hides syntax symbols (e.g., `**`, `*`, `~~`) and renders styling inline.
*   **Caret Offset Mapping:** Synchronized caret positioning mapping to prevent cursor jumps when navigating rich styled text.
*   **Multi-Media Notes:** Attach local images, audio clips, and voice recordings directly to note cards.
*   **Document Annotations:** Store highlights, bookmarks, and sentence-level notes. An alignment remapping algorithm keeps annotations anchored even if the document text is edited.
*   **Note Reminders:** Schedule exact alarms on notes, with automatic fallbacks for newer API versions.
*   **Extracted Text Editor:** Repair or edit selected sentences directly within the reader app without modifying the original source document.
*   **Pronunciation Rules:** Configure speech replacements/corrections (e.g., spelling out abbreviations or fixing phonetic anomalies) applied before playback and audio export.

### 4. On-Device AI & Reference Utilities
*   **Offline Study Tools:** Generate local summaries, key points, terms, flashcards, and quizzes from saved text without requiring external API keys.
*   **Ask AI Handoff:** Instantly prepare selected text or a reading part for an installed AI app (like Gemini) without needing developer API keys.
*   **Translation Handoff:** Prepare selected text, sections, or documents for installed translation apps.
*   **Dictionary & Wikipedia Lookups:** Quick definition lookup for selected words through local dictionary databases or Wikipedia search shortcuts.

### 5. Habit Analytics & App Updates
*   **Daily Streaks:** Streak tracker showing current and longest reading patterns.
*   **Habit Heatmap:** Visual calendar heatmap showing daily read sessions.
*   **Interactive Analytics Charts:** Dynamic donut charts displaying Format Distribution and Top Book Allocation with spring-bouncy expansion and details cards.
*   **8-Week Rolling History:** Swipeable bar charts displaying daily reading metrics.
*   **GitHub Update Checker:** A custom, non-compulsory update checking service that queries latest GitHub Releases and prompts users on launch when updates exist.
*   **Local Progress Sync Pack:** Export, share, and import local reading progress, notes, lists, and settings without requiring cloud accounts.

### 6. UI Customization & Aesthetics
*   **Premium Themes:** Deep UI custom styling including Material You, Liquid Glass, One UI-style, and High Contrast configurations.
*   **Night Mode PDF Inversion:** Custom color-matrix filter to invert PDF page rendering for comfortable night-time reading.
*   **IME Keyboard Alignment:** Dynamic input layout buffer to adjust note controls above the software keyboard automatically.

---

## 🛠️ Architecture & Tech Stack

Veritas Reader is built using standard Android architecture guidelines and modern engineering patterns:

*   **Jetpack Compose:** 100% declarative UI built with Material 3 components and spring-based animations.
*   **StateFlow MVVM:** Unidirectional Data Flow (UDF) driven by a single-source-of-truth UI state inside `ReaderViewModel`.
*   **Kotlin Coroutines & Flow:** Asynchronous task threading with cooperative cancellation points (`yield()`).
*   **Room Database & SharedPreferences:** Structured local database mappings with a resilient double-write JSON serialization system to secure user libraries against write corruption.
*   **Google ML Kit:** Latin text recognition (OCR) and Language Identification.
*   **AndroidX Media3:** Background media session integration and TTS configurations.

---

## 🏗️ Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   Java Development Kit (JDK) 21
*   Android SDK Platform 36 (Compile SDK: 36.1, Target SDK: 36, Min SDK: 28)

### Building the Project
Open the root directory in Android Studio, let Gradle sync, and build via the GUI:
```text
Build > Build APK(s)
```

For command-line gradle builds:
*   **Debug APK Build:**
    ```powershell
    .\gradlew.bat :app:assembleDebug
    ```
*   **Full Verification (Unit Tests + Build):**
    ```powershell
    .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --rerun-tasks
    ```

The generated APK will be available in:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📁 Project Structure

*   `app/src/main/java/com/veritas/reader/` - Core Kotlin source code.
    *   `ui/` - Layout screens, ViewModels, and state models.
*   `app/src/main/res/` - Android vector drawables, XML layouts, widgets, themes, and translation strings.
*   `CHANGELOG.md` - Complete version release history.
*   `KNOWN_LIMITATIONS.md` - Known technical and product boundaries.
*   `PRIVACY_POLICY.md` - Standard privacy guidelines.
