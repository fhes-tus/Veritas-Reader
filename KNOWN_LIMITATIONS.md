# Known Limitations

These limitations describe the current `0.3.7-beta-release-hardening` state.

## Build And Distribution

- The included APK flow is a debug build for testing. It is not Play Store signed.
- The project pins Gradle to Android Studio's embedded JDK path for this machine. On another PC, `org.gradle.java.home` may need to be changed or removed.
- Generated folders such as `.gradle`, `.kotlin`, `.idea`, and `app/build` are not included in the clean source bundle and will be recreated by Android Studio.

## Import And Extraction

- PDF text extraction depends on the PDF. Scanned, image-only, DRM-protected, malformed, or unusual-layout PDFs may import poorly or require OCR.
- The extractor now keeps internal page markers for part planning and performs per-page column-aware ordering, but highly complex multi-column layouts can still need manual review.
- OCR quality depends on scan quality, lighting, page rotation, image resolution, language, and the device's OCR support.
- Very large PDFs, images, DOCX files, EPUBs, and web pages can take time to import. The reader display now opens by active part instead of rendering every sentence as a card, but device memory and PDF complexity still matter.
- DOCX and EPUB parsing covers common document structures, but complex formatting, embedded objects, footnotes, tables, and unusual EPUB navigation may not fully preserve layout.
- Web article import uses direct page fetching and basic extraction. Sites with login walls, heavy scripts, anti-bot protection, or dynamic rendering may not import cleanly.

## PDF Viewer And Actual Document View

- The primary PDF viewer supports selection-based `Read from here`, but it depends on Android PDF viewer text selection, the viewer exposing a copy action, and the selected text matching extracted document text.
- If the PDF viewer hides or blocks its copy action, option-one may still fail; a fallback/custom flow should only be added after device testing confirms this path is still failing.
- Page sync uses extracted sentence/page indexing when available and falls back to approximate mapping when older saved text lacks internal page markers. It is not a full layout-aware text-to-page alignment engine.
- The fallback actual document canvas displays rendered pages/images but does not provide the same native text-selection behavior as the PDF viewer.

## Playback And Audio

- Text-to-speech quality, available voices, languages, and network requirements depend on the installed Android TTS engine.
- Some premium or network voices may require device settings, downloaded voice data, or internet access handled by the TTS provider.
- Background playback depends on Android foreground-service rules and notification permission on newer Android versions.
- WAV export uses the installed TTS engine. Voice availability and synthesis behavior can vary by device.
- Long exports may take time and can fail if the TTS engine rejects long synthesis jobs or storage is unavailable.

## AI, Translation, And Sharing

- AI and translation tools are external handoffs. Veritas prepares or shares text, but the external app controls the final result.
- If no compatible app is installed, Veritas may copy the prompt/text to the clipboard or open a relevant install page.
- External apps may apply their own limits, privacy policies, rate limits, or text truncation.

## Storage, Sync, And Backup

- Saved readings and app settings are local to the device.
- There is no account system or cloud sync in this build.
- Android backup is disabled in the manifest for this app build.
- Clearing app data or uninstalling the app can remove saved readings unless the user exported or backed up content separately through available app tools.

## Testing Status

- The latest code-level build and unit-test check completed with `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --rerun-tasks`.
- Manual device QA is still recommended before sharing to non-test users.
