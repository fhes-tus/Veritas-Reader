# Veritas Reader Android

[![Release](https://img.shields.io/badge/Release-v2.1.0-orange.svg)](https://github.com/fhes-tus/Veritas-Reader/releases)
[![Android API Target](https://img.shields.io/badge/Target-Android%2016%20(API%2036)-green.svg)](https://developer.android.com/)
[![Android API Min](https://img.shields.io/badge/Min%20SDK-API%2028-blue.svg)](https://developer.android.com/)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.4.0-purple.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](#)

Veritas Reader is a modern, high-fidelity Android reading and text-to-speech (TTS) application. It transforms imported documents—including TXT, PDFs, DOCX, EPUBs, PPTX, images, scanned pages, and web articles—into a centralized, personal reading library equipped with on-device neural voice playback, interactive study tools, smart cover extraction, dyslexia-friendly typography, and reading habit analytics.

---

## 📥 Downloads (v2.1.0)

Choose the APK package suitable for your device from the [Releases](https://github.com/fhes-tus/Veritas-Reader/releases) page:

| Package | Size | Target Devices | Description |
| :--- | :--- | :--- | :--- |
| **`Veritas_Reader_v2.1.0_arm64.apk`** | **~55 MB** | **64-bit ARM** *(Recommended)* | Optimized build for ~99% of modern Android devices (Snapdragon, MediaTek, Tensor, Exynos). |
| **`Veritas_Reader_v2.1.0_universal.apk`** | **~76 MB** | **All Devices** | Universal package containing all native binary architectures. |
| **`Veritas_Reader_v2.1.0_armeabi-v7a.apk`** | **~46 MB** | **32-bit ARM** | Lightweight package for legacy 32-bit Android phones and tablets. |

---

## 📱 Core Features

### 1. Document Import & Preloaded Library
*   **Default Classic Book:** Comes bundled with Dr. Spencer Johnson's classic *"Who Moved My Cheese?"* with full cover artwork and chapter formatting, alongside the interactive *Veritas Welcome Guide*.
*   **Multi-Format Support:** Import local EPUB, DOCX, PPTX, PDF, and TXT files, or extract clean content from web links.
*   **Intelligent Cover Extractor:** Multi-page cover candidate scoring with aspect-ratio validation, text-presence analysis, and stylized gradient fallback generation.
*   **Hybrid OCR & Text Extraction:** Uses a hybrid extraction system powered by PDFBox, native Android PDF rendering, and Google ML Kit OCR to parse content, preserve formatting, and identify column structures.

### 2. Audio Playback & Neural TTS Engine
*   **On-Device Neural Voices:** High-fidelity neural TTS engines powered by Sherpa-ONNX, Kokoro, and Piper with native streaming audio buffers alongside System TTS fallbacks.
*   **Voice Management:** Deep voice configurations allowing speed, pitch, engine preference, and accent selections.
*   **Audio Output Safety:** Auto-pauses or ducks speech during audio focus changes (e.g., incoming phone calls) and headphone disconnects.
*   **Headset Control Mapping:** Map wired, Bluetooth, and media-button actions to reading controls.
*   **Audio Export:** Convert processed texts into standard WAV audio files for offline listening.
*   **Sleep Timer:** Auto-stop playback with section-end boundaries so audio never halts mid-sentence.

### 3. Study Tools & Markdown Annotations
*   **Multi-Media Notes:** Attach local images, audio clips, and voice recordings directly to note cards.
*   **Document Annotations:** Store highlights, bookmarks, and sentence-level notes. An alignment remapping algorithm keeps annotations anchored even if the document text is edited.
*   **Note Reminders:** Schedule exact alarms on notes, with automatic fallbacks for newer API versions.
*   **Extracted Text Editor:** Repair or edit selected sentences directly within the reader app without modifying the original source document.
*   **Pronunciation Rules:** Configure speech replacements/corrections (e.g., spelling out abbreviations or fixing phonetic anomalies) applied before playback and audio export.

### 4. Onboarding Quests & Interactive Guided Tour
*   **Interactive Guided Tour:** Multi-step spotlight tutorial that navigates through the document reader, Notes Studio, AI Study Hub, and Settings Hub.
*   **Onboarding Quests:** Floating mission checklist tracking core app milestones (guided tour, import, voice customization, bookmarks) with persistent progress.

### 5. On-Device AI & Reference Utilities
*   **Offline Study Tools:** Generate local summaries, key points, terms, flashcard sets, and quizzes from saved text without requiring external API keys.
*   **Ask AI Handoff:** Instantly prepare selected text or a reading part for an installed AI app (like Gemini) without needing developer API keys.
*   **Translation Handoff:** Prepare selected text, sections, or documents for installed translation apps.
*   **Dictionary & Wikipedia Lookups:** Quick definition lookup for selected words through local dictionary databases or Wikipedia search shortcuts.

### 6. Habit Analytics & App Updates
*   **Daily Streaks:** Streak tracker showing current and longest reading patterns.
*   **Habit Heatmap:** Visual calendar heatmap showing daily read sessions.
*   **Interactive Analytics Charts:** Dynamic donut charts displaying Format Distribution and Top Book Allocation with spring-bouncy expansion and details cards.
*   **8-Week Rolling History:** Swipeable bar charts displaying daily reading metrics.
*   **Local Progress Sync Pack:** Export, share, and import local reading progress, notes, lists, and settings without requiring cloud accounts.

### 7. UI Customization & Typography
*   **Custom Reading Fonts:** Atkinson Hyperlegible, Bitter, Literata, and Lora variable fonts for high legibility and reading comfort.
*   **Premium Themes:** Deep UI custom styling including Material You, Liquid Glass, One UI-style, and High Contrast configurations.
*   **Night Mode PDF Inversion:** Custom color-matrix filter to invert PDF page rendering for comfortable night-time reading.

---

## 🛠️ Architecture & Tech Stack

Veritas Reader is built using standard Android architecture guidelines and modern engineering patterns:

*   **Jetpack Compose & Material 3:** 100% declarative UI built with Material 3 components and spring-based animations.
*   **StateFlow MVVM:** Unidirectional Data Flow (UDF) driven by a single-source-of-truth UI state inside `ReaderViewModel`.
*   **Kotlin Coroutines & Flow:** Asynchronous task threading with cooperative cancellation points (`yield()`).
*   **Sherpa-ONNX & Kokoro / Piper:** High-efficiency C++/JNI neural text-to-speech runtime.
*   **Room Database & SharedPreferences:** Structured local database mappings with a resilient double-write JSON serialization system to secure user libraries against write corruption.
*   **Google ML Kit:** Latin text recognition (OCR) and Language Identification.
*   **AndroidX Media3:** Background media session integration and TTS configurations.

---

## 📸 Screenshots

<p align="center">
  <img width="45%" alt="Library Home" src="https://github.com/user-attachments/assets/da75ac3a-471e-44fc-aea0-b75cb24244bf" />
  <img width="45%" alt="Reading Mode" src="https://github.com/user-attachments/assets/812b50c6-3dac-480a-88fb-fbdba486c941" />
</p>
<p align="center">
  <img width="45%" alt="Study Tools & Notes" src="https://github.com/user-attachments/assets/522fa0d7-2856-4350-9d76-94599136bc63" />
  <img width="45%" alt="Habit Analytics" src="https://github.com/user-attachments/assets/e64e1b7c-ee5b-4d09-9883-1b2701407ba3" />
</p>
<p align="center">
  <img width="45%" alt="Onboarding & Quests" src="https://github.com/user-attachments/assets/3821df10-f370-4b57-a276-d9f946b73bfb" />
  <img width="45%" alt="Settings Hub" src="https://github.com/user-attachments/assets/557bdf81-cf84-4c39-8591-65d33cfb5f40" />
</p>

---

Download and install the newest release APK for the most up-to-date app experience:  
👉 **[Veritas Reader Releases](https://github.com/fhes-tus/Veritas-Reader/releases)** 🎉

