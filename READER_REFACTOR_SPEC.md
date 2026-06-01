# Reader Refactor Spec

This file records the agreed reader changes before wiring them into the app.

## Reading Units

- The visible extracted text reader is organized by parts, not chunk cards or section cards.
- Inside a part, the text is shown as one continuous document-like page.
- The action anchor is the sentence that contains the selected word, characters, or tapped text.
- Notes, highlights, bookmarks, AI actions, sharing actions, and read-from-here actions apply to that sentence unless the UI explicitly asks for a larger scope.
- Edit-document actions may apply to the selected string, the containing sentence, or the entire current part.
- Double tapping any sentence in the extracted text view starts reading from that sentence.

## Part Rules

- Parts are based on original document page numbers.
- For documents below 30 pages, the preferred minimum part size is 6 pages and the highest part size remains 12 pages.
- For documents above 30 pages, part sizes should stay between 9 and 12 pages where possible.
- The app must avoid tiny leftover final parts.
- Pages should be balanced so the largest part has at most 1 page more than the smallest part.
- Example: 25 pages becomes 3 balanced parts, such as 9 / 8 / 8, instead of 12 / 12 / 1.
- Very large documents should be split into balanced parts near 12 pages each.

## Navigation

- Navigation inside a part is vertical.
- Navigation between parts is horizontal/logical part-to-part movement.
- Existing reader buttons should keep their familiar meaning.
- The extracted text lower deck should use icon-only controls in this order:
  - App icon on the far left
  - Previous part
  - Previous sentence
  - Play/pause
  - Next sentence
  - Next part
- End-of-part controls should appear at the end of each part as icon-only previous-part and next-part actions.
- End-of-part declarations should display as `(Part X of Y)` and must not be spoken by TTS.

## Extracted Text Layout

- Preserve paragraphing, line breaks, spacing, casing, and outline as much as the source extraction allows.
- PDF text extraction should follow visual line order and should not merge separated columns or separated line groups into one sentence.
- A PDF page remains one original page for part calculation even when its extracted text contains multiple visual blocks.

## Actual Document View

- When sync is enabled, the actual PDF/document view follows the page currently being read.
- If the user scrolls or navigates far away from the currently read page, roughly past 75% of a page length, sync turns off automatically.
- Turning sync back on jumps the actual document view back to the current reading page.
- The decks/chrome toggle should only react to a real tap in the top 30% of the screen.
- Scrolls and drags must not toggle the decks.
- Long pressing/selecting text in the actual PDF view and choosing read-from-here maps that selection back to the containing extracted sentence, then continues reading from that sentence while staying in the actual document view.

## Large Document Reliability

- Large documents, including documents above 500 pages, must import, save, reopen, and read without crashing.
- The app should use the most appropriate robust approach: page/part indexing, bounded memory use, cached metadata, and loading only the text or rendered pages needed for the active operation.
- Library reopen must not eagerly render, index, or display an entire very large document in one burst.

## Verification

- Add focused tests for page parting and sentence anchoring.
- Build the Android debug APK after implementation.
- Update the final APK readiness file with the QA result after the build passes.
