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

### 2. Audio Playback & Text-to-Speech Engine
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
*   **Local Progress Sync Pack:** Export, share, and import local reading progress, notes, lists, and settings without requiring cloud accounts.

### 6. UI Customization & Aesthetics
*   **Premium Themes:** Deep UI custom styling including Material You, Liquid Glass, One UI-style, and High Contrast configurations.
*   **Night Mode PDF Inversion:** Custom color-matrix filter to invert PDF page rendering for comfortable night-time reading.

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

<img width="493" height="1280" alt="image" src="https://github.com/user-attachments/assets/da75ac3a-471e-44fc-aea0-b75cb24244bf" />
<img width="432" height="1280" alt="image" src="https://github.com/user-attachments/assets/812b50c6-3dac-480a-88fb-fbdba486c941" />
<img width="629" height="1280" alt="image" src="https://github.com/user-attachments/assets/522fa0d7-2856-4350-9d76-94599136bc63" />
<img width="629" height="1280" alt="image" src="https://github.com/user-attachments/assets/e64e1b7c-ee5b-4d09-9883-1b2701407ba3" />
<img width="629" height="1280" alt="image" src="https://github.com/user-attachments/assets/3821df10-f370-4b57-a276-d9f946b73bfb" />
<img width="311" height="1280" alt="image" src="https://github.com/user-attachments/assets/557bdf81-cf84-4c39-8591-65d33cfb5f40" />


Download and install the newest release apk for the most up-to-date app experience.

 https://github.com/fhes-tus/Veritas-Reader/releases  **🎉**                                                               **ENJOY**
