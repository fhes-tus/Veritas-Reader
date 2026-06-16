# Known Limitations

This document outlines the technical boundaries, platform constraints, and design limitations for **Veritas Reader Android v1.0.1**.

---

## 🏗️ Build & Distribution
*   **Debug Testing Build:** The current default APK configuration is a debug build intended for local QA and testing. Prior to Google Play Store deployment, a production-signed build target must be configured.
*   **Gradle JDK Pinning:** The project's build properties (`gradle.properties`) target Android Studio's local embedded JDK path. If building on a different machine, update or omit `org.gradle.java.home` to allow Android Studio to resolve the local JDK path automatically.
*   **Generated Folders:** Standard build cache directories (`.gradle/`, `.kotlin/`, `.idea/`, `app/build/`) are excluded from version control and will be automatically re-created by Gradle upon the first compilation sync.

---

## 📄 Document Import & Extraction
*   **PDF Layout Extraction:** Text extraction from PDF documents utilizes a custom parser built on PDFBox. Complex PDF formats—such as multi-column layouts, magazines, tabular data, mathematical notations, and embedded images—may parse with spacing irregularities.
*   **Optical Character Recognition (OCR):** Fallback image text recognition depends on Google ML Kit. OCR output accuracy is subject to scan lighting, character resolution, orientation, and device runtime capabilities.
*   **DOCX & EPUB Preservation:** Parsers extract primary document text flows, bookmarks, and structural points. Complex styling (e.g., sidebars, embedded charts, footnotes, dynamic styling sheets) may not be preserved in the text reading mode.
*   **Web Imports:** The web article reader fetches publicly accessible pages using direct HTTP queries. Web sites behind paywalls, login screens, CAPTCHAs, heavy JavaScript frameworks, or anti-bot shields (e.g., Cloudflare) will not import cleanly.

---

## 🔍 Native PDF Viewer & Page Sync
*   **Read from Here Sync:** The primary PDF view supports selection-based audio playback. This feature relies on Android's `PdfViewerFragment` exposing text selections. If the OS framework hides or restricts the selection action, this mapping may fail.
*   **Approximated Sync:** Synchronization between the original PDF view page and the active TTS playback sentence uses approximate string search mappings. It is not a layout-aware alignment engine; documents with inconsistent layout coordinates may experience alignment drift.
*   **Fallback Document Canvas:** The custom image-based canvas fallback displays document pages as high-resolution images but does not support system-level native text selection.

---

## 🔊 Playback & Audio Synthesizer
*   **Engine Dependency:** Text-to-speech quality, accent availability, and voice selections are bound to the active TTS engine installed on the user's Android device (such as Google Speech Services).
*   **Background Lifecycle Permissions:** Background playback relies on Android's foreground service execution rules. If notification permissions are revoked, playback will stop when the app enters the background.
*   **Audio Export Constraints:** Converting text to WAV files runs via the local TTS engine. Synthesis operations for very long books may take time and could fail if the local engine rejects massive audio generation jobs.

---

## 🔒 Storage, Backup & Synchronization
*   **Local-Only Storage:** All imported documents, reading progress coordinates, streaks, and note files are stored locally on the device.
*   **No Cloud Sync:** Veritas Reader is designed as a private offline-first reading tool; it does not feature a central cloud-sync account service.
*   **Android Backup Disabled:** Auto-backups are explicitly disabled in the app manifest. Uninstalling the app or clearing its local storage will permanently delete all saved readings, streaks, and notes unless manual backups were exported.

> [!WARNING]
> Clearing the application data or uninstalling the app will result in the loss of all local database files. Ensure notes are exported using the app's sharing tools if they contain critical data.

---

## 🧪 Testing & Verification Status
*   **Unit & Instrumentation Tests:** Basic unit test coverage (covering state transitions, formatting rules, and habit calculations) compiles successfully using:
    ```powershell
    .\gradlew.bat :app:testDebugUnitTest
    ```
*   **Device-Level QA:** Manual device validation across targeted API levels is highly recommended prior to distribution to verify hardware text-to-speech synthesis quirks.
