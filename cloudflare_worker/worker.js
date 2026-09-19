/**
 * Vern TTS Autonomous Telegram Bot Worker (Comprehensive Edition)
 * Serverless 24/7 Telegram Webhook Handler on Cloudflare Workers
 * Covers all features, voice setup masterclass, categorized FAQs, formats, and classics.
 */

const BOT_TOKEN = "8285832720:AAHM20ABnUrB1VzLM81eUgkO6NdXnb7Je-o";
const REPO = "fhes-tus/Vern-TTS";
const WEBSITE_URL = "https://fhes-tus.github.io/Vern-TTS/";
const CHANNEL_URL = "https://t.me/myreader_veritas";
const GITHUB_URL = "https://github.com/fhes-tus/Vern-TTS";
const PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.veritas.reader";

export default {
  async fetch(request, env, ctx) {
    if (request.method !== "POST") {
      return new Response("Vern TTS Telegram Bot Webhook Active", { status: 200 });
    }

    try {
      const update = await request.json();
      if (update.message) {
        await handleMessage(update.message, env);
      } else if (update.callback_query) {
        await handleCallbackQuery(update.callback_query, env);
      }
      return new Response("OK", { status: 200 });
    } catch (err) {
      return new Response("Error: " + err.message, { status: 200 });
    }
  }
};

/* --- Telegram API Helpers --- */

async function sendMessage(token, chatId, text, replyMarkup = null) {
  const url = `https://api.telegram.org/bot${token}/sendMessage`;
  const payload = {
    chat_id: chatId,
    text: text,
    parse_mode: "HTML",
    disable_web_page_preview: true
  };
  if (replyMarkup) {
    payload.reply_markup = replyMarkup;
  }
  return fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

async function editMessageText(token, chatId, messageId, text, replyMarkup = null) {
  const url = `https://api.telegram.org/bot${token}/editMessageText`;
  const payload = {
    chat_id: chatId,
    message_id: messageId,
    text: text,
    parse_mode: "HTML",
    disable_web_page_preview: true
  };
  if (replyMarkup) {
    payload.reply_markup = replyMarkup;
  }
  return fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

async function answerCallbackQuery(token, callbackQueryId, text = "") {
  const url = `https://api.telegram.org/bot${token}/answerCallbackQuery`;
  return fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ callback_query_id: callbackQueryId, text: text })
  });
}

/* --- Navigation & Menu Handlers --- */

async function sendWelcome(token, chatId, messageId = null) {
  const text = 
    `👋 <b>Welcome to Vern TTS Knowledge Center!</b>\n\n` +
    `<b>Vern TTS</b> (root word <i>Vernehmen</i> — to hear / perceive) is the premier <b>100% private, offline-first reading engine and document studio</b> for Android.\n\n` +
    `⚡️ <b>Quick Highlights:</b>\n` +
    `• <b>Natural Speech Engine</b>: On-device narration with lockscreen media controls & sentence sync.\n` +
    `• <b>Universal Document Studio</b>: PDF, EPUB, TXT, DOCX, & Markdown with automatic prose synopses.\n` +
    `• <b>4 Synchronized Paper Tones</b>: Clean White, Warm Sepia, Soft Dark, & True AMOLED Black.\n` +
    `• <b>35+ Curated Classics</b>: Pre-loaded with authentic, first-edition published cover artwork.\n` +
    `• <b>Integrated Study Hub</b>: Smart flashcards, sentence excerpts, & reading statistics.\n` +
    `• <b>Absolute Privacy</b>: 0 ads, 0 trackers, 0 telemetry, and zero cloud requirement.\n\n` +
    `<i>Explore any topic below or tap a quick action:</i>`;

  const markup = {
    inline_keyboard: [
      [
        { text: "⬇️ Download Vern TTS", callback_data: "cmd_download" },
        { text: "🌟 All Features", callback_data: "cmd_features" }
      ],
      [
        { text: "🎧 Natural Voice Setup", callback_data: "cmd_guide_voice" },
        { text: "📚 Curated Classics", callback_data: "cmd_classics" }
      ],
      [
        { text: "📖 User Guide & Tips", callback_data: "cmd_guide" },
        { text: "❓ In-Depth FAQs", callback_data: "cmd_faq" }
      ],
      [
        { text: "📢 Telegram Channel", url: CHANNEL_URL },
        { text: "🌐 Official Website", url: WEBSITE_URL }
      ]
    ]
  };

  if (messageId) {
    await editMessageText(token, chatId, messageId, text, markup);
  } else {
    await sendMessage(token, chatId, text, markup);
  }
}

async function sendDownloadInfo(token, chatId, messageId = null) {
  const text = 
    `📦 <b>Download Vern TTS (v2.5.0 Latest Stable)</b>\n\n` +
    `Select the recommended build for your device:\n\n` +
    `• <a href="${GITHUB_URL}/releases/download/v2.5.0/Veritas-Reader-v2.5.0-arm64-v8a-release.apk"><b>64-bit ARM APK (arm64-v8a)</b></a> ⭐️ <i>Recommended</i>\n` +
    `  Optimized for modern Android devices (Android 8.0 through Android 15). Smallest footprint and highest execution speed.\n\n` +
    `• <a href="${GITHUB_URL}/releases/download/v2.5.0/Veritas-Reader-v2.5.0-universal-release.apk"><b>Universal Release APK</b></a>\n` +
    `  Contains all architecture binaries. Works on every compatible Android phone, tablet, and Chromebook.\n\n` +
    `• <a href="${GITHUB_URL}/releases/download/v2.5.0/Veritas-Reader-v2.5.0-armeabi-v7a-release.apk"><b>32-bit Legacy APK (armeabi-v7a)</b></a>\n` +
    `  Tailored for older devices and budget hardware.\n\n` +
    `🛡 <b>Safe Sideloading Note:</b>\n` +
    `If Android prompts <i>\"Install from Unknown Sources\"</i>, tap <b>Allow</b>. This is standard for apps downloaded outside Google Play. Vern contains zero ads, zero tracking, and is 100% open source.\n\n` +
    `⏳ <b>Google Play Store:</b> Review submission is in progress. The official Play Store link will be live soon!`;

  const markup = {
    inline_keyboard: [
      [
        { text: "⬇️ Download arm64 APK (Fastest)", url: `${GITHUB_URL}/releases/download/v2.5.0/Veritas-Reader-v2.5.0-arm64-v8a-release.apk` }
      ],
      [
        { text: "📦 All Builds on GitHub", url: `${GITHUB_URL}/releases/latest` },
        { text: "🌐 Official Website", url: WEBSITE_URL }
      ],
      [
        { text: "⬅️ Back to Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  if (messageId) {
    await editMessageText(token, chatId, messageId, text, markup);
  } else {
    await sendMessage(token, chatId, text, markup);
  }
}

async function sendFeaturesMenu(token, chatId, messageId = null) {
  const text = 
    `🌟 <b>Vern TTS Feature Architecture</b>\n\n` +
    `Vern TTS is engineered from the ground up for serious readers who value their focus and privacy.\n\n` +
    `Select an area below to inspect how it works:`;

  const markup = {
    inline_keyboard: [
      [
        { text: "🎧 Speech Synthesis Engine", callback_data: "feat_speech" },
        { text: "📄 Document Studio & Formats", callback_data: "feat_docs" }
      ],
      [
        { text: "👁 4 Unified Paper Tones", callback_data: "feat_tones" },
        { text: "📖 Dual Reading Modes", callback_data: "feat_modes" }
      ],
      [
        { text: "📝 Study Hub & Flashcards", callback_data: "feat_study" },
        { text: "🛡 Offline Privacy Engine", callback_data: "feat_privacy" }
      ],
      [
        { text: "⬅️ Back to Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  if (messageId) {
    await editMessageText(token, chatId, messageId, text, markup);
  } else {
    await sendMessage(token, chatId, text, markup);
  }
}

async function sendFeatureDetail(token, chatId, messageId, topic) {
  let title = "";
  let body = "";

  if (topic === "speech") {
    title = "🎧 <b>On-Device Speech Synthesis Engine</b>";
    body = 
      `• <b>Zero-Cloud Latency</b>: All speech generation runs locally using on-device engines. Zero data sent to external servers.\n` +
      `• <b>Continuous Background Audio</b>: Keep listening seamlessly while browsing other apps, multitasking, or with your screen completely turned off.\n` +
      `• <b>Lockscreen & Notification Bar Media Bar</b>: Native Android media controls with Play, Pause, Previous/Next sentence skip, and chapter progress.\n` +
      `• <b>Synchronized Sentence Highlighting</b>: Words and active sentences highlight dynamically on screen as the voice speaks.\n` +
      `• <b>Granular Tuning</b>: Real-time slider adjustments for speech rate (0.5x – 3.0x), pitch, and system voice switching.`;
  } else if (topic === "docs") {
    title = "📄 <b>Universal Document Studio & Formats</b>";
    body = 
      `• <b>Supported Formats</b>: PDF, EPUB, TXT, DOCX (Word), and Markdown (.md).\n` +
      `• <b>Intelligent Heuristic Synopsis</b>: When you import a book or document, Vern skips boilerplate Gutenberg headers and legal disclaimers, automatically extracting the first pure narrative prose paragraph as a clean synopsis.\n` +
      `• <b>Dedicated PDF Media Bar</b>: Dedicated playback controls directly inside the PDF reader with 1-tap screen orientation switching (Portrait / Landscape) and Next sentence skip.\n` +
      `• <b>Universal Storage Access</b>: Import documents effortlessly from Internal Storage, SD Cards, Google Drive, or Downloads.`;
  } else if (topic === "tones") {
    title = "👁 <b>4 Unified Paper Tones</b>";
    body = 
      `Vern features 4 carefully calibrated color profiles designed for eye comfort across all light conditions:\n\n` +
      `1. <b>Clean White</b> (Daylight): Crisp, high-contrast black typography on neutral white.\n` +
      `2. <b>Warm Sepia</b> (Extended Reading): Warm cream background with softened amber tint that eliminates blue-light strain during long reading marathons.\n` +
      `3. <b>Soft Dark</b> (Evening): Gentle slate-slate contrast that eliminates harsh glare in low-light environments.\n` +
      `4. <b>AMOLED Black</b> (Night / Battery Saver): Pure 0% luminance black (` + `#000000) pixels that completely turn off OLED subpixels, saving massive battery life.\n\n` +
      `💡 <i>Bidirectional Sync</i>: Changes made in PDF or Extracted mode automatically synchronize across the entire app.`;
  } else if (topic === "modes") {
    title = "📖 <b>Dual Reading Modes & Navigation</b>";
    body = 
      `• <b>Paged Reading Mode</b>: Authentic book simulation with horizontal page flips, subtle page shadows, and haptic feedback. Reads exactly like a physical book.\n` +
      `• <b>Continuous Flow Mode</b>: Smooth, frictionless vertical scrolling with responsive typography that automatically reflows to fit your screen size and font size.\n` +
      `• <b>Sentence Snap</b>: Tap any sentence anywhere on a page to immediately jump audio narration directly to that sentence.\n` +
      `• <b>Fluid Library Gestures</b>: Drag-and-drop to reorder your bookshelf, hold-to-select for batch actions, and swipe to bookmark.`;
  } else if (topic === "study") {
    title = "📝 <b>Integrated Study Hub & Flashcards</b>";
    body = 
      `• <b>Smart Study Cards</b>: Highlight key quotes, vocabulary, or passages and save them instantly to your personal Study Hub.\n` +
      `• <b>Interactive Flashcards</b>: Review saved cards with question/answer flips to master concepts, literature insights, and exam notes.\n` +
      `• <b>Reading Insights</b>: On-device tracking of daily reading minutes, total pages read, words-per-minute (WPM) speed, and completion streaks.\n` +
      `• <b>Local Annotations</b>: Add document-level and sentence-level notes without cloud dependencies.`;
  } else if (topic === "privacy") {
    title = "🛡 <b>100% Offline Privacy Guarantee</b>";
    body = 
      `• <b>Zero Analytics & Telemetry</b>: No Google Analytics, no Firebase tracking, no Crashlytics data collection.\n` +
      `• <b>Zero Accounts Required</b>: No logins, passwords, or emails. Download and start reading in 2 seconds.\n` +
      `• <b>Zero Ads Forever</b>: Clean, distraction-free reading with zero banners, popups, or subscriptions.\n` +
      `• <b>Local SQLite Storage</b>: All reading history, bookmarks, and notes are stored strictly on your phone's internal sandbox.\n` +
      `• <b>Open Source</b>: Complete source code is auditable on GitHub under the Apache 2.0 license.`;
  }

  const text = `${title}\n\n${body}`;
  const markup = {
    inline_keyboard: [
      [
        { text: "⬅️ Back to Features", callback_data: "cmd_features" },
        { text: "🏠 Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  await editMessageText(token, chatId, messageId, text, markup);
}

async function sendGuideMasterclass(token, chatId, messageId = null) {
  const text = 
    `🎧 <b>Masterclass: How to Get Ultra-Natural Voices</b>\n\n` +
    `Vern TTS uses your phone's built-in Android speech engine. By default, some phones use a low-quality system voice. You can turn this into an <b>ultra-realistic, human-like voice</b> in 60 seconds completely free!\n\n` +
    `<b>How to enable High-Quality Natural Voices:</b>\n` +
    `1. Open your phone's <b>Settings</b>.\n` +
    `2. Search for <b>\"Text-to-Speech\"</b> (or go to <i>Accessibility > Text-to-speech output</i>).\n` +
    `3. Under <b>Preferred Engine</b>, select <b>Speech Services by Google</b> (or Samsung TTS).\n` +
    `4. Tap the <b>⚙️ Gear icon</b> next to Preferred Engine.\n` +
    `5. Tap <b>Install voice data</b>, choose your language (e.g., <i>English - United States</i>), and download the <b>Voice Pack (High Quality / Network or Neural)</b>.\n` +
    `6. Return to <b>Vern TTS</b> — tap the speaker icon while reading to switch to your new high-definition voice!\n\n` +
    `💡 <i>Pro Tip: Speech Services by Google offers over 15 distinct accents and natural vocal timbres that sound indistinguishable from human narrators!</i>`;

  const markup = {
    inline_keyboard: [
      [
        { text: "⬇️ Download Vern TTS", callback_data: "cmd_download" },
        { text: "❓ FAQs", callback_data: "cmd_faq" }
      ],
      [
        { text: "⬅️ Back to Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  if (messageId) {
    await editMessageText(token, chatId, messageId, text, markup);
  } else {
    await sendMessage(token, chatId, text, markup);
  }
}

async function sendClassicsOverview(token, chatId, messageId = null) {
  const text = 
    `📚 <b>35+ Curated Public Domain Classics</b>\n\n` +
    `Vern TTS comes with a built-in catalog of immortal literary masterpieces. Every book includes <b>authentic published first-edition cover art</b>, famous quotes, and reading time estimates!\n\n` +
    `🌟 <b>Featured Works Include:</b>\n` +
    `• <i>Pride and Prejudice</i> by Jane Austen\n` +
    `• <i>The Adventures of Sherlock Holmes</i> by Arthur Conan Doyle\n` +
    `• <i>Frankenstein</i> by Mary Shelley\n` +
    `• <i>The Count of Monte Cristo</i> by Alexandre Dumas\n` +
    `• <i>The Picture of Dorian Gray</i> by Oscar Wilde\n` +
    `• <i>Meditations</i> by Marcus Aurelius\n` +
    `• <i>The Art of War</i> by Sun Tzu\n` +
    `• <i>Alice in Wonderland</i> by Lewis Carroll\n` +
    `• <i>Anne of Green Gables</i> by L.M. Montgomery\n` +
    `• <i>A Tale of Two Cities</i> by Charles Dickens\n\n` +
    `📥 <b>Zero Storage Waste</b>: Classics download cleanly on-demand with 1-tap and are saved straight to your local library!`;

  const markup = {
    inline_keyboard: [
      [
        { text: "⬇️ Download App to Read", callback_data: "cmd_download" }
      ],
      [
        { text: "⬅️ Back to Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  if (messageId) {
    await editMessageText(token, chatId, messageId, text, markup);
  } else {
    await sendMessage(token, chatId, text, markup);
  }
}

async function sendFaqMenu(token, chatId, messageId = null) {
  const text = 
    `❓ <b>Vern TTS Knowledge Base & FAQs</b>\n\n` +
    `Choose a category to find comprehensive answers:`;

  const markup = {
    inline_keyboard: [
      [
        { text: "🎧 Voice Quality & Audio", callback_data: "faq_audio" },
        { text: "📄 Formats & Scanned PDFs", callback_data: "faq_formats" }
      ],
      [
        { text: "👁 Paper Tones & AMOLED", callback_data: "faq_display" },
        { text: "📝 Study Cards & Notes", callback_data: "faq_study" }
      ],
      [
        { text: "🛡 Privacy & Safe Sideloading", callback_data: "faq_security" },
        { text: "🔄 Updates & Progress", callback_data: "faq_updates" }
      ],
      [
        { text: "⬅️ Back to Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  if (messageId) {
    await editMessageText(token, chatId, messageId, text, markup);
  } else {
    await sendMessage(token, chatId, text, markup);
  }
}

async function sendFaqDetail(token, chatId, messageId, topic) {
  let title = "";
  let body = "";

  if (topic === "audio") {
    title = "🎧 <b>Audio & Speech Engine FAQs</b>";
    body = 
      `<b>Q: Why does the voice sound robotic on my phone?</b>\n` +
      `A: Vern uses Android's on-device speech engine. Many phones default to low-res voice samples. To fix this in 30 seconds: open Android Settings > Text-to-speech > install Google Speech Services and download high-quality voice packs.\n\n` +
      `<b>Q: Can I listen with my phone screen turned off?</b>\n` +
      `A: Yes! Vern TTS features a persistent Android Media Notification service. It continues narration in the background while your phone is locked or while using other apps.\n\n` +
      `<b>Q: Can I change playback speed and pitch?</b>\n` +
      `A: Yes. Tap the speaker icon on the bottom reading bar to adjust speech rate from 0.5x up to 3.0x, tune vocal pitch, and select specific voice engines.\n\n` +
      `<b>Q: Does text highlight as the voice speaks?</b>\n` +
      `A: Yes! Active sentences and paragraphs dynamically highlight on screen in sync with the spoken audio.`;
  } else if (topic === "formats") {
    title = "📄 <b>Document Formats & Scanned PDFs</b>";
    body = 
      `<b>Q: What file formats are supported?</b>\n` +
      `A: Vern TTS supports PDF, EPUB, TXT, DOCX (Microsoft Word), and Markdown (.md).\n\n` +
      `<b>Q: Can Vern read scanned image-only PDFs?</b>\n` +
      `A: Standard PDFs with digital text layers extract and read instantly. Image-only scanned PDFs (photocopies without embedded text) require OCR (optical character recognition) before text can be extracted for speech.\n\n` +
      `<b>Q: Where are the 35+ classic books stored?</b>\n` +
      `A: The classics catalog metadata is pre-indexed inside the app. When you tap a book to read, it downloads the clean public domain text on-demand and saves it locally.`;
  } else if (topic === "display") {
    title = "👁 <b>Paper Tones & Display FAQs</b>";
    body = 
      `<b>Q: How does AMOLED Black save battery?</b>\n` +
      `A: On OLED/AMOLED smartphone screens, black pixels (` + `#000000) are physically turned OFF completely and consume 0% power. Reading in AMOLED Black can extend your battery life by up to 40% during long sessions.\n\n` +
      `<b>Q: Why is Warm Sepia recommended for reading?</b>\n` +
      `A: Blue light from white screens disrupts melatonin production and causes eye fatigue. Warm Sepia filters out harsh high-frequency blue spectrums, creating a comforting experience identical to aged physical book pages.\n\n` +
      `<b>Q: Can I switch between paged flips and vertical scrolling?</b>\n` +
      `A: Yes! You can toggle between physical-style Paged Mode (with page-turn haptics) and continuous vertical Flow Mode at any time.`;
  } else if (topic === "study") {
    title = "📝 <b>Study Hub & Notes FAQs</b>";
    body = 
      `<b>Q: How do Study Cards work?</b>\n` +
      `A: While reading, highlight any passage or sentence and tap <i>Save to Study Hub</i>. You can review them anytime with interactive flip cards (Question / Answer) to memorize key concepts.\n\n` +
      `<b>Q: Does Vern track my reading speed?</b>\n` +
      `A: Yes! Vern calculates your Words Per Minute (WPM), total minutes read, pages completed, and streaks. All stats are calculated 100% locally on your phone.`;
  } else if (topic === "security") {
    title = "🛡 <b>Privacy & Safe Sideloading FAQs</b>";
    body = 
      `<b>Q: Why does Android show \"Unknown App / Source\" when installing?</b>\n` +
      `A: Android displays this warning for *any* application installed from an APK file outside the Google Play Store. It is standard Android behavior. Vern TTS contains zero malware, zero trackers, and is 100% open source on GitHub.\n\n` +
      `<b>Q: Does Vern require any internet permissions?</b>\n` +
      `A: Reading, text extraction, speech generation, notes, and study cards all run 100% offline. Internet is only used if you choose to download a new classic from the catalog.\n\n` +
      `<b>Q: Does Vern collect or sell my data?</b>\n` +
      `A: Never. Vern has no telemetry, no tracking IDs, no ads, and no user accounts. Your library and progress remain strictly on your device.`;
  } else if (topic === "updates") {
    title = "🔄 <b>Updating & Reading Progress FAQs</b>";
    body = 
      `<b>Q: Will I lose my books and bookmarks when I update?</b>\n` +
      `A: No! When you install a newer version over your existing Vern TTS app, Android preserves your SQLite database, bookmarks, reading progress, and custom settings automatically.\n\n` +
      `<b>Q: How do I get notified of updates?</b>\n` +
      `A: Join our official Telegram channel <b>@myreader_veritas</b>! New releases and changelogs are published there automatically as soon as they are ready.`;
  }

  const text = `${title}\n\n${body}`;
  const markup = {
    inline_keyboard: [
      [
        { text: "⬅️ Back to FAQs", callback_data: "cmd_faq" },
        { text: "🏠 Main Menu", callback_data: "cmd_menu" }
      ]
    ]
  };

  await editMessageText(token, chatId, messageId, text, markup);
}

/* --- Core Message & Callback Routers --- */

async function handleMessage(message, env) {
  const token = env?.TELEGRAM_BOT_TOKEN || BOT_TOKEN;
  const chatId = message.chat.id;
  const text = (message.text || "").trim().toLowerCase();

  // Command handlers
  if (text.startsWith("/start") || text.startsWith("/help") || text === "menu") {
    await sendWelcome(token, chatId);
  } else if (text.startsWith("/download") || text.includes("download") || text.includes("apk")) {
    await sendDownloadInfo(token, chatId);
  } else if (text.startsWith("/features") || text.includes("feature")) {
    await sendFeaturesMenu(token, chatId);
  } else if (text.startsWith("/guide") || text.startsWith("/tips") || text.includes("guide") || text.includes("voice") || text.includes("robotic")) {
    await sendGuideMasterclass(token, chatId);
  } else if (text.startsWith("/classics") || text.includes("classic") || text.includes("book")) {
    await sendClassicsOverview(token, chatId);
  } else if (text.startsWith("/faq") || text.includes("faq") || text.includes("question") || text.includes("help")) {
    await sendFaqMenu(token, chatId);
  } else if (text.startsWith("/community") || text.includes("channel") || text.includes("group")) {
    const commMsg = 
      `📣 <b>Join the Vern TTS Community</b>\n\n` +
      `Stay up to date with new releases, feature previews, and community polls!\n\n` +
      `👉 <b>Official Channel:</b> @myreader_veritas\n` +
      `👉 <b>GitHub Issues & Discussions:</b> <a href="${GITHUB_URL}/issues">Open on GitHub</a>`;
    await sendMessage(token, chatId, commMsg, {
      inline_keyboard: [
        [{ text: "📢 Open Channel @myreader_veritas", url: CHANNEL_URL }],
        [{ text: "🌐 Visit Website", url: WEBSITE_URL }]
      ]
    });
  } else if (text.includes("play store") || text.includes("google play")) {
    const playMsg = 
      `⏳ <b>Google Play Store Status</b>\n\n` +
      `Vern TTS is currently in review for Google Play Store publication!\n\n` +
      `In the meantime, you can download and install the official <b>arm64 release APK</b> directly today from GitHub.`;
    await sendMessage(token, chatId, playMsg, {
      inline_keyboard: [
        [{ text: "⬇️ Download Direct APK", callback_data: "cmd_download" }],
        [{ text: "📢 Follow Channel for Launch", url: CHANNEL_URL }]
      ]
    });
  } else {
    // Intelligent contextual fallback
    const fallback = 
      `🤖 <b>Vern TTS Assistant</b>\n\n` +
      `I can help answer your questions about offline reading, natural speech voices, document formats, and updates.\n\n` +
      `<i>Tap an option below:</i>`;
    await sendMessage(token, chatId, fallback, {
      inline_keyboard: [
        [
          { text: "⬇️ Download App", callback_data: "cmd_download" },
          { text: "🌟 Key Features", callback_data: "cmd_features" }
        ],
        [
          { text: "🎧 Natural Voice Setup", callback_data: "cmd_guide_voice" },
          { text: "❓ In-Depth FAQs", callback_data: "cmd_faq" }
        ]
      ]
    });
  }
}

async function handleCallbackQuery(cbQuery, env) {
  const token = env?.TELEGRAM_BOT_TOKEN || BOT_TOKEN;
  const chatId = cbQuery.message.chat.id;
  const messageId = cbQuery.message.message_id;
  const data = cbQuery.data;

  await answerCallbackQuery(token, cbQuery.id);

  if (data === "cmd_menu") {
    await sendWelcome(token, chatId, messageId);
  } else if (data === "cmd_download") {
    await sendDownloadInfo(token, chatId, messageId);
  } else if (data === "cmd_features") {
    await sendFeaturesMenu(token, chatId, messageId);
  } else if (data === "cmd_guide" || data === "cmd_guide_voice") {
    await sendGuideMasterclass(token, chatId, messageId);
  } else if (data === "cmd_classics") {
    await sendClassicsOverview(token, chatId, messageId);
  } else if (data === "cmd_faq") {
    await sendFaqMenu(token, chatId, messageId);
  } else if (data.startsWith("feat_")) {
    const topic = data.replace("feat_", "");
    await sendFeatureDetail(token, chatId, messageId, topic);
  } else if (data.startsWith("faq_")) {
    const topic = data.replace("faq_", "");
    await sendFaqDetail(token, chatId, messageId, topic);
  }
}
