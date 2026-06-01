# Veritas Reader Privacy Policy Draft

Effective date: May 18, 2026

This is a draft privacy policy for the current Veritas Reader Android build. It should be reviewed before public release or Play Store submission.

## Overview

Veritas Reader is a local document reading and text-to-speech app. The app is designed to import documents selected by the user, extract readable text, save readings on the device, and play them aloud using Android's Text-to-Speech system.

## Information The App Handles

The app may handle the following information when the user chooses to provide it:

- Imported documents, including text files, PDFs, DOCX files, EPUB files, images, scanned PDFs, and shared text.
- Extracted text from imported documents.
- Reading progress, favorites, Listen Later queue items, collections, bookmarks, highlights, notes, pronunciation rules, voice settings, and reader preferences.
- Web article URLs or page text when the user imports a web page.
- Text selected by the user for sharing, AI prompts, translation, search, or read-aloud actions.

## How Information Is Stored

Readings, extracted text, settings, and notes are stored locally by the app on the user's device. Veritas Reader does not include an account system, cloud sync service, advertising SDK, or analytics SDK in this build.

Android backup is disabled for this app build in the manifest.

## Network Use

The app requests internet permission. Network access may be used when the user imports a web article or opens web-based external actions.

The app does not run its own analytics, advertising, or background upload service. Some Android system services or external apps used by the user, such as browser apps, AI apps, translation apps, TTS providers, or Google Play services, may use network access under their own privacy policies.

## Text Recognition And TTS

The app uses text recognition to extract text from images or scanned PDFs where possible. The app also uses Android's installed Text-to-Speech engine to read text aloud and export audio. Behavior may vary depending on the device, installed OCR/TTS components, selected voice, and system settings.

Some TTS voices may require network access or downloaded voice data depending on the selected engine. Those services are controlled by the TTS provider, not by Veritas Reader.

## Sharing With External Apps

Veritas Reader can hand off selected text, document excerpts, generated prompts, or translation text to external apps when the user chooses an action such as share, Ask AI, translation, or Google search.

Once text is sent to another app, that app's own privacy policy and behavior apply. Users should avoid sharing sensitive documents or excerpts with external apps they do not trust.

## Permissions

Current app permissions include:

- `INTERNET` for web article import and web/external actions.
- `POST_NOTIFICATIONS` for playback notifications on supported Android versions.
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` for background reading controls.

The app receives document access through Android's document picker, share sheet, and temporary URI permissions when the user selects or shares a file.

## User Control

Users can:

- Choose which documents to import.
- Delete saved readings from the app.
- Clear reading progress for saved readings.
- Choose whether to share selected text with external apps.
- Change voice, speed, pitch, theme, and reader preferences.
- Uninstall the app or clear app data through Android settings.

## Data Retention

Imported readings and app settings remain on the device until the user deletes them, clears app data, or uninstalls the app. Exported files, such as WAV audio exports, remain wherever the user saves them until deleted by the user.

## Children

This app is not designed to knowingly collect personal information from children. Because documents are user-selected and stored locally, users should avoid importing sensitive documents on shared devices.

## Contact

Developer/contact details should be added here before public distribution.
