# Veritas Reader v2.4.0 (Build 37)

**Veritas Reader v2.4.0** is a major milestone release delivering a completely redesigned **Universal Classic Books Bookstore**, **Theme-Adaptive Visual Table Cards**, **Fluid Reader Gestures with Focus Trap Elimination**, **Smart Read Time Indicators**, **Dual Play Store / GitHub Update Routing**, and full compliance with Google Play's exact alarm policy and Android 16's 16 KB page-size architecture.

---

### 📚 1. Universal Classic Books Bookstore & Catalogue
* **Curated 36-Masterpiece Bookstore:** Re-engineered the classic books experience from a plain list into a full digital bookstore featuring 36 hand-curated public domain classics spanning 7 distinct categories: *Family & Youth*, *Mystery*, *Adventure*, *Romance*, *Life & Habits*, *Quick Reads*, and *Epic Classics*.
* **Bookstore Shelf (2-Column Grid) vs. List View:** Added an interactive view switcher allowing readers to browse cloth-bound 3D cover art on a physical bookstore shelf or switch to a high-density, metadata-rich list view.
* **Interactive Book Details Preview Sheet:** Tapping any volume presents an immersive bottom sheet previewing the book's synopsis, author background, estimated reading duration, curated literary quotes, and 1-tap library download & reader launch.
* **Classics Catalog Guided Tour Integration:** Connected the catalog into the in-app interactive walkthrough (`CLASSICS_SPOTLIGHT`) with dynamic viewport scrolling, spotlight cutouts, and synchronized voice narration.

---

### 📊 2. Visual Table Card Formatting in Extracted Text Reader
* **Theme-Adaptive Container Cards:** Replaced raw pipe-delimited text (`| Col 1 | Col 2 |`) with modern, bordered table cards (`TableCardBackgroundSpan` via `LineBackgroundSpan`). The card background, borders, and dividers automatically harmonize with Dark/AMOLED, Warm Sepia, and White canvas tones.
* **Header Highlights & Soft Dividers:** Header rows automatically receive bold styling and primary accent color. Ugly outer boundary pipes are hidden with zero-width spans, while internal column dividers are rendered in subtle separator tones.
* **Compact Row Spacing:** Tightened double-newline paragraph spacing between consecutive table rows down to `0.35x`, uniting multi-row tables into cohesive, elegant cards.
* **100% TTS Invariant Preservation:** Table formatting uses purely non-destructive spans; character offsets in `part.text` remain identical. `SpeechSanitizer` treats pipes as silent spaces, ensuring speech synthesis reads cells naturally without delimiter audio artifacts.

---

### 🛠️ 3. Reader Paging Smoothness & Text Selection Snapping Fix
* **Eliminated Android Focus Trap:** Resolved a critical issue where text selection on a page granted window focus to its native `TextView`, causing Android's focus subsystem to snap `HorizontalPager` back to the previously selected page during page navigation.
* **De-focused Off-Screen Pages:** Off-screen `TextView` instances now automatically strip selection handles and clear focus upon page transitions.
* **Fluid Swipe Gestures:** Decoupled text selection from pager scrolling. Swiping horizontally now fluidly clears active text selection handles without locking `userScrollEnabled` or dead-locking manual page swipes.

---

### ⏱️ 4. Zero-Bloat Expected Read Time Indicators
* **Calculated Reading Duration Pills:** Added smart reading time estimates (e.g., `~15m`, `~1h 20m`) based on word count and standard reading speeds.
* **Zero Height Impact:** Integrated into subtitle metadata across all Library card formats (`DocumentCard`, `DocumentTileCard`, `HomeRecentBookGridItem`, `RecentImportItem`) without altering card padding, layout heights, or visual density.

---

### 🛡️ 5. Dual Google Play & GitHub In-App Updater
* **Source-Aware Update Routing:** The in-app updater now inspects the app's installer package (`com.android.vending`). Installations from Google Play are routed directly to the Play Store to strictly adhere to Google Play Developer Policies.
* **Direct Sideload/GitHub Updates Preserved:** Installations from GitHub or manual APK sideloads retain seamless in-app APK download, integrity verification, and local package installation.

---

### 🔒 6. Google Play Policy Compliance & Android 16 Readiness
* **Exact Alarm Removal:** Fully removed `USE_EXACT_ALARM` and `SCHEDULE_EXACT_ALARM` permissions; replaced exact alarms in `GeneralNotesEditor` with compliant inexact notification scheduling.
* **Android 15/16 16 KB Page-Size Support:** Verified and passed Google Play's 16 KB memory page size compatibility check.
* **versionCode 37:** Incremented version code to `37` for seamless Play Console submission.

---

### 🎨 7. Settings Hub & UI Harmonization
* **AMOLED Display Preference:** Moved the AMOLED Mode toggle into Reader Display Preferences menu alongside paper tones for intuitive customization.
* **Onboarding Quests Missions UI:** Restored theme-adaptive mission card layout and celebration persistence.
* **Standardized Spotlight Cards:** Unified tour spotlight cards across `ClassicsCatalogDialog`, `LibraryInsightsUi`, and `SettingsHub`.

---

### 📦 Release Binaries & Checksums

| Filename | Type | Size | Target / Architecture | SHA-256 Checksum |
| :--- | :--- | :--- | :--- | :--- |
| **`Veritas-Reader-v2.4.0-release.aab`** | Google Play Bundle | 59.5 MB | Google Play Store (versionCode 37) | `97CD7210CEDE73BCB48F7F9159ECC9B2FB75AEF93DBC0D447CC03D817DBFD5F0` |
| **`Veritas-Reader-v2.4.0-arm64-v8a-release.apk`** | Release APK | 55.1 MB | 64-bit ARM (`arm64-v8a`) | `16CD16E8C487D093E8CB1019E5FF2A049CBDA15E2F36B42C3BE83E88217B413B` |
| **`Veritas-Reader-v2.4.0-armeabi-v7a-release.apk`** | Release APK | 45.7 MB | 32-bit ARM (`armeabi-v7a`) | `782CAECCB438E38588FF772017F68E4F3FEF9710F55543C1B4E8D583A52C24DC` |
| **`Veritas-Reader-v2.4.0-universal-release.apk`** | Release APK | 77.0 MB | Universal (All Architectures) | `CB460F8A73DC5B4D1DBB8484E6BF021EA13863B5D595EEA9E43792162B4ADA0F` |
| **`Veritas-Reader-v2.4.0-release.apk`** | Release APK | 77.0 MB | Universal Standard Alias | `CB460F8A73DC5B4D1DBB8484E6BF021EA13863B5D595EEA9E43792162B4ADA0F` |

---

### 🧪 Verification Summary
* **Kotlin Compilation**: `compileDebugKotlin` passed with 0 errors.
* **Automated Unit Tests**: `testDebugUnitTest` executed 27 task suites — 100% passed.
* **ProGuard / R8**: Full mode enabled; DEX size optimized to 16.8 MB with zero mapping collisions.
