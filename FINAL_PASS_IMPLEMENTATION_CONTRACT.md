# Final Pass Implementation Contract

This file is the contract for the final Veritas Reader Android pass. If an older implementation disagrees with this file, this file wins.

## 1. Large PDF Import And Loading

- The foreground loading screen is allowed to show for about 1 minute.
- After that wait, the app must not fail the import just because the whole document is not finished.
- The reader should open with the pages/parts that are ready.
- Extraction should continue in the background or resume on demand.
- Extracted page text must be cached so reopening a saved document does not start from zero.
- If a user was reading near a later page, such as page 600, extraction should prioritize that page and nearby pages before trying to finish the whole book from page 1.
- The import message must not say "Import stopped after 1 minute" for text-based PDFs.

## 2. PDF Column Extraction

- Normal text spacing must be preserved.
- The broken output where words are joined together, such as `courseprescribedforsurgeons`, is unacceptable.
- Column extraction must actually fix two-column reading order; it must not fall back to a worse one-column reading order when a page clearly has columns.
- Layout is detected per page because documents can switch between one-column and two-column pages.
- One-column pages use the fast normal extraction path.
- Clear two-column pages are extracted by physical page regions before text is merged into paragraphs.
- Region extraction must keep real word spaces and paragraph breaks as much as possible.

## 3. Extracted Text View

- Extracted text is shown as one clean document-like part surface, not chunk/section cards.
- Parts are page-based and balanced according to the existing part rules.
- End of part shows `(Part X of Y)` and must not be read by TTS.
- Previous/next part controls stay under the part, not in the bottom deck.
- Bottom deck remains: app icon, previous sentence, play/pause, next sentence, playback menu.

## 4. Natural Text Selection

- Selection must feel like normal Android text selection.
- Do not use the custom draggable dot handles that cause scroll fighting.
- A user can select across multiple sentences.
- Selection actions map the selected text range to every touched sentence.
- Tapping outside the active selection/menu should dismiss the selection/menu.
- Double tap on a sentence starts reading from that sentence.

## 5. Selection Actions

- If selected text touches 4 sentences, notes/highlights/bookmarks apply to all 4 sentences.
- Read from here starts at the first touched sentence.
- Copy/share/translate/search/AI use the exact selected text.
- Edit extracted text edits the selected sentence range, not the whole document.

## 6. Edit Extracted Text

- The normal editor scope is either selected sentence range or current part.
- Whole-document editing is not the normal path.
- Saving a selected range only replaces those sentences.
- Saving a part only replaces that part.

## 7. Actual PDF View

- Sync checkbox jumps back to the page/sentence being read.
- If the user scrolls away far enough, sync unchecks.
- Deck toggling only responds to a clean tap in the top 30% of the screen.
- Read from here uses the actual selected PDF text, then searches the current PDF page first and nearby pages second.
- It must not search the whole document first for common words.

## 8. Large Document Safety

- 500-1000+ page documents must not crash import, reopen, outline, or playback.
- Smart Outline remains capped/sampled.
- Library reopen should be stable.

## 9. Verification Before APK

- Run unit tests and debug build before creating any APK bundle.
- Do not ship a bundle if extraction spacing is obviously broken or import fails after the foreground wait.
