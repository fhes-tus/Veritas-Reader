# Vern TTS v2.5.0 (Build 38)

**Vern TTS v2.5.0** is a major milestone release delivering the official **Vern TTS** brand identity, **Authentic Published Book Covers** for all 35 curated classics, **Bidirectional 3-Way Paper Tone Synchronization**, an **Intelligent Heuristic Book Synopsis Engine** for storage files, **Universal Drag-and-Drop Elevation**, and optimized **PDF/Document Orientation Controls**.

---

### 🌟 1. Vern TTS Identity & Philosophy
* **Vern TTS Branding:** Established the official identity across app bars, drawer footers, and onboarding.
* **Origin & Etymology:** Rooted in the German *Vernehmen* ("to hear, perceive, comprehend"), emphasizing privacy-first, 100% on-device text-to-speech learning without tracking.

---

### 🎨 2. Authentic Published Book Covers & Fast Caching
* **35 Bundled Classic Volume Covers:** Integrated authentic published cover artwork from Project Gutenberg for all 35 curated classics.
* **Synchronous Cover Pipeline:** Eliminates initial placeholder flashes by decoding and saving covers synchronously upon download.
* **Universal Asset Fallback:** Existing classics in the library automatically render authentic artwork across cards, Book of the Day, and reader page 1.

---

### 📖 3. Bidirectional 3-Way Paper Tone Sync
* **Seamless Visual Consistency:** Paper tone selections (Default, Warm Sepia, Soft Dark, AMOLED Black) in Extracted Text, Actual Document, and PDF viewers immediately synchronize bidirectionally across all reader modes.

---

### ⚡ 4. Zero-Lag Orientation & Reorganized PDF Playback Bar
* **Instant Dynamic Rotation:** Eliminated sensor latency by utilizing explicit portrait and landscape requests.
* **Reorganized PDF Controls:** Added the missing Next button in standard playback order: Previous -> Play/Pause -> Next -> Rotate -> Expand.

---

### 📑 5. Intelligent Heuristic Book Synopsis Engine
* **Clean Synopses for Storage Documents:** Automatically detects and filters out Gutenberg headers, publisher licenses, copyright boilerplate, and tables of contents when importing local EPUBs, PDFs, and TXT files.
* **First Substantive Narrative Paragraph:** Captures the true opening prose of books for a polished, finished presentation in the Book Details sheet.

---

### 🖐️ 6. Universal Drag-and-Drop & Hold-to-Select Gestures
* **Smooth Depth & Floating Cards:** Elevated dragged items (zIndex = 100f, shadowElevation = 36f, scale = 1.06f) with non-blocking glide animations for neighboring cards.
* **Calibrated List Threshold:** Set to 90dp to eliminate sudden jumping during reordering.
* **Clean Hold-to-Select:** Holding without dragging enters multiselect mode cleanly.

---

### 🏷️ 7. Universal Hero Card Cover & Details Inspector
* **Split Action on Continue Card:** Tapping the cover of any active document opens the Book Details sheet, while tapping the card body resumes reading.
* **Polished Info Icon:** Sized the (i) info button in list cards to a subtle 24dp container / 15dp icon.

---

### 🧪 Verification Summary
* **Kotlin Compilation**: compileDebugKotlin passed with 0 errors.
* **Automated Unit Tests**: testDebugUnitTest executed 27 task suites -- 100% passed.
* **Full Debug APK Assembly**: assembleDebug passed with 0 errors.

---

### 📦 Downloads & Recommended Architectures

* **64-bit ARM (`arm64-v8a`) Release APK:** `Veritas-Reader-v2.5.0-arm64-v8a-release.apk` (55.9 MB) — *Recommended for 99% of modern Android phones & tablets.*
  - **SHA-256:** `8F1D3AA76DE07901ABA347CA4F8EC3C0D72B608486DD689516DC707031F56EFF`
* **Universal Release APK:** `Veritas-Reader-v2.5.0-universal-release.apk` (77.8 MB) — *Compatible with all supported Android devices.*
  - **SHA-256:** `54E8F053078F1009CBD1AF33E6A7130D5E3C715F645575E0408E18E4A3943D24`
* **32-bit ARM (`armeabi-v7a`) Release APK:** `Veritas-Reader-v2.5.0-armeabi-v7a-release.apk` (46.5 MB) — *For legacy 32-bit devices.*
  - **SHA-256:** `E87937DF9B778F647724A339AC62178372E93C4017C9AA7C81F6575D9C8E0A8A`
* **Google Play Bundle:** `Veritas-Reader-v2.5.0-release.aab` (60.4 MB, versionCode 38) — *For Google Play Console submission.*
  - **SHA-256:** `D2C655AB7819F921EC3EECEE0969ECFECBBB264080AB1886A37E956AFA5A4DB1`
