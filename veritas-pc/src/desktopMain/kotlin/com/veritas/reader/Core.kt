package com.veritas.reader

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Intent
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

object PlaybackActions {
    const val ACTION_PLAY = "com.veritas.reader.action.PLAY"
    const val ACTION_PAUSE = "com.veritas.reader.action.PAUSE"
    const val ACTION_STOP = "com.veritas.reader.action.STOP"
    const val ACTION_NEXT = "com.veritas.reader.action.NEXT"
    const val ACTION_PREVIOUS = "com.veritas.reader.action.PREVIOUS"
    const val ACTION_JUMP_TO = "com.veritas.reader.action.JUMP_TO"
    const val ACTION_SPEAK_SELECTION = "com.veritas.reader.action.SPEAK_SELECTION"
    const val ACTION_UPDATE_PLAYBACK_SETTINGS = "com.veritas.reader.action.UPDATE_PLAYBACK_SETTINGS"
    const val ACTION_SET_SLEEP_TIMER = "com.veritas.reader.action.SET_SLEEP_TIMER"
    const val ACTION_CANCEL_SLEEP_TIMER = "com.veritas.reader.action.CANCEL_SLEEP_TIMER"
    const val ACTION_MEDIA_BUTTON = "android.intent.action.MEDIA_BUTTON"

    const val EXTRA_DOCUMENT_ID = "document_id"
    const val EXTRA_START_INDEX = "start_index"
    const val EXTRA_RATE = "rate"
    const val EXTRA_PITCH = "pitch"
    const val EXTRA_SELECTION_TEXT = "selection_text"
    const val EXTRA_CHAR_OFFSET = "char_offset"
    const val EXTRA_SLEEP_TIMER_DURATION_MILLIS = "sleep_timer_duration_millis"
    const val EXTRA_SLEEP_TIMER_ACTION = "sleep_timer_action"
    const val EXTRA_SLEEP_TIMER_STOP_AT_END_OF_SECTION = "sleep_timer_stop_at_end_of_section"
}

object PlaybackStateStore {
    var activeDocumentId by mutableStateOf<String?>(null)
    var documentTitle by mutableStateOf("")
    var sourceLabel by mutableStateOf("")
    var currentIndex by mutableIntStateOf(0)
    var chunkCount by mutableIntStateOf(0)
    var isPlaying by mutableStateOf(false)
    var isForegroundActive by mutableStateOf(false)
    var statusMessage by mutableStateOf("Ready.")
    var readerMode by mutableStateOf(ReaderMode.TEXT)
    var pendingPronunciationFixWord by mutableStateOf<String?>(null)
    // Whether the app UI is currently in the foreground. The PlaybackService uses this to
    // attribute listening time to background playback (foreground reading time is tracked
    // separately by the ViewModel's session timer, so this prevents double counting).
    var appInForeground by mutableStateOf(true)

    var sentenceCount: Int
        get() = chunkCount
        set(value) {
            chunkCount = value
        }
    var rate by mutableFloatStateOf(1.0f)
    var pitch by mutableFloatStateOf(1.0f)
    var queueCount by mutableIntStateOf(0)
    var autoPlayQueue by mutableStateOf(true)
    var currentSentenceStart by mutableIntStateOf(0)
    var currentSentenceEnd by mutableIntStateOf(0)
    var sleepTimerDurationMillis by mutableLongStateOf(0L)
    var sleepTimerEndsAtMillis by mutableLongStateOf(0L)
    var sleepTimerStopAtEndOfSection by mutableStateOf(false)
    var sleepTimerActionName by mutableStateOf(VeritasSleepTimerAction.PAUSE.name)

    val sleepTimerAction: VeritasSleepTimerAction
        get() = VeritasSleepTimerAction.fromName(sleepTimerActionName)

    fun activeSleepTimerSnapshot(nowMillis: Long = System.currentTimeMillis()): VeritasSleepTimerSnapshot? {
        val snapshot = VeritasSleepTimerSnapshot(
            durationMillis = sleepTimerDurationMillis,
            endsAtMillis = sleepTimerEndsAtMillis,
            action = sleepTimerAction,
            stopAtEndOfSection = sleepTimerStopAtEndOfSection
        )
        return snapshot.takeIf { sleepTimerStopAtEndOfSection || (sleepTimerDurationMillis > 0L && snapshot.isActive(nowMillis)) }
    }

    fun setSleepTimer(request: VeritasSleepTimerRequest, nowMillis: Long = System.currentTimeMillis()) {
        sleepTimerDurationMillis = request.durationMillis
        sleepTimerEndsAtMillis = if (request.stopAtEndOfSection) 0L else request.endsAt(nowMillis)
        sleepTimerActionName = request.action.name
        sleepTimerStopAtEndOfSection = request.stopAtEndOfSection
    }

    fun clearSleepTimer() {
        sleepTimerDurationMillis = 0L
        sleepTimerEndsAtMillis = 0L
        sleepTimerStopAtEndOfSection = false
        sleepTimerActionName = VeritasSleepTimerAction.PAUSE.name
    }

    fun reset() {
        activeDocumentId = null
        documentTitle = ""
        sourceLabel = ""
        currentIndex = 0
        chunkCount = 0
        isPlaying = false
        isForegroundActive = false
        statusMessage = "Ready."
        queueCount = 0
        currentSentenceStart = 0
        currentSentenceEnd = 0
        clearSleepTimer()
    }
}

private fun previewText(text: String): String {
    return ReaderTextIndex.stripInternalMarkers(text).replace(Regex("\\s+"), " ").trim().take(180)
}

data class SavedDocument(
    val id: String,
    val title: String,
    val fileName: String,
    val sourceLabel: String,
    val createdAt: Long,
    val updatedAt: Long,
    val currentIndex: Int,
    val chunkCount: Int,
    val charCount: Int,
    val preview: String,
    val favorite: Boolean = false,
    val collection: String = "",
    val originalFileName: String = "",
    val originalMimeType: String = "",
    val pageCount: Int = 0,
    val partial: Boolean = false,
    val language: String = ""
) {
    val sentenceCount: Int
        get() = chunkCount

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("fileName", fileName)
        .put("sourceLabel", sourceLabel)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .put("currentIndex", currentIndex)
        .put("chunkCount", chunkCount)
        .put("charCount", charCount)
        .put("preview", preview)
        .put("favorite", favorite)
        .put("collection", collection)
        .put("originalFileName", originalFileName)
        .put("originalMimeType", originalMimeType)
        .put("pageCount", pageCount)
        .put("partial", partial)
        .put("language", language)

    companion object {
        fun fromJson(obj: JSONObject): SavedDocument = SavedDocument(
            id = obj.optString("id"),
            title = obj.optString("title", "Untitled reading"),
            fileName = obj.optString("fileName"),
            sourceLabel = obj.optString("sourceLabel", "Text"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
            currentIndex = obj.optInt("currentIndex", 0),
            chunkCount = obj.optInt("chunkCount", 0),
            charCount = obj.optInt("charCount", 0),
            preview = obj.optString("preview"),
            favorite = obj.optBoolean("favorite", false),
            collection = obj.optString("collection", ""),
            originalFileName = obj.optString("originalFileName", ""),
            originalMimeType = obj.optString("originalMimeType", ""),
            pageCount = obj.optInt("pageCount", 0),
            partial = obj.optBoolean("partial", false),
            language = obj.optString("language", "")
        )
    }
}

data class QueueEntry(
    val documentId: String,
    val addedAt: Long
) {
    fun toJson(): JSONObject = JSONObject()
        .put("documentId", documentId)
        .put("addedAt", addedAt)

    companion object {
        fun fromJson(obj: JSONObject): QueueEntry = QueueEntry(
            documentId = obj.optString("documentId"),
            addedAt = obj.optLong("addedAt", System.currentTimeMillis())
        )
    }
}

data class ReadingHistoryEntry(
    val documentId: String,
    val title: String,
    val sourceLabel: String,
    val currentIndex: Int,
    val chunkCount: Int,
    val openedAt: Long
) {
    fun toJson(): JSONObject = JSONObject()
        .put("documentId", documentId)
        .put("title", title)
        .put("sourceLabel", sourceLabel)
        .put("currentIndex", currentIndex)
        .put("chunkCount", chunkCount)
        .put("openedAt", openedAt)

    companion object {
        fun fromJson(obj: JSONObject): ReadingHistoryEntry? {
            val documentId = obj.optString("documentId").trim()
            if (documentId.isBlank()) return null
            return ReadingHistoryEntry(
                documentId = documentId,
                title = obj.optString("title", "Untitled reading").ifBlank { "Untitled reading" },
                sourceLabel = obj.optString("sourceLabel", "Text").ifBlank { "Text" },
                currentIndex = obj.optInt("currentIndex", 0).coerceAtLeast(0),
                chunkCount = obj.optInt("chunkCount", 0).coerceAtLeast(0),
                openedAt = obj.optLong("openedAt", System.currentTimeMillis())
            )
        }
    }
}

data class GeneralNote(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val color: String? = null,
    val pinned: Boolean = false,
    val isChecklist: Boolean = false,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val reminderAt: Long? = null
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("content", content)
        .put("updatedAt", updatedAt)
        .put("color", color ?: "")
        .put("pinned", pinned)
        .put("isChecklist", isChecklist)
        .put("imageUrl", imageUrl ?: "")
        .put("audioUrl", audioUrl ?: "")
        .put("reminderAt", reminderAt ?: 0L)

    companion object {
        fun fromJson(obj: JSONObject): GeneralNote = GeneralNote(
            id = obj.optString("id", ""),
            title = obj.optString("title", ""),
            content = obj.optString("content", ""),
            updatedAt = obj.optLong("updatedAt", 0L),
            color = obj.optString("color", "").takeIf { it.isNotBlank() },
            pinned = obj.optBoolean("pinned", false),
            isChecklist = obj.optBoolean("isChecklist", false),
            imageUrl = obj.optString("imageUrl", "").takeIf { it.isNotBlank() },
            audioUrl = obj.optString("audioUrl", "").takeIf { it.isNotBlank() },
            reminderAt = obj.optLong("reminderAt", 0L).takeIf { it > 0L }
        )
    }
}

enum class VeritasScreen {
    TEXT_EDITOR,
    FILE_BROWSER,
    PDF_IMPORT_TOOLS,
    READER_SETTINGS,
    PRONUNCIATION_RULES,
    VOICE_STUDIO,
    NARRATION_STUDIO,
    AI_STUDY_TOOLS,
    OFFLINE_STUDY_TOOLS,
    AI_CENTER,
    ASK_AI_SETTINGS,
    TRANSLATION_TOOLS,
    SLEEP_TIMER,
    READING_LISTS,
    READING_HISTORY,
    DOCUMENT_NOTES,
    SETTINGS_HUB,
    BACKUP_TOOLS,
    SYNC_CENTER,
    APP_HEALTH,
    TUTORIAL,
    CANVAS_VIEW,
    GENERAL_NOTES_EDITOR
}

enum class AnnotationType {
    BOOKMARK,
    HIGHLIGHT,
    NOTE
}

data class ReaderAnnotation(
    val documentId: String,
    val chunkIndex: Int,
    val type: AnnotationType,
    val note: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val highlightColor: String? = null,
    val selectionGroupId: String? = null
) {
    val sentenceIndex: Int
        get() = chunkIndex

    val stableKey: String
        get() = "$documentId:$chunkIndex:${type.name}"

    fun toJson(): JSONObject = JSONObject()
        .put("documentId", documentId)
        .put("chunkIndex", chunkIndex)
        .put("type", type.name)
        .put("note", note)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
        .put("highlightColor", highlightColor)
        .put("selectionGroupId", selectionGroupId)

    companion object {
        fun fromJson(obj: JSONObject): ReaderAnnotation? {
            val type = runCatching { AnnotationType.valueOf(obj.optString("type")) }.getOrNull() ?: return null
            val documentId = obj.optString("documentId")
            if (documentId.isBlank()) return null
            return ReaderAnnotation(
                documentId = documentId,
                chunkIndex = obj.optInt("chunkIndex", 0),
                type = type,
                note = obj.optString("note"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                highlightColor = if (obj.has("highlightColor")) obj.optString("highlightColor") else null,
                selectionGroupId = if (obj.has("selectionGroupId")) obj.optString("selectionGroupId") else null
            )
        }
    }
}

data class FlashcardProgress(
    val id: String,
    val documentId: String,
    val front: String,
    val back: String,
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val nextReviewTime: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("documentId", documentId)
        .put("front", front)
        .put("back", back)
        .put("intervalDays", intervalDays)
        .put("easeFactor", easeFactor.toDouble())
        .put("repetitions", repetitions)
        .put("nextReviewTime", nextReviewTime)

    companion object {
        fun fromJson(json: JSONObject): FlashcardProgress {
            return FlashcardProgress(
                id = json.getString("id"),
                documentId = json.getString("documentId"),
                front = json.getString("front"),
                back = json.getString("back"),
                intervalDays = json.optInt("intervalDays", 1),
                easeFactor = json.optDouble("easeFactor", 2.5).toFloat(),
                repetitions = json.optInt("repetitions", 0),
                nextReviewTime = json.optLong("nextReviewTime", 0L)
            )
        }
    }
}

private const val DOCUMENT_NOTE_STABLE_KEY_PREFIX = "document-note:"

fun documentNoteStableKey(documentId: String): String = "$DOCUMENT_NOTE_STABLE_KEY_PREFIX$documentId"

fun documentIdFromDocumentNoteStableKey(stableKey: String): String? {
    if (!stableKey.startsWith(DOCUMENT_NOTE_STABLE_KEY_PREFIX)) return null
    return stableKey.removePrefix(DOCUMENT_NOTE_STABLE_KEY_PREFIX).takeIf { it.isNotBlank() }
}

data class ReaderDocument(
    val id: String?,
    val title: String,
    val sourceLabel: String,
    val rawText: String,
    val sentences: List<String>,
    val pageCount: Int = 0
) {
    // Legacy alias while older playback/storage code is migrated from chunks to sentences.
    val chunks: List<String>
        get() = sentences
}

sealed interface VeritasTextEditTarget {
    data class SentenceRange(
        val startSentenceIndex: Int,
        val endSentenceIndexExclusive: Int,
        val label: String
    ) : VeritasTextEditTarget

    data class Part(
        val partIndex: Int,
        val label: String
    ) : VeritasTextEditTarget
}

data class PronunciationRule(
    val id: String,
    val find: String,
    val replaceWith: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("find", find)
        .put("replaceWith", replaceWith)
        .put("enabled", enabled)
        .put("createdAt", createdAt)

    companion object {
        fun fromJson(obj: JSONObject): PronunciationRule? {
            val id = obj.optString("id").ifBlank { UUID.randomUUID().toString() }
            val find = obj.optString("find")
            if (find.isBlank()) return null
            return PronunciationRule(
                id = id,
                find = find,
                replaceWith = obj.optString("replaceWith"),
                enabled = obj.optBoolean("enabled", true),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}


object VeritasThemePackCatalog {
    const val DEFAULT_ID = "veritas_media"

    val packOptions: List<Pair<String, String>> = listOf(
        "veritas_media" to "Veritas Media",
        "material_you" to "Material You",
        "liquid_glass" to "Liquid Glass",
        "one_ui" to "One UI"
    )

    fun normalizePackId(id: String): String {
        return packOptions.firstOrNull { it.first == id }?.first ?: DEFAULT_ID
    }

    fun displayName(id: String): String {
        val normalized = normalizePackId(id)
        return packOptions.firstOrNull { it.first == normalized }?.second ?: "Veritas Media"
    }
}

object VeritasThemeCatalog {
    const val DEFAULT_ID = "system"

    val themeOptions: List<Pair<String, String>> = listOf(
        "system" to "System Default",
        "dark" to "Dark",
        "light" to "Light",
        "neon" to "Neon",
        "default_dark_2026" to "Default Dark 2026",
        "solarized_dark" to "Solarized Dark",
        "tomorrow_night_blue" to "Tomorrow Night Blue",
        "dark_high_contrast" to "Dark High Contrast",
        "white_high_contrast" to "White High Contrast",
        "bw_gradient_light" to "B/W Gradient Light",
        "bw_gradient_dark" to "B/W Gradient Dark",
        "blue_high_contrast" to "Blue High Contrast",
        "one_dark_pro" to "One Dark Pro",
        "github_dark" to "GitHub Dark",
        "github_light" to "GitHub Light",
        "dracula" to "Dracula"
    )

    fun normalizeThemeId(id: String): String {
        return themeOptions.firstOrNull { it.first == id }?.first ?: DEFAULT_ID
    }

    fun displayName(id: String): String {
        val normalized = normalizeThemeId(id)
        return themeOptions.firstOrNull { it.first == normalized }?.second ?: "Default Dark 2026"
    }
}

data class ReaderSettings(
    val fontSizeSp: Int = 18,
    val sectionSpacingDp: Int = 10,
    val showSectionNumbers: Boolean = true,
    val autoPlayQueue: Boolean = true,
    val themeId: String = VeritasThemeCatalog.DEFAULT_ID,
    val themePackId: String = "veritas_media",
    val adaptiveCover: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject()
        .put("fontSizeSp", fontSizeSp)
        .put("sectionSpacingDp", sectionSpacingDp)
        .put("showSectionNumbers", showSectionNumbers)
        .put("autoPlayQueue", autoPlayQueue)
        .put("themeId", themeId)
        .put("themePackId", themePackId)
        .put("adaptiveCover", adaptiveCover)

    companion object {
        fun fromJson(obj: JSONObject): ReaderSettings {
            val rawThemeId = obj.optString("themeId", VeritasThemeCatalog.DEFAULT_ID)
            val migratedPack = if (rawThemeId == "material_you") "material_you" else obj.optString("themePackId", "veritas_media")
            val migratedTheme = if (rawThemeId == "material_you") "default_dark_2026" else rawThemeId
            return ReaderSettings(
                fontSizeSp = obj.optInt("fontSizeSp", 18).coerceIn(14, 28),
                sectionSpacingDp = obj.optInt("sectionSpacingDp", 10).coerceIn(6, 24),
                showSectionNumbers = obj.optBoolean("showSectionNumbers", true),
                autoPlayQueue = obj.optBoolean("autoPlayQueue", true),
                themeId = VeritasThemeCatalog.normalizeThemeId(migratedTheme),
                themePackId = VeritasThemePackCatalog.normalizePackId(migratedPack),
                adaptiveCover = obj.optBoolean("adaptiveCover", false)
            )
        }
    }
}


data class VoiceSettings(
    val enginePackage: String = "",
    val engineLabel: String = "System default",
    val voiceName: String = "",
    val voiceLabel: String = "System default voice",
    val localeTag: String = "",
    val profileName: String = "Balanced",
    val preferredRate: Float = 1.0f,
    val preferredPitch: Float = 1.0f,
    val showNetworkVoices: Boolean = false
) {
    val displayName: String
        get() = if (voiceName.isBlank()) voiceLabel.ifBlank { "System default voice" } else voiceLabel.ifBlank { voiceName }

    fun toJson(): JSONObject = JSONObject()
        .put("enginePackage", enginePackage)
        .put("engineLabel", engineLabel)
        .put("voiceName", voiceName)
        .put("voiceLabel", voiceLabel)
        .put("localeTag", localeTag)
        .put("profileName", profileName)
        .put("preferredRate", preferredRate.toDouble())
        .put("preferredPitch", preferredPitch.toDouble())
        .put("showNetworkVoices", showNetworkVoices)

    companion object {
        fun fromJson(obj: JSONObject): VoiceSettings = VoiceSettings(
            enginePackage = obj.optString("enginePackage"),
            engineLabel = obj.optString("engineLabel", "System default"),
            voiceName = obj.optString("voiceName"),
            voiceLabel = obj.optString("voiceLabel", "System default voice"),
            localeTag = obj.optString("localeTag"),
            profileName = obj.optString("profileName", "Balanced"),
            preferredRate = obj.optDouble("preferredRate", 1.0).toFloat().coerceIn(0.5f, 2.0f),
            preferredPitch = obj.optDouble("preferredPitch", 1.0).toFloat().coerceIn(0.7f, 1.4f),
            showNetworkVoices = obj.optBoolean("showNetworkVoices", false)
        )
    }
}




data class NarrationSettings(
    val enabled: Boolean = false,
    val dialogueDetection: Boolean = true,
    val narratorRateMultiplier: Float = 1.0f,
    val narratorPitchMultiplier: Float = 1.0f,
    val dialogueRateMultiplier: Float = 1.03f,
    val dialoguePitchMultiplier: Float = 1.08f,
    val showDialogueBadges: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject()
        .put("enabled", enabled)
        .put("dialogueDetection", dialogueDetection)
        .put("narratorRateMultiplier", narratorRateMultiplier.toDouble())
        .put("narratorPitchMultiplier", narratorPitchMultiplier.toDouble())
        .put("dialogueRateMultiplier", dialogueRateMultiplier.toDouble())
        .put("dialoguePitchMultiplier", dialoguePitchMultiplier.toDouble())
        .put("showDialogueBadges", showDialogueBadges)

    companion object {
        fun fromJson(obj: JSONObject): NarrationSettings = NarrationSettings(
            enabled = obj.optBoolean("enabled", false),
            dialogueDetection = obj.optBoolean("dialogueDetection", true),
            narratorRateMultiplier = obj.optDouble("narratorRateMultiplier", 1.0).toFloat().coerceIn(0.75f, 1.25f),
            narratorPitchMultiplier = obj.optDouble("narratorPitchMultiplier", 1.0).toFloat().coerceIn(0.80f, 1.25f),
            dialogueRateMultiplier = obj.optDouble("dialogueRateMultiplier", 1.03).toFloat().coerceIn(0.75f, 1.25f),
            dialoguePitchMultiplier = obj.optDouble("dialoguePitchMultiplier", 1.08).toFloat().coerceIn(0.80f, 1.25f),
            showDialogueBadges = obj.optBoolean("showDialogueBadges", true)
        )
    }
}

data class AskAiSettings(
    val assistantId: String = "chooser",
    val assistantLabel: String = "Choose each time",
    val packageName: String = "",
    val promptTemplate: String = "Answer clearly using this selected Veritas text:\n\n{selection}"
) {
    fun toJson(): JSONObject = JSONObject()
        .put("assistantId", assistantId)
        .put("assistantLabel", assistantLabel)
        .put("packageName", packageName)
        .put("promptTemplate", promptTemplate)

    companion object {
        fun fromJson(obj: JSONObject): AskAiSettings = AskAiSettings(
            assistantId = obj.optString("assistantId", "chooser"),
            assistantLabel = obj.optString("assistantLabel", "Choose each time"),
            packageName = obj.optString("packageName"),
            promptTemplate = obj.optString(
                "promptTemplate",
                "Answer clearly using this selected Veritas text:\n\n{selection}"
            )
        )
    }
}

object NarrationAnalyzer {
    private val quotePattern = Regex("[\"“][^\"”]{4,}[\"”]|['‘](?:[^'’]|(?<=\\p{L})'(?=\\p{L})){4,}['’]")
    private val speakerPattern = Regex("\\b(said|asked|replied|answered|whispered|shouted|cried|murmured|continued|responded)\\b", RegexOption.IGNORE_CASE)
    private val dashDialoguePattern = Regex("^\\s*[—–-]\\s+\\S+")

    fun isDialogue(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false
        if (dashDialoguePattern.containsMatchIn(trimmed)) return true
        if (quotePattern.containsMatchIn(trimmed)) return true
        return speakerPattern.containsMatchIn(trimmed) && trimmed.any { it == '\"' || it == '“' || it == '”' || it == '‘' || it == '’' || it == '\'' }
    }

    fun labelFor(text: String, settings: NarrationSettings): String {
        if (!settings.enabled) return "Narrator"
        return if (settings.dialogueDetection && isDialogue(text)) "Dialogue" else "Narrator"
    }

    fun effectiveRate(baseRate: Float, settings: NarrationSettings, text: String): Float {
        val multiplier = if (settings.enabled && settings.dialogueDetection && isDialogue(text)) {
            settings.dialogueRateMultiplier
        } else if (settings.enabled) {
            settings.narratorRateMultiplier
        } else {
            1.0f
        }
        return (baseRate * multiplier).coerceIn(0.5f, 2.0f)
    }

    fun effectivePitch(basePitch: Float, settings: NarrationSettings, text: String): Float {
        val multiplier = if (settings.enabled && settings.dialogueDetection && isDialogue(text)) {
            settings.dialoguePitchMultiplier
        } else if (settings.enabled) {
            settings.narratorPitchMultiplier
        } else {
            1.0f
        }
        return (basePitch * multiplier).coerceIn(0.7f, 1.4f)
    }
}

data class ExtractedImport(
    val title: String,
    val text: String,
    val sourceLabel: String,
    val note: String? = null,
    val pageCount: Int = 0,
    val partial: Boolean = false
)

data class DocumentCreateResult(
    val document: SavedDocument,
    val originalCopyError: String? = null
)


data class BackupRestoreResult(
    val documentCount: Int,
    val annotationCount: Int,
    val queueCount: Int,
    val readingListCount: Int,
    val pronunciationRuleCount: Int,
    val restoredReaderSettings: Boolean,
    val restoredVoiceSettings: Boolean
)

data class AiPromptTemplate(
    val id: String,
    val title: String,
    val instruction: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("instruction", instruction)
        .put("createdAt", createdAt)

    companion object {
        fun fromJson(obj: JSONObject): AiPromptTemplate? {
            val instruction = obj.optString("instruction").trim()
            if (instruction.isBlank()) return null
            return AiPromptTemplate(
                id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                title = obj.optString("title", "Custom prompt").ifBlank { "Custom prompt" },
                instruction = instruction,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

data class AiPromptHistoryEntry(
    val id: String,
    val documentTitle: String,
    val promptType: String,
    val scope: String,
    val promptPreview: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("documentTitle", documentTitle)
        .put("promptType", promptType)
        .put("scope", scope)
        .put("promptPreview", promptPreview)
        .put("createdAt", createdAt)

    companion object {
        fun fromJson(obj: JSONObject): AiPromptHistoryEntry? {
            val preview = obj.optString("promptPreview").trim()
            if (preview.isBlank()) return null
            return AiPromptHistoryEntry(
                id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                documentTitle = obj.optString("documentTitle", "Untitled document"),
                promptType = obj.optString("promptType", "AI prompt"),
                scope = obj.optString("scope", "whole document"),
                promptPreview = preview,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}

data class VeritasDocumentOutlineEntry(
    val title: String,
    val targetIndex: Int,
    val pageNumber: Int? = null,
    val level: Int = 0,
    val source: String = "Smart outline"
)

data class ReaderTrackerDay(
    val dateKey: String,
    val appOpenCount: Int = 0,
    val usageMillis: Long = 0L,
    val readDocumentIds: Set<String> = emptySet()
) {
    fun toJson(): JSONObject = JSONObject()
        .put("dateKey", dateKey)
        .put("appOpenCount", appOpenCount)
        .put("usageMillis", usageMillis)
        .put("readDocumentIds", JSONArray().also { array -> readDocumentIds.sorted().forEach(array::put) })

    companion object {
        fun fromJson(obj: JSONObject): ReaderTrackerDay {
            val ids = mutableSetOf<String>()
            val array = obj.optJSONArray("readDocumentIds") ?: JSONArray()
            for (i in 0 until array.length()) {
                array.optString(i).trim().takeIf { it.isNotBlank() }?.let(ids::add)
            }
            return ReaderTrackerDay(
                dateKey = obj.optString("dateKey"),
                appOpenCount = obj.optInt("appOpenCount", 0).coerceAtLeast(0),
                usageMillis = obj.optLong("usageMillis", 0L).coerceAtLeast(0L),
                readDocumentIds = ids
            )
        }
    }
}

data class ReaderTrackerCompletion(
    val documentId: String,
    val title: String,
    val completedAt: Long
) {
    fun toJson(): JSONObject = JSONObject()
        .put("documentId", documentId)
        .put("title", title)
        .put("completedAt", completedAt)

    companion object {
        fun fromJson(obj: JSONObject): ReaderTrackerCompletion? {
            val documentId = obj.optString("documentId").trim()
            if (documentId.isBlank()) return null
            return ReaderTrackerCompletion(
                documentId = documentId,
                title = obj.optString("title", "Untitled reading").ifBlank { "Untitled reading" },
                completedAt = obj.optLong("completedAt", 0L)
            )
        }
    }
}

/** One week of Monday-first daily reading totals, for the swipeable weekly bar chart. */
data class WeekBars(
    val label: String,
    val values: List<Long>,
    val totalMillis: Long,
    val isCurrentWeek: Boolean
)

data class ReaderTrackerSnapshot(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyUsageMillis: Long = 0L,
    val weeklyAverageMillis: Long = 0L,
    val documentsReadThisWeek: Int = 0,
    val documentsCompletedThisMonth: Int = 0,
    val weeklyUsageByDay: List<Long> = List(7) { 0L },
    val weeklyHistory: List<WeekBars> = emptyList(),
    val recentCompletions: List<ReaderTrackerCompletion> = emptyList(),
    val activeDateKeys: Set<String> = emptySet()
) {
    companion object {
        fun empty(): ReaderTrackerSnapshot = ReaderTrackerSnapshot()
    }
}

object ReaderTrackerMath {
    const val COMPLETION_THRESHOLD = 0.90f

    fun currentStreak(openDateKeys: Set<String>, todayKey: String): Int {
        return streakEndingAt(openDateKeys, todayKey)
    }

    fun longestStreak(openDateKeys: Set<String>): Int {
        if (openDateKeys.isEmpty()) return 0
        var longest = 0
        openDateKeys.sorted().forEach { key ->
            longest = maxOf(longest, streakEndingAt(openDateKeys, key))
        }
        return longest
    }

    fun weeklyUsage(days: List<ReaderTrackerDay>, weekKeys: List<String>): Pair<Long, Long> {
        val byDate = days.associateBy { it.dateKey }
        val usage = weekKeys.map { key -> byDate[key]?.usageMillis ?: 0L }
        val total = usage.sum()
        return total to if (usage.isEmpty()) 0L else total / usage.size
    }

    fun monthCompletionCount(completions: List<ReaderTrackerCompletion>, monthPrefix: String, dateKeyFor: (Long) -> String): Int {
        return completions
            .filter { completion -> dateKeyFor(completion.completedAt).startsWith(monthPrefix) }
            .distinctBy { it.documentId }
            .size
    }

    private fun streakEndingAt(openDateKeys: Set<String>, endingKey: String): Int {
        var cursor = calendarFromDateKey(endingKey) ?: return 0
        var streak = 0
        while (true) {
            val key = dateKey(cursor.timeInMillis)
            if (key !in openDateKeys) return streak
            streak += 1
            cursor.add(Calendar.DAY_OF_YEAR, -1)
        }
    }

    fun dateKey(timestamp: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))

    private fun calendarFromDateKey(key: String): Calendar? {
        val date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key) }.getOrNull() ?: return null
        return Calendar.getInstance().apply { time = date }
    }
}

data class QuestProgress(
    val tourDone: Boolean,
    val importDone: Boolean,
    val speedDone: Boolean,
    val bookmarkDone: Boolean
)

class DocumentRepository(context: Context) {
    private val appContext = context
    private val userHome = System.getProperty("user.home")
    private val dataDir = File(userHome, ".veritas_reader").apply { mkdirs() }
    private val prefs = DesktopPreferences()
    private val docsDir: File = File(dataDir, "reader_documents").apply { mkdirs() }
    private val originalsDir: File = File(dataDir, "original_documents").apply { mkdirs() }

    fun saveDocumentTitle(documentId: String, title: String) {
        val raw = prefs.getString("document_titles", "{}") ?: "{}"
        val json = JSONObject(raw)
        json.put(documentId, title)
        prefs.edit().putString("document_titles", json.toString()).apply()
    }

    fun getDocumentTitle(documentId: String): String {
        val raw = prefs.getString("document_titles", "{}") ?: "{}"
        val json = JSONObject(raw)
        return json.optString(documentId, "Deleted Book")
    }

    fun loadAllDocumentTitles(): Map<String, String> {
        val raw = prefs.getString("document_titles", "{}") ?: "{}"
        val json = JSONObject(raw)
        val map = mutableMapOf<String, String>()
        
        // Auto-backfill active document titles
        runCatching {
            loadDocuments().forEach { doc ->
                map[doc.id] = doc.title
                if (!json.has(doc.id)) {
                    json.put(doc.id, doc.title)
                }
            }
        }
        
        json.keys().forEach { key ->
            map[key] = json.optString(key, "Deleted Book")
        }
        
        runCatching {
            prefs.edit().putString("document_titles", json.toString()).apply()
        }
        return map
    }

    // Sleep timer state is mirrored to prefs so a process kill mid-timer does not
    // silently lose it; the service restores and re-schedules on creation.
    fun saveSleepTimerState(durationMillis: Long, endsAtMillis: Long, actionName: String, stopAtEndOfSection: Boolean) {
        prefs.edit()
            .putLong("sleep_timer_duration", durationMillis)
            .putLong("sleep_timer_ends_at", endsAtMillis)
            .putString("sleep_timer_action", actionName)
            .putBoolean("sleep_timer_stop_at_section_end", stopAtEndOfSection)
            .apply()
    }

    fun clearSleepTimerState() {
        prefs.edit()
            .remove("sleep_timer_duration")
            .remove("sleep_timer_ends_at")
            .remove("sleep_timer_action")
            .remove("sleep_timer_stop_at_section_end")
            .apply()
    }

    fun loadPersistedSleepTimer(nowMillis: Long = System.currentTimeMillis()): VeritasSleepTimerSnapshot? {
        val duration = prefs.getLong("sleep_timer_duration", 0L)
        val endsAt = prefs.getLong("sleep_timer_ends_at", 0L)
        val stopAtEnd = prefs.getBoolean("sleep_timer_stop_at_section_end", false)
        if (!stopAtEnd && (duration <= 0L || endsAt <= nowMillis)) {
            clearSleepTimerState()
            return null
        }
        return VeritasSleepTimerSnapshot(
            durationMillis = duration,
            endsAtMillis = endsAt,
            action = VeritasSleepTimerAction.fromName(prefs.getString("sleep_timer_action", null)),
            stopAtEndOfSection = stopAtEnd
        )
    }

    fun recordDocReadingTime(documentId: String, durationMillis: Long, nowMillis: Long = System.currentTimeMillis()) {
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(nowMillis))
        val prefKey = "monthly_reading_time_$monthKey"
        // The reading-time counter is read-modify-written from both the UI (foreground session
        // timer) and the PlaybackService (background listening). Without a process-wide lock the
        // two could interleave and lose an update. Serialise the whole RMW.
        synchronized(LIBRARY_WRITE_LOCK) {
            val raw = prefs.getString(prefKey, "{}") ?: "{}"
            val json = runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
            val currentVal = json.optLong(documentId, 0L)
            json.put(documentId, currentVal + durationMillis)
            // apply() (not commit()) so this never blocks the caller's thread on disk I/O —
            // this runs on the main thread from the service's onDone. The lock guarantees the
            // in-memory read-modify-write is consistent across the UI and service.
            prefs.edit().putString(prefKey, json.toString()).apply()
        }
    }

    fun loadDocReadingTimes(nowMillis: Long = System.currentTimeMillis()): Map<String, Long> {
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(nowMillis))
        val prefKey = "monthly_reading_time_$monthKey"
        val raw = prefs.getString(prefKey, "{}") ?: "{}"
        val json = runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
        val map = mutableMapOf<String, Long>()
        json.keys().forEach { key ->
            map[key] = json.optLong(key, 0L)
        }
        return map
    }

    fun loadDocuments(): List<SavedDocument> {
        val array = readResilientJsonArray(KEY_DOCUMENTS)
        val docs = mutableListOf<SavedDocument>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val doc = runCatching { SavedDocument.fromJson(item) }.getOrNull() ?: continue
            if (doc.id.isNotBlank() && doc.fileName.isNotBlank() && File(docsDir, doc.fileName).exists()) {
                docs.add(doc)
            }
        }
        return docs.sortedByDescending { it.updatedAt }
    }

    fun findDocument(documentId: String): SavedDocument? {
        return loadDocuments().firstOrNull { it.id == documentId }
    }

    fun createDocument(
        title: String,
        text: String,
        sourceLabel: String,
        originalUri: Uri? = null,
        originalDisplayName: String = title,
        originalMimeType: String = "",
        pageCount: Int = 0,
        partial: Boolean = false,
        language: String = ""
    ): SavedDocument = createDocumentWithResult(
        title = title,
        text = text,
        sourceLabel = sourceLabel,
        originalUri = originalUri,
        originalDisplayName = originalDisplayName,
        originalMimeType = originalMimeType,
        pageCount = pageCount,
        partial = partial,
        language = language
    ).document

    fun createDocumentWithResult(
        title: String,
        text: String,
        sourceLabel: String,
        originalUri: Uri? = null,
        originalDisplayName: String = title,
        originalMimeType: String = "",
        pageCount: Int = 0,
        partial: Boolean = false,
        language: String = ""
    ): DocumentCreateResult {
        val normalizedTitle = title.trim().ifBlank { "Untitled reading" }
        val id = UUID.randomUUID().toString()
        val fileName = "$id.txt"
        File(docsDir, fileName).writeText(text, Charsets.UTF_8)
        saveDocumentTitle(id, normalizedTitle)
        val originalFileResult = originalUri?.let { saveOriginalFileReference(id, it, originalDisplayName) }
        val originalFileName = originalFileResult?.getOrNull().orEmpty()
        val fileErrorNote = originalFileResult?.exceptionOrNull()?.let {
            "Note: Original file could not be saved (${it.message}). PDF/image viewer may not be available."
        }

        val chunks = TextChunker.chunk(text)
        val now = System.currentTimeMillis()
        val doc = SavedDocument(
            id = id,
            title = normalizedTitle,
            fileName = fileName,
            sourceLabel = sourceLabel.ifBlank { "Text" },
            createdAt = now,
            updatedAt = now,
            currentIndex = 0,
            chunkCount = chunks.size,
            charCount = text.length,
            preview = previewText(text),
            originalFileName = originalFileName,
            originalMimeType = originalMimeType,
            pageCount = pageCount.coerceAtLeast(0),
            partial = partial,
            language = language
        )

        saveDocuments(listOf(doc) + loadDocuments().filterNot { it.id == id })

        // Log file copy errors
        if (fileErrorNote != null) {
            Log.w(TAG, "Original file save error for $id: ${originalFileResult.exceptionOrNull()?.message}")
        }

        return DocumentCreateResult(doc, fileErrorNote)
    }

    fun readText(document: SavedDocument): String {
        return runCatching { File(docsDir, document.fileName).readText(Charsets.UTF_8) }.getOrDefault("")
    }

    fun updateDocumentText(documentId: String, text: String): SavedDocument? {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return null
        val documents = loadDocuments()
        val target = documents.firstOrNull { it.id == documentId } ?: return null
        val oldChunks = runCatching {
            File(docsDir, target.fileName).takeIf { it.exists() }
                ?.readText(Charsets.UTF_8)
                ?.let { TextChunker.chunk(it) }
        }.getOrNull().orEmpty()
        ReaderTextModelCache.invalidate(documentId)
        File(docsDir, target.fileName).writeText(cleanText, Charsets.UTF_8)
        val chunks = TextChunker.chunk(cleanText)
        remapAnnotationsAfterEdit(documentId, oldChunks, chunks)
        val now = System.currentTimeMillis()
        val updatedTarget = target.copy(
            updatedAt = now,
            currentIndex = target.currentIndex.coerceIn(0, (chunks.size - 1).coerceAtLeast(0)),
            chunkCount = chunks.size,
            charCount = cleanText.length,
            preview = previewText(cleanText),
            pageCount = target.pageCount.takeIf { it > 0 } ?: ReaderTextIndex.build(cleanText).pageCount,
            partial = false
        )
        saveDocuments(listOf(updatedTarget) + documents.filterNot { it.id == documentId })
        return updatedTarget
    }

    // Annotations are anchored by chunk index; an edit can renumber chunks and leave
    // bookmarks/notes pointing at the wrong sentence. Re-anchor each one by matching
    // its original sentence text in the new chunk list.
    private fun remapAnnotationsAfterEdit(documentId: String, oldChunks: List<String>, newChunks: List<String>) {
        if (oldChunks.isEmpty() || newChunks.isEmpty()) return
        val all = loadAllAnnotations()
        if (all.none { it.documentId == documentId }) return
        val newIndexByText = HashMap<String, MutableList<Int>>()
        newChunks.forEachIndexed { idx, chunk ->
            newIndexByText.getOrPut(normalizeChunkForRemap(chunk)) { mutableListOf() }.add(idx)
        }
        var changed = false
        val remapped = all.map { ann ->
            if (ann.documentId != documentId) return@map ann
            val oldText = oldChunks.getOrNull(ann.chunkIndex) ?: return@map ann
            val candidates = newIndexByText[normalizeChunkForRemap(oldText)]
            val newIndex = if (candidates.isNullOrEmpty()) {
                // Sentence no longer exists verbatim; keep the position but stay in bounds.
                ann.chunkIndex.coerceIn(0, newChunks.lastIndex)
            } else {
                candidates.minByOrNull { kotlin.math.abs(it - ann.chunkIndex) } ?: ann.chunkIndex
            }
            if (newIndex != ann.chunkIndex) {
                changed = true
                ann.copy(chunkIndex = newIndex)
            } else {
                ann
            }
        }
        if (changed) saveAllAnnotations(remapped.distinctBy { it.stableKey })
    }

    private fun normalizeChunkForRemap(chunk: String): String =
        chunk.trim().replace(Regex("\\s+"), " ").lowercase(Locale.US)

    fun appendDocumentText(documentId: String, text: String, isComplete: Boolean = false): SavedDocument? {
        val cleanText = text.trim()
        if (cleanText.isBlank() && !isComplete) return null
        
        val documents = loadDocuments()
        val target = documents.firstOrNull { it.id == documentId } ?: return null
        ReaderTextModelCache.invalidate(documentId)
        
        val file = File(docsDir, target.fileName)
        if (cleanText.isNotBlank()) {
            file.appendText("\n\n" + cleanText, Charsets.UTF_8)
        }
        
        val newChunks = TextChunker.chunk(cleanText)
        val now = System.currentTimeMillis()
        
        val updatedTarget = target.copy(
            updatedAt = now,
            chunkCount = target.chunkCount + newChunks.size,
            charCount = target.charCount + cleanText.length + 2, // +2 for the \n\n
            partial = !isComplete
        )
        saveDocuments(listOf(updatedTarget) + documents.filterNot { it.id == documentId })
        return updatedTarget
    }

    fun originalFile(document: SavedDocument): File? {
        if (document.originalFileName.isBlank() || document.originalFileName.startsWith("content://")) return null
        return File(originalsDir, document.originalFileName).takeIf { it.exists() }
    }

    fun originalUri(document: SavedDocument): Uri? {
        val name = document.originalFileName
        if (name.isBlank()) return null
        if (name.startsWith("content://")) {
            return Uri.parse(name)
        }
        val file = File(originalsDir, name)
        if (!file.exists()) return null
        return runCatching {
            Uri.parse(file.absolutePath)
        }.getOrNull()
    }

    private fun saveOriginalFileReference(documentId: String, uri: Uri, displayName: String): Result<String> {
        // Always copy the original file to the app sandbox to guarantee persistent access 
        // across app sessions, background workers, and different UI components.

        
        val extension = displayName.substringAfterLast('.', "").takeIf { it.length in 1..8 } ?: "bin"
        val safeExtension = extension.replace(Regex("[^A-Za-z0-9]"), "").ifBlank { "bin" }
        val fileName = "$documentId.$safeExtension"
        val target = File(originalsDir, fileName)
        return try {
            File(uri.path ?: uri.toString()).inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return Result.failure(IllegalStateException("Could not open original file for reading."))
            Result.success(fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy original file $displayName to $fileName", e)
            runCatching { target.delete() }
            Result.failure(e)
        }
    }

    fun buildBackupJson(): String {
        val root = JSONObject()
            .put("schema", "veritas.reader.backup.v1")
            .put("createdAt", System.currentTimeMillis())
            .put(
                "appVersion",
                runCatching {
                    appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
                }.getOrNull() ?: "1.1.0"
            )
            .put("syncPeer", "android")

        val documentArray = JSONArray()
        loadDocuments().forEach { document ->
            documentArray.put(
                document.toJson()
                    .put("text", readText(document))
            )
        }
        root.put("documents", documentArray)

        val queueArray = JSONArray()
        loadQueueEntries().forEach { queueArray.put(it.toJson()) }
        root.put("queue", queueArray)

        root.put("readingLists", loadReadingListCatalog().toJsonArray())

        val readingHistoryArray = JSONArray()
        loadReadingHistory().forEach { readingHistoryArray.put(it.toJson()) }
        root.put("readingHistory", readingHistoryArray)

        val annotationArray = JSONArray()
        loadAllAnnotations().forEach { annotationArray.put(it.toJson()) }
        root.put("annotations", annotationArray)

        val documentNotesArray = JSONArray()
        loadDocumentNotes().forEach { (documentId, note) ->
            documentNotesArray.put(
                JSONObject()
                    .put("documentId", documentId)
                    .put("note", note)
            )
        }
        root.put("documentNotes", documentNotesArray)

        val pronunciationArray = JSONArray()
        loadPronunciationRules().forEach { pronunciationArray.put(it.toJson()) }
        root.put("pronunciationRules", pronunciationArray)

        root.put("readerSettings", loadReaderSettings().toJson())
        root.put("voiceSettings", loadVoiceSettings().toJson())
        root.put("narrationSettings", loadNarrationSettings().toJson())
        root.put("askAiSettings", loadAskAiSettings().toJson())

        val aiTemplateArray = JSONArray()
        loadAiPromptTemplates().forEach { aiTemplateArray.put(it.toJson()) }
        root.put("aiPromptTemplates", aiTemplateArray)

        val aiHistoryArray = JSONArray()
        loadAiPromptHistory().forEach { aiHistoryArray.put(it.toJson()) }
        root.put("aiPromptHistory", aiHistoryArray)
        return root.toString(2)
    }

    fun restoreBackupJson(rawJson: String, replaceExisting: Boolean = false): BackupRestoreResult {
        val root = runCatching { JSONObject(rawJson) }
            .getOrElse { throw IllegalArgumentException("This is not a valid Veritas backup file.") }
        val documentArray = root.optJSONArray("documents")
            ?: throw IllegalArgumentException("The backup does not contain a documents section.")

        if (replaceExisting) {
            loadDocuments().forEach { document ->
                runCatching { File(docsDir, document.fileName).delete() }
                runCatching { originalFile(document)?.delete() }
                runCatching { CoverExtractor.deleteCover(appContext, document.id) }
            }
        }

        val existingDocuments = if (replaceExisting) emptyList() else loadDocuments()
        val existingById = existingDocuments.associateBy { it.id }
        val usedIds = existingDocuments.map { it.id }.toMutableSet()
        val idMap = mutableMapOf<String, String>()
        val importedDocuments = mutableListOf<SavedDocument>()

        for (i in 0 until documentArray.length()) {
            val obj = documentArray.optJSONObject(i) ?: continue
            val originalId = obj.optString("id").trim()
            val incomingId = originalId.ifBlank { UUID.randomUUID().toString() }
            val text = obj.optString("text")
            if (text.isBlank()) continue

            val existingMatch = existingById[incomingId]
            var newId = when {
                existingMatch != null -> existingMatch.id
                incomingId !in usedIds -> incomingId
                else -> UUID.randomUUID().toString()
            }
            while (newId in usedIds && existingMatch?.id != newId) newId = UUID.randomUUID().toString()
            if (existingMatch?.id != newId) usedIds.add(newId)
            idMap[incomingId] = newId
            if (originalId.isNotBlank()) idMap[originalId] = newId

            val fileName = existingMatch?.fileName ?: "$newId.txt"
            File(docsDir, fileName).writeText(text, Charsets.UTF_8)
            val chunks = TextChunker.chunk(text)
            val base = runCatching { SavedDocument.fromJson(obj) }.getOrNull() ?: existingMatch
            val now = System.currentTimeMillis()
            val document = (base ?: SavedDocument(
                id = newId,
                title = obj.optString("title", "Restored reading"),
                fileName = fileName,
                sourceLabel = obj.optString("sourceLabel", "Backup"),
                createdAt = obj.optLong("createdAt", now),
                updatedAt = now,
                currentIndex = 0,
                chunkCount = chunks.size,
                charCount = text.length,
                preview = previewText(text)
            )).copy(
                id = newId,
                fileName = fileName,
                currentIndex = obj.optInt("currentIndex", 0).coerceIn(0, (chunks.size - 1).coerceAtLeast(0)),
                chunkCount = chunks.size,
                charCount = text.length,
                preview = previewText(text),
                updatedAt = now
            )
            importedDocuments.add(document)
        }

        val finalDocuments = (importedDocuments + existingDocuments).distinctBy { it.id }
        saveDocuments(finalDocuments)

        val importedAnnotations = mutableListOf<ReaderAnnotation>()
        root.optJSONArray("annotations")?.let { array ->
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val annotation = ReaderAnnotation.fromJson(obj) ?: continue
                val mappedId = idMap[annotation.documentId] ?: continue
                importedAnnotations.add(annotation.copy(documentId = mappedId))
            }
        }
        val finalAnnotations = if (replaceExisting) {
            importedAnnotations
        } else {
            loadAllAnnotations() + importedAnnotations
        }.distinctBy { it.stableKey }
        saveAllAnnotations(finalAnnotations)

        val importedDocumentNotes = mutableMapOf<String, String>()
        root.optJSONArray("documentNotes")?.let { array ->
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val mappedId = idMap[obj.optString("documentId")] ?: continue
                val note = obj.optString("note").trim()
                if (note.isNotBlank()) importedDocumentNotes[mappedId] = note
            }
        }
        val finalDocumentNotes = if (replaceExisting) {
            importedDocumentNotes
        } else {
            loadDocumentNotes() + importedDocumentNotes
        }
        saveDocumentNotes(finalDocumentNotes)

        val importedQueue = mutableListOf<QueueEntry>()
        root.optJSONArray("queue")?.let { array ->
            for (i in 0 until array.length()) {
                val entry = QueueEntry.fromJson(array.optJSONObject(i) ?: continue)
                val mappedId = idMap[entry.documentId] ?: continue
                importedQueue.add(entry.copy(documentId = mappedId))
            }
        }
        val finalQueue = if (replaceExisting) importedQueue else loadQueueEntries() + importedQueue
        saveQueueEntries(finalQueue.distinctBy { it.documentId })

        val importedReadingLists = importReadingLists(root.optJSONArray("readingLists"), idMap, finalDocuments, replaceExisting)

        val importedHistory = mutableListOf<ReadingHistoryEntry>()
        root.optJSONArray("readingHistory")?.let { array ->
            for (i in 0 until array.length()) {
                val entry = ReadingHistoryEntry.fromJson(array.optJSONObject(i) ?: continue) ?: continue
                val mappedId = idMap[entry.documentId] ?: continue
                val mappedDocument = finalDocuments.firstOrNull { it.id == mappedId } ?: continue
                importedHistory.add(
                    entry.copy(
                        documentId = mappedId,
                        title = mappedDocument.title,
                        sourceLabel = mappedDocument.sourceLabel,
                        currentIndex = entry.currentIndex.coerceIn(0, (mappedDocument.chunkCount - 1).coerceAtLeast(0)),
                        chunkCount = mappedDocument.chunkCount
                    )
                )
            }
        }
        if (importedHistory.isNotEmpty() || replaceExisting) {
            val finalHistory = if (replaceExisting) importedHistory else importedHistory + loadReadingHistory()
            saveReadingHistory(finalHistory.sortedByDescending { it.openedAt }.distinctBy { it.documentId })
        }

        val importedRules = mutableListOf<PronunciationRule>()
        root.optJSONArray("pronunciationRules")?.let { array ->
            for (i in 0 until array.length()) {
                val rule = PronunciationRule.fromJson(array.optJSONObject(i) ?: continue) ?: continue
                importedRules.add(rule)
            }
        }
        if (importedRules.isNotEmpty()) {
            val finalRules = if (replaceExisting) importedRules else importedRules + loadPronunciationRules()
            savePronunciationRules(finalRules.distinctBy { it.id })
        }

        val restoredReaderSettings = root.optJSONObject("readerSettings")?.let {
            saveReaderSettings(ReaderSettings.fromJson(it)); true
        } ?: false
        val restoredVoiceSettings = root.optJSONObject("voiceSettings")?.let {
            saveVoiceSettings(VoiceSettings.fromJson(it)); true
        } ?: false
        root.optJSONObject("narrationSettings")?.let {
            saveNarrationSettings(NarrationSettings.fromJson(it))
        }
        root.optJSONObject("askAiSettings")?.let {
            saveAskAiSettings(AskAiSettings.fromJson(it))
        }

        root.optJSONArray("aiPromptTemplates")?.let { array ->
            val imported = mutableListOf<AiPromptTemplate>()
            for (i in 0 until array.length()) {
                AiPromptTemplate.fromJson(array.optJSONObject(i) ?: continue)?.let { imported.add(it) }
            }
            if (imported.isNotEmpty()) {
                saveAiPromptTemplates((loadAiPromptTemplates() + imported).distinctBy { it.id })
            }
        }

        root.optJSONArray("aiPromptHistory")?.let { array ->
            val imported = mutableListOf<AiPromptHistoryEntry>()
            for (i in 0 until array.length()) {
                AiPromptHistoryEntry.fromJson(array.optJSONObject(i) ?: continue)?.let { imported.add(it) }
            }
            if (imported.isNotEmpty()) {
                saveAiPromptHistory((loadAiPromptHistory() + imported).distinctBy { it.id }.sortedByDescending { it.createdAt }.take(50))
            }
        }

        return BackupRestoreResult(
            documentCount = importedDocuments.size,
            annotationCount = importedAnnotations.size,
            queueCount = importedQueue.size,
            readingListCount = importedReadingLists,
            pronunciationRuleCount = importedRules.size,
            restoredReaderSettings = restoredReaderSettings,
            restoredVoiceSettings = restoredVoiceSettings
        )
    }

    fun loadAiPromptTemplates(): List<AiPromptTemplate> {
        val raw = prefs.getString(KEY_AI_TEMPLATES, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val items = mutableListOf<AiPromptTemplate>()
        for (i in 0 until array.length()) {
            AiPromptTemplate.fromJson(array.optJSONObject(i) ?: continue)?.let { items.add(it) }
        }
        return items.sortedByDescending { it.createdAt }
    }

    fun saveAiPromptTemplates(templates: List<AiPromptTemplate>) {
        val array = JSONArray()
        templates.take(40).forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_AI_TEMPLATES, array.toString()) }
    }

    fun addAiPromptTemplate(title: String, instruction: String): List<AiPromptTemplate> {
        val cleanInstruction = instruction.trim()
        if (cleanInstruction.isBlank()) return loadAiPromptTemplates()
        val template = AiPromptTemplate(
            id = UUID.randomUUID().toString(),
            title = title.trim().ifBlank { "Custom prompt" },
            instruction = cleanInstruction
        )
        saveAiPromptTemplates(listOf(template) + loadAiPromptTemplates())
        return loadAiPromptTemplates()
    }

    fun deleteAiPromptTemplate(id: String): List<AiPromptTemplate> {
        saveAiPromptTemplates(loadAiPromptTemplates().filterNot { it.id == id })
        return loadAiPromptTemplates()
    }

    fun loadAiPromptHistory(): List<AiPromptHistoryEntry> {
        val raw = prefs.getString(KEY_AI_HISTORY, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val items = mutableListOf<AiPromptHistoryEntry>()
        for (i in 0 until array.length()) {
            AiPromptHistoryEntry.fromJson(array.optJSONObject(i) ?: continue)?.let { items.add(it) }
        }
        return items.sortedByDescending { it.createdAt }
    }

    private fun saveAiPromptHistory(history: List<AiPromptHistoryEntry>) {
        val array = JSONArray()
        history.take(50).forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_AI_HISTORY, array.toString()) }
    }

    fun addAiPromptHistory(documentTitle: String, promptType: String, scope: String, prompt: String): List<AiPromptHistoryEntry> {
        val preview = prompt.replace(Regex("\\s+"), " ").trim().take(420)
        if (preview.isBlank()) return loadAiPromptHistory()
        val entry = AiPromptHistoryEntry(
            id = UUID.randomUUID().toString(),
            documentTitle = documentTitle.ifBlank { "Untitled document" },
            promptType = promptType,
            scope = scope,
            promptPreview = preview
        )
        saveAiPromptHistory(listOf(entry) + loadAiPromptHistory())
        return loadAiPromptHistory()
    }

    fun clearAiPromptHistory(): List<AiPromptHistoryEntry> {
        saveAiPromptHistory(emptyList())
        return emptyList()
    }

    fun hasSeenOnboardingTutorial(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_TUTORIAL_SEEN, false)
    }

    fun markOnboardingTutorialSeen() {
        prefs.edit { putBoolean(KEY_ONBOARDING_TUTORIAL_SEEN, true) }
    }

    fun resetOnboardingState() {
        prefs.edit { putBoolean(KEY_ONBOARDING_TUTORIAL_SEEN, false) }
    }

    fun loadUserName(): String {
        return prefs.getString(KEY_USER_NAME, "")?.trim().orEmpty()
    }

    fun saveUserName(name: String) {
        prefs.edit { putString(KEY_USER_NAME, name.trim().take(48)) }
    }

    fun markOnboardingComplete(name: String) {
        prefs.edit {
            putBoolean(KEY_ONBOARDING_TUTORIAL_SEEN, true)
            putString(KEY_USER_NAME, name.trim().take(48))
        }
    }

    fun loadQuestProgress(): QuestProgress {
        return QuestProgress(
            tourDone = prefs.getBoolean(KEY_QUEST_TOUR_DONE, false),
            importDone = prefs.getBoolean(KEY_QUEST_IMPORT_DONE, false),
            speedDone = prefs.getBoolean(KEY_QUEST_SPEED_DONE, false),
            bookmarkDone = prefs.getBoolean(KEY_QUEST_BOOKMARK_DONE, false)
        )
    }

    fun saveQuestProgress(tour: Boolean, import: Boolean, speed: Boolean, bookmark: Boolean) {
        prefs.edit {
            putBoolean(KEY_QUEST_TOUR_DONE, tour)
            putBoolean(KEY_QUEST_IMPORT_DONE, import)
            putBoolean(KEY_QUEST_SPEED_DONE, speed)
            putBoolean(KEY_QUEST_BOOKMARK_DONE, bookmark)
        }
    }

    fun hasImportedOrOpenedDocument(): Boolean {
        return prefs.getBoolean(KEY_HAS_IMPORTED_OR_OPENED_DOCUMENT, loadDocuments().isNotEmpty())
    }

    fun markImportedOrOpenedDocument() {
        prefs.edit { putBoolean(KEY_HAS_IMPORTED_OR_OPENED_DOCUMENT, true) }
    }

    fun recordAppOpen(nowMillis: Long = System.currentTimeMillis()): ReaderTrackerSnapshot {
        val key = trackerDateKey(nowMillis)
        val updated = loadTrackerDays().toMutableMap()
        val current = updated[key] ?: ReaderTrackerDay(dateKey = key)
        updated[key] = current.copy(appOpenCount = maxOf(current.appOpenCount, 1))
        saveTrackerDays(updated.values)
        return loadReaderTrackerSnapshot(nowMillis)
    }

    fun recordUsageDuration(durationMillis: Long, endMillis: Long = System.currentTimeMillis()): ReaderTrackerSnapshot {
        val cleanDuration = durationMillis.coerceIn(0L, MAX_TRACKER_SESSION_MILLIS)
        if (cleanDuration <= 0L) return loadReaderTrackerSnapshot(endMillis)
        val key = trackerDateKey(endMillis)
        val updated = loadTrackerDays().toMutableMap()
        val current = updated[key] ?: ReaderTrackerDay(dateKey = key, appOpenCount = 1)
        updated[key] = current.copy(
            appOpenCount = maxOf(current.appOpenCount, 1),
            usageMillis = current.usageMillis + cleanDuration
        )
        saveTrackerDays(updated.values)
        return loadReaderTrackerSnapshot(endMillis)
    }

    fun recordDocumentRead(documentId: String, title: String, nowMillis: Long = System.currentTimeMillis()): ReaderTrackerSnapshot {
        if (documentId.isBlank()) return loadReaderTrackerSnapshot(nowMillis)
        val key = trackerDateKey(nowMillis)
        val updated = loadTrackerDays().toMutableMap()
        val current = updated[key] ?: ReaderTrackerDay(dateKey = key, appOpenCount = 1)
        updated[key] = current.copy(
            appOpenCount = maxOf(current.appOpenCount, 1),
            readDocumentIds = current.readDocumentIds + documentId
        )
        saveTrackerDays(updated.values)
        return loadReaderTrackerSnapshot(nowMillis)
    }

    fun recordDocumentProgress(document: SavedDocument, nowMillis: Long = System.currentTimeMillis()): ReaderTrackerSnapshot {
        recordDocumentRead(document.id, document.title, nowMillis)
        if (document.chunkCount > 0) {
            val progress = ((document.currentIndex + 1).toFloat() / document.chunkCount.toFloat()).coerceIn(0f, 1f)
            if (progress >= ReaderTrackerMath.COMPLETION_THRESHOLD) {
                recordDocumentCompletion(document.id, document.title, nowMillis)
            }
        }
        return loadReaderTrackerSnapshot(nowMillis)
    }

    fun loadReaderTrackerSnapshot(nowMillis: Long = System.currentTimeMillis()): ReaderTrackerSnapshot {
        val days = loadTrackerDays().values.sortedBy { it.dateKey }
        val todayKey = trackerDateKey(nowMillis)
        val openDateKeys = days.filter { it.appOpenCount > 0 }.map { it.dateKey }.toSet()
        val weekKeys = trackerWeekKeys(nowMillis)
        val (weeklyUsage, weeklyAverage) = ReaderTrackerMath.weeklyUsage(days, weekKeys)
        val daysByKey = days.associateBy { it.dateKey }
        val weeklyUsageByDay = weekKeys.map { key -> daysByKey[key]?.usageMillis ?: 0L }
        // Last 8 weeks of Monday-first daily totals for the swipeable weekly chart,
        // ordered oldest -> newest so the chart defaults to the current (last) week.
        val weeksToShow = 8
        val weeklyHistory = (weeksToShow - 1 downTo 0).map { offset ->
            val weekTime = nowMillis - offset * 7L * 24L * 60L * 60L * 1000L
            val keys = trackerWeekKeys(weekTime)
            val values = keys.map { daysByKey[it]?.usageMillis ?: 0L }
            val label = when (offset) {
                0 -> "This week"
                1 -> "Last week"
                else -> trackerWeekRangeLabel(keys.first(), keys.last())
            }
            WeekBars(label = label, values = values, totalMillis = values.sum(), isCurrentWeek = offset == 0)
        }
        val readThisWeek = weekKeys
            .flatMap { key -> daysByKey[key]?.readDocumentIds.orEmpty() }
            .toSet()
            .size
        val completions = loadTrackerCompletions()
        val completedThisMonth = ReaderTrackerMath.monthCompletionCount(
            completions = completions,
            monthPrefix = trackerDateKey(nowMillis).take(7),
            dateKeyFor = ::trackerDateKey
        )
        return ReaderTrackerSnapshot(
            currentStreak = ReaderTrackerMath.currentStreak(openDateKeys, todayKey),
            longestStreak = ReaderTrackerMath.longestStreak(openDateKeys),
            weeklyUsageMillis = weeklyUsage,
            weeklyAverageMillis = weeklyAverage,
            documentsReadThisWeek = readThisWeek,
            documentsCompletedThisMonth = completedThisMonth,
            weeklyUsageByDay = weeklyUsageByDay,
            weeklyHistory = weeklyHistory,
            recentCompletions = completions.sortedByDescending { it.completedAt }.take(8),
            activeDateKeys = openDateKeys
        )
    }

    private fun recordDocumentCompletion(documentId: String, title: String, nowMillis: Long) {
        if (documentId.isBlank()) return
        val completions = loadTrackerCompletions().toMutableList()
        if (completions.none { it.documentId == documentId }) {
            completions.add(ReaderTrackerCompletion(documentId = documentId, title = title, completedAt = nowMillis))
            saveTrackerCompletions(completions)
        }
    }

    fun loadDocumentOutline(document: SavedDocument, chunks: List<String>): List<VeritasDocumentOutlineEntry> {
        val original = originalFile(document) ?: return emptyList()
        val isPdf = document.originalMimeType.contains("pdf", ignoreCase = true) ||
            document.originalFileName.endsWith(".pdf", ignoreCase = true) ||
            original.extension.equals("pdf", ignoreCase = true)
        if (!isPdf) return emptyList()
        return runCatching {
            PDDocument.load(original).use { pdf ->
                val outline = pdf.documentCatalog.documentOutline ?: return@use emptyList()
                val entries = mutableListOf<VeritasDocumentOutlineEntry>()
                var child = outline.firstChild
                while (child != null) {
                    collectPdfOutlineEntries(pdf, child, chunks, 0, entries)
                    child = child.nextSibling
                }
                entries
            }
        }.getOrElse { error ->
            Log.w(TAG, "Could not read PDF outline for ${document.id}: ${error.message}")
            emptyList()
        }
    }

    private fun collectPdfOutlineEntries(
        pdf: PDDocument,
        item: PDOutlineItem,
        chunks: List<String>,
        level: Int,
        entries: MutableList<VeritasDocumentOutlineEntry>
    ) {
        val title = item.title?.replace(Regex("\\s+"), " ")?.trim().orEmpty()
        val pageIndex = pdfOutlinePageIndex(item)
        if (title.isNotBlank()) {
            entries.add(
                VeritasDocumentOutlineEntry(
                    title = title.take(120),
                    targetIndex = outlineTargetIndex(pageIndex, pdf.numberOfPages, chunks),
                    pageNumber = pageIndex?.plus(1),
                    level = level.coerceIn(0, 6),
                    source = "PDF table of contents"
                )
            )
        }
        var child = item.firstChild
        while (child != null) {
            collectPdfOutlineEntries(pdf, child, chunks, level + 1, entries)
            child = child.nextSibling
        }
    }

    private fun pdfOutlinePageIndex(item: PDOutlineItem): Int? {
        val destination = item.destination ?: (item.action as? PDActionGoTo)?.destination
        return (destination as? PDPageDestination)
            ?.retrievePageNumber()
            ?.takeIf { it >= 0 }
    }

    private fun outlineTargetIndex(pageIndex: Int?, pageCount: Int, chunks: List<String>): Int {
        if (chunks.isEmpty()) return 0
        val page = pageIndex ?: 0
        val denominator = (pageCount - 1).coerceAtLeast(1)
        return ((page.toFloat() / denominator.toFloat()) * chunks.lastIndex.toFloat())
            .roundToInt()
            .coerceIn(0, chunks.lastIndex)
    }

    fun updateProgress(documentId: String, currentIndex: Int, chunkCount: Int): List<SavedDocument> {
        val now = System.currentTimeMillis()
        val updated = loadDocuments().map { doc ->
            if (doc.id == documentId) {
                val safeIndex = if (chunkCount <= 0) 0 else currentIndex.coerceIn(0, chunkCount - 1)
                doc.copy(currentIndex = safeIndex, chunkCount = chunkCount, updatedAt = now)
            } else {
                doc
            }
        }
        saveDocuments(updated)
        return loadDocuments()
    }

    fun clearProgress(documentId: String): List<SavedDocument> {
        val updated = loadDocuments().map { doc ->
            if (doc.id == documentId) doc.copy(currentIndex = 0) else doc
        }
        saveDocuments(updated)
        return loadDocuments()
    }

    fun deleteDocument(documentId: String): List<SavedDocument> {
        ReaderTextModelCache.invalidate(documentId)
        val docs = loadDocuments()
        docs.firstOrNull { it.id == documentId }?.let { doc ->
            runCatching { File(docsDir, doc.fileName).delete() }
            originalFile(doc)?.let { runCatching { it.delete() } }
            CoverExtractor.deleteCover(appContext, documentId)
        }
        val updated = docs.filterNot { it.id == documentId }
        saveDocuments(updated)
        removeFromQueue(documentId)
        // Keep annotations, document notes, and reading history intact so they can still be viewed in Study tab
        saveReadingListCatalog(loadReadingListCatalog().removeDocumentEverywhere(documentId))
        return updated
    }

    fun renameDocument(documentId: String, newTitle: String): List<SavedDocument> {
        val cleanTitle = newTitle.trim().ifBlank { "Untitled reading" }
        val now = System.currentTimeMillis()
        val updated = loadDocuments().map { doc ->
            if (doc.id == documentId) doc.copy(title = cleanTitle, updatedAt = now) else doc
        }
        saveDocuments(updated)
        return loadDocuments()
    }

    fun toggleFavorite(documentId: String): List<SavedDocument> {
        val now = System.currentTimeMillis()
        val updated = loadDocuments().map { doc ->
            if (doc.id == documentId) doc.copy(favorite = !doc.favorite, updatedAt = now) else doc
        }
        saveDocuments(updated)
        return loadDocuments()
    }

    fun setCollection(documentId: String, collectionName: String): List<SavedDocument> {
        val cleanCollection = collectionName.trim()
        val now = System.currentTimeMillis()
        val updated = loadDocuments().map { doc ->
            if (doc.id == documentId) doc.copy(collection = cleanCollection, updatedAt = now) else doc
        }
        saveDocuments(updated)
        return loadDocuments()
    }

    fun loadQueueEntries(): List<QueueEntry> {
        val existingIds = loadDocuments().map { it.id }.toSet()
        val raw = prefs.getString(KEY_QUEUE, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val entries = mutableListOf<QueueEntry>()
        val seen = mutableSetOf<String>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val entry = runCatching { QueueEntry.fromJson(item) }.getOrNull() ?: continue
            if (entry.documentId.isNotBlank() && entry.documentId in existingIds && seen.add(entry.documentId)) {
                entries.add(entry)
            }
        }
        if (entries.size != array.length()) saveQueueEntries(entries)
        PlaybackStateStore.queueCount = entries.size
        return entries
    }

    fun loadQueueDocuments(): List<SavedDocument> {
        val docsById = loadDocuments().associateBy { it.id }
        val queueDocs = loadQueueEntries().mapNotNull { docsById[it.documentId] }
        PlaybackStateStore.queueCount = queueDocs.size
        return queueDocs
    }

    fun loadReadingHistory(): List<ReadingHistoryEntry> {
        val existingDocs = loadDocuments().associateBy { it.id }
        val raw = prefs.getString(KEY_READING_HISTORY, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val entries = mutableListOf<ReadingHistoryEntry>()
        val seen = mutableSetOf<String>()
        for (i in 0 until array.length()) {
            val entry = ReadingHistoryEntry.fromJson(array.optJSONObject(i) ?: continue) ?: continue
            val document = existingDocs[entry.documentId]
            if (seen.add(entry.documentId)) {
                if (document != null) {
                    entries.add(
                        entry.copy(
                            title = document.title,
                            sourceLabel = document.sourceLabel,
                            currentIndex = entry.currentIndex.coerceIn(0, (document.chunkCount - 1).coerceAtLeast(0)),
                            chunkCount = document.chunkCount
                        )
                    )
                } else {
                    entries.add(entry)
                }
            }
        }
        val normalized = entries.sortedByDescending { it.openedAt }.take(MAX_READING_HISTORY)
        if (normalized.size != array.length()) saveReadingHistory(normalized)
        return normalized
    }

    fun addReadingHistory(document: SavedDocument, currentIndex: Int = document.currentIndex): List<ReadingHistoryEntry> {
        val safeIndex = currentIndex.coerceIn(0, (document.chunkCount - 1).coerceAtLeast(0))
        val entry = ReadingHistoryEntry(
            documentId = document.id,
            title = document.title,
            sourceLabel = document.sourceLabel,
            currentIndex = safeIndex,
            chunkCount = document.chunkCount,
            openedAt = System.currentTimeMillis()
        )
        saveReadingHistory(
            (listOf(entry) + loadReadingHistory().filterNot { it.documentId == document.id })
                .take(MAX_READING_HISTORY)
        )
        return loadReadingHistory()
    }

    fun clearReadingHistory(): List<ReadingHistoryEntry> {
        saveReadingHistory(emptyList())
        return emptyList()
    }

    fun removeReadingHistoryEntry(documentId: String): List<ReadingHistoryEntry> {
        val current = loadReadingHistory().filterNot { it.documentId == documentId }
        saveReadingHistory(current)
        return loadReadingHistory()
    }

    fun isQueued(documentId: String): Boolean {
        return loadQueueEntries().any { it.documentId == documentId }
    }

    fun addToQueue(documentId: String): List<SavedDocument> {
        if (findDocument(documentId) == null) return loadQueueDocuments()
        val current = loadQueueEntries()
        if (current.any { it.documentId == documentId }) return loadQueueDocuments()
        saveQueueEntries(current + QueueEntry(documentId, System.currentTimeMillis()))
        return loadQueueDocuments()
    }

    fun removeFromQueue(documentId: String): List<SavedDocument> {
        saveQueueEntries(loadQueueEntries().filterNot { it.documentId == documentId })
        return loadQueueDocuments()
    }

    fun moveQueueItem(documentId: String, offset: Int): List<SavedDocument> {
        val queue = loadQueueEntries().toMutableList()
        val oldIndex = queue.indexOfFirst { it.documentId == documentId }
        if (oldIndex == -1) return loadQueueDocuments()
        val newIndex = (oldIndex + offset).coerceIn(0, queue.lastIndex)
        if (oldIndex == newIndex) return loadQueueDocuments()
        val item = queue.removeAt(oldIndex)
        queue.add(newIndex, item)
        saveQueueEntries(queue)
        return loadQueueDocuments()
    }

    fun clearQueue(): List<SavedDocument> {
        saveQueueEntries(emptyList())
        return emptyList()
    }

    fun completeCurrentAndGetNextQueued(completedDocumentId: String?): SavedDocument? {
        val queue = loadQueueEntries()
        val docsById = loadDocuments().associateBy { it.id }
        completedDocumentId?.let { id ->
            docsById[id]?.let { completed -> recordDocumentCompletion(completed.id, completed.title, System.currentTimeMillis()) }
        }
        val remaining = if (completedDocumentId == null) {
            queue
        } else {
            queue.filterNot { it.documentId == completedDocumentId }
        }
        saveQueueEntries(remaining)
        val next = remaining.firstOrNull()?.let { docsById[it.documentId] }
        PlaybackStateStore.queueCount = remaining.size
        return next
    }

    fun loadReadingListCatalog(): VeritasReadingListCatalog {
        val raw = prefs.getString(KEY_READING_LISTS, "[]") ?: "[]"
        val source = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val catalog = VeritasReadingListCatalog.fromJsonArray(source)
        val normalized = normalizeReadingListCatalog(catalog)
        if (normalized != catalog) saveReadingListCatalog(normalized)
        return normalized
    }

    fun createReadingList(title: String, description: String = ""): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().createList(title = title, description = description))

    fun renameReadingList(listId: String, title: String, description: String? = null): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().renameList(listId, title, description))

    fun archiveReadingList(listId: String): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().archiveList(listId))

    fun deleteReadingList(listId: String): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().deleteList(listId))

    fun addDocumentToReadingList(listId: String, documentId: String): VeritasReadingListCatalog {
        if (findDocument(documentId) == null) return loadReadingListCatalog()
        return saveReadingListCatalog(loadReadingListCatalog().addDocument(listId, documentId))
    }

    fun removeDocumentFromReadingList(listId: String, documentId: String): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().removeDocument(listId, documentId))

    fun moveReadingListDocument(listId: String, documentId: String, offset: Int): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().moveDocument(listId, documentId, offset))

    fun setReadingListSortMode(listId: String, sortMode: VeritasReadingListSortMode): VeritasReadingListCatalog =
        saveReadingListCatalog(loadReadingListCatalog().setSortMode(listId, sortMode))

    fun loadAnnotations(documentId: String): List<ReaderAnnotation> {
        return loadAllAnnotations()
            .filter { it.documentId == documentId }
            .sortedWith(compareBy<ReaderAnnotation> { it.chunkIndex }.thenBy { it.type.name })
    }

    fun loadAnnotationCount(): Int = loadAllAnnotations().size + loadDocumentNotes().size

    fun loadAnnotationsForChunk(documentId: String, chunkIndex: Int): List<ReaderAnnotation> {
        return loadAnnotations(documentId).filter { it.chunkIndex == chunkIndex }
    }

    fun upsertAnnotation(
        documentId: String,
        chunkIndex: Int,
        type: AnnotationType,
        note: String = "",
        highlightColor: String? = null,
        selectionGroupId: String? = null
    ): List<ReaderAnnotation> {
        val now = System.currentTimeMillis()
        val existing = loadAllAnnotations().toMutableList()
        val index = existing.indexOfFirst { it.documentId == documentId && it.chunkIndex == chunkIndex && it.type == type }
        if (index >= 0) {
            val old = existing[index]
            existing[index] = old.copy(
                note = note,
                updatedAt = now,
                highlightColor = highlightColor ?: old.highlightColor,
                selectionGroupId = selectionGroupId ?: old.selectionGroupId
            )
        } else {
            existing.add(
                ReaderAnnotation(
                    documentId = documentId,
                    chunkIndex = chunkIndex,
                    type = type,
                    note = note,
                    createdAt = now,
                    updatedAt = now,
                    highlightColor = highlightColor,
                    selectionGroupId = selectionGroupId
                )
            )
        }
        saveAllAnnotations(existing)
        return loadAnnotations(documentId)
    }

    fun removeAnnotation(documentId: String, chunkIndex: Int, type: AnnotationType): List<ReaderAnnotation> {
        saveAllAnnotations(loadAllAnnotations().filterNot {
            it.documentId == documentId && it.chunkIndex == chunkIndex && it.type == type
        })
        return loadAnnotations(documentId)
    }

    fun removeAnnotations(stableKeys: Set<String>): List<ReaderAnnotation> {
        if (stableKeys.isEmpty()) return loadAllAnnotations()
        saveAllAnnotations(loadAllAnnotations().filterNot { it.stableKey in stableKeys })
        return loadAllAnnotations()
    }

    fun toggleAnnotation(documentId: String, chunkIndex: Int, type: AnnotationType): List<ReaderAnnotation> {
        val exists = loadAllAnnotations().any { it.documentId == documentId && it.chunkIndex == chunkIndex && it.type == type }
        return if (exists) removeAnnotation(documentId, chunkIndex, type) else upsertAnnotation(documentId, chunkIndex, type)
    }

    fun deleteAnnotationsForDocument(documentId: String) {
        saveAllAnnotations(loadAllAnnotations().filterNot { it.documentId == documentId })
    }

    fun loadDocumentNote(documentId: String): String {
        return loadDocumentNotes()[documentId].orEmpty()
    }

    fun loadAllDocumentNotes(): Map<String, String> = loadDocumentNotes()

    fun saveDocumentNote(documentId: String, note: String): String {
        if (findDocument(documentId) == null) return ""
        val updated = loadDocumentNotes().toMutableMap()
        val cleanNote = note.trim()
        if (cleanNote.isBlank()) {
            updated.remove(documentId)
        } else {
            updated[documentId] = cleanNote
        }
        saveDocumentNotes(updated)
        return loadDocumentNote(documentId)
    }

    fun deleteDocumentNote(documentId: String) {
        saveDocumentNotes(loadDocumentNotes().filterKeys { it != documentId })
    }

    fun deleteDocumentNotes(documentIds: Set<String>): Map<String, String> {
        if (documentIds.isEmpty()) return loadDocumentNotes()
        saveDocumentNotes(loadDocumentNotes().filterKeys { it !in documentIds })
        return loadDocumentNotes()
    }


    fun loadPronunciationRules(): List<PronunciationRule> {
        val raw = prefs.getString(KEY_PRONUNCIATION_RULES, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val rules = mutableListOf<PronunciationRule>()
        val seen = mutableSetOf<String>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val rule = PronunciationRule.fromJson(obj) ?: continue
            if (rule.id.isNotBlank() && seen.add(rule.id)) rules.add(rule)
        }
        if (rules.size != array.length()) savePronunciationRules(rules)
        return rules.sortedByDescending { it.createdAt }
    }

    fun addPronunciationRule(find: String, replaceWith: String): List<PronunciationRule> {
        val cleanFind = find.trim()
        if (cleanFind.isBlank()) return loadPronunciationRules()
        val cleanReplace = replaceWith.trim()
        val rule = PronunciationRule(
            id = UUID.randomUUID().toString(),
            find = cleanFind,
            replaceWith = cleanReplace,
            enabled = true,
            createdAt = System.currentTimeMillis()
        )
        savePronunciationRules(listOf(rule) + loadPronunciationRules())
        return loadPronunciationRules()
    }

    fun removePronunciationRule(ruleId: String): List<PronunciationRule> {
        savePronunciationRules(loadPronunciationRules().filterNot { it.id == ruleId })
        return loadPronunciationRules()
    }

    fun togglePronunciationRule(ruleId: String): List<PronunciationRule> {
        savePronunciationRules(loadPronunciationRules().map { rule ->
            if (rule.id == ruleId) rule.copy(enabled = !rule.enabled) else rule
        })
        return loadPronunciationRules()
    }

    fun applyPronunciationRules(text: String): String {
        var output = text
        loadPronunciationRules()
            .filter { it.enabled && it.find.isNotBlank() }
            .forEach { rule ->
                val escapedFind = Regex.escape(rule.find)
                val isWord = rule.find.all { it.isLetterOrDigit() || it == '_' }
                val pattern = if (isWord) "\\b$escapedFind\\b" else escapedFind
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                output = regex.replace(output) { matchResult ->
                    val matchedText = matchResult.value
                    val replacement = rule.replaceWith
                    when {
                        matchedText.all { it.isUpperCase() } -> replacement.uppercase()
                        matchedText.firstOrNull()?.isUpperCase() == true -> {
                            replacement.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                        else -> replacement
                    }
                }
            }
        return output
    }

    fun loadReaderSettings(): ReaderSettings {
        val raw = prefs.getString(KEY_READER_SETTINGS, null) ?: return ReaderSettings()
        return runCatching { ReaderSettings.fromJson(JSONObject(raw)) }.getOrDefault(ReaderSettings())
    }

    fun saveReaderSettings(settings: ReaderSettings): ReaderSettings {
        val normalized = settings.copy(
            fontSizeSp = settings.fontSizeSp.coerceIn(14, 28),
            sectionSpacingDp = settings.sectionSpacingDp.coerceIn(6, 24),
            themeId = VeritasThemeCatalog.normalizeThemeId(settings.themeId),
            themePackId = VeritasThemePackCatalog.normalizePackId(settings.themePackId)
        )
        prefs.edit { putString(KEY_READER_SETTINGS, normalized.toJson().toString()) }
        PlaybackStateStore.autoPlayQueue = normalized.autoPlayQueue
        return normalized
    }

    fun loadVoiceSettings(): VoiceSettings {
        val raw = prefs.getString(KEY_VOICE_SETTINGS, null) ?: return VoiceSettings()
        return runCatching { VoiceSettings.fromJson(JSONObject(raw)) }.getOrDefault(VoiceSettings())
    }

    fun saveVoiceSettings(settings: VoiceSettings): VoiceSettings {
        val normalized = settings.copy(
            engineLabel = settings.engineLabel.ifBlank { "System default" },
            voiceLabel = settings.voiceLabel.ifBlank { "System default voice" },
            profileName = settings.profileName.ifBlank { "Balanced" },
            preferredRate = settings.preferredRate.coerceIn(0.5f, 2.0f),
            preferredPitch = settings.preferredPitch.coerceIn(0.7f, 1.4f)
        )
        prefs.edit { putString(KEY_VOICE_SETTINGS, normalized.toJson().toString()) }
        return normalized
    }

    fun loadNarrationSettings(): NarrationSettings {
        val raw = prefs.getString(KEY_NARRATION_SETTINGS, null) ?: return NarrationSettings()
        return runCatching { NarrationSettings.fromJson(JSONObject(raw)) }.getOrDefault(NarrationSettings())
    }

    fun saveNarrationSettings(settings: NarrationSettings): NarrationSettings {
        val normalized = settings.copy(
            narratorRateMultiplier = settings.narratorRateMultiplier.coerceIn(0.75f, 1.25f),
            narratorPitchMultiplier = settings.narratorPitchMultiplier.coerceIn(0.80f, 1.25f),
            dialogueRateMultiplier = settings.dialogueRateMultiplier.coerceIn(0.75f, 1.25f),
            dialoguePitchMultiplier = settings.dialoguePitchMultiplier.coerceIn(0.80f, 1.25f)
        )
        prefs.edit { putString(KEY_NARRATION_SETTINGS, normalized.toJson().toString()) }
        return normalized
    }

    fun loadAskAiSettings(): AskAiSettings {
        val raw = prefs.getString(KEY_ASK_AI_SETTINGS, null) ?: return AskAiSettings()
        return runCatching { AskAiSettings.fromJson(JSONObject(raw)) }.getOrDefault(AskAiSettings())
    }

    fun saveAskAiSettings(settings: AskAiSettings): AskAiSettings {
        val normalized = settings.copy(
            assistantId = settings.assistantId.ifBlank { "chooser" },
            assistantLabel = settings.assistantLabel.ifBlank { "Choose each time" },
            promptTemplate = settings.promptTemplate.ifBlank { "Answer clearly using this selected Veritas text:\n\n{selection}" }
        )
        prefs.edit { putString(KEY_ASK_AI_SETTINGS, normalized.toJson().toString()) }
        return normalized
    }

    private fun savePronunciationRules(rules: List<PronunciationRule>) {
        val array = JSONArray()
        rules.forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_PRONUNCIATION_RULES, array.toString()) }
    }

    fun loadGeneralNotes(): List<GeneralNote> {
        val array = readResilientJsonArray("general_notes")
        val notes = mutableListOf<GeneralNote>()
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val note = runCatching { GeneralNote.fromJson(item) }.getOrNull() ?: continue
            notes.add(note)
        }
        return notes.sortedByDescending { it.updatedAt }
    }

    fun saveGeneralNotes(notes: List<GeneralNote>) {
        val array = JSONArray()
        notes.forEach { array.put(it.toJson()) }
        commitResilientJson("general_notes", array.toString())
    }

    fun loadAllAnnotations(): List<ReaderAnnotation> {
        val existingIds = loadDocuments().map { it.id }.toSet()
        val raw = prefs.getString(KEY_ANNOTATIONS, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val annotations = mutableListOf<ReaderAnnotation>()
        val seen = mutableSetOf<String>()
        var changed = false
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val source = ReaderAnnotation.fromJson(obj) ?: continue
            val annotation = if (source.type == AnnotationType.HIGHLIGHT) {
                changed = true
                source.copy(type = AnnotationType.BOOKMARK)
            } else {
                source
            }
            if (annotation.documentId in existingIds && annotation.chunkIndex >= 0 && seen.add(annotation.stableKey)) {
                annotations.add(annotation)
            }
        }
        if (changed || annotations.size != array.length()) saveAllAnnotations(annotations)
        return annotations
    }

    fun saveAllAnnotations(annotations: List<ReaderAnnotation>) {
        val array = JSONArray()
        annotations.sortedWith(compareBy<ReaderAnnotation> { it.documentId }.thenBy { it.chunkIndex }.thenBy { it.type.name })
            .forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_ANNOTATIONS, array.toString()) }
    }

    fun loadAllFlashcards(): List<FlashcardProgress> {
        val raw = prefs.getString("study_flashcards", "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val list = mutableListOf<FlashcardProgress>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            list.add(FlashcardProgress.fromJson(obj))
        }
        return list
    }

    fun saveAllFlashcards(list: List<FlashcardProgress>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        prefs.edit { putString("study_flashcards", array.toString()) }
    }

    private fun loadDocumentNotes(): Map<String, String> {
        val existingIds = loadDocuments().map { it.id }.toSet()
        val raw = prefs.getString(KEY_DOCUMENT_NOTES, "{}") ?: "{}"
        val obj = runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
        val notes = linkedMapOf<String, String>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val documentId = keys.next()
            val note = obj.optString(documentId).trim()
            if (documentId in existingIds && note.isNotBlank()) {
                notes[documentId] = note
            }
        }
        if (notes.size != obj.length()) saveDocumentNotes(notes)
        return notes
    }

    private fun saveDocumentNotes(notes: Map<String, String>) {
        val existingIds = loadDocuments().map { it.id }.toSet()
        val obj = JSONObject()
        notes.toSortedMap().forEach { (documentId, note) ->
            val cleanNote = note.trim()
            if (documentId in existingIds && cleanNote.isNotBlank()) {
                obj.put(documentId, cleanNote)
            }
        }
        prefs.edit { putString(KEY_DOCUMENT_NOTES, obj.toString()) }
    }

    fun loadTrackerDays(): Map<String, ReaderTrackerDay> {
        val raw = prefs.getString(KEY_TRACKER_DAYS, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val days = linkedMapOf<String, ReaderTrackerDay>()
        for (i in 0 until array.length()) {
            val day = array.optJSONObject(i)?.let(ReaderTrackerDay::fromJson) ?: continue
            if (day.dateKey.isNotBlank()) days[day.dateKey] = day
        }
        return days
    }

    private fun saveTrackerDays(days: Collection<ReaderTrackerDay>) {
        val array = JSONArray()
        days.sortedByDescending { it.dateKey }
            .take(MAX_TRACKER_DAYS)
            .sortedBy { it.dateKey }
            .forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_TRACKER_DAYS, array.toString()) }
    }

    private fun loadTrackerCompletions(): List<ReaderTrackerCompletion> {
        val raw = prefs.getString(KEY_TRACKER_COMPLETIONS, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val completions = mutableListOf<ReaderTrackerCompletion>()
        val seen = mutableSetOf<String>()
        for (i in 0 until array.length()) {
            val completion = array.optJSONObject(i)?.let(ReaderTrackerCompletion::fromJson) ?: continue
            if (completion.documentId.isNotBlank() && seen.add(completion.documentId)) completions.add(completion)
        }
        return completions
    }

    private fun saveTrackerCompletions(completions: List<ReaderTrackerCompletion>) {
        val array = JSONArray()
        completions.sortedByDescending { it.completedAt }
            .distinctBy { it.documentId }
            .take(MAX_TRACKER_COMPLETIONS)
            .forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_TRACKER_COMPLETIONS, array.toString()) }
    }

    private fun trackerDateKey(timestamp: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timestamp))

    private fun trackerWeekRangeLabel(startKey: String, endKey: String): String {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val out = SimpleDateFormat("MMM d", Locale.getDefault())
        return runCatching {
            val start = parser.parse(startKey)
            val end = parser.parse(endKey)
            if (start != null && end != null) "${out.format(start)} – ${out.format(end)}" else startKey
        }.getOrDefault(startKey)
    }

    private fun trackerWeekKeys(timestamp: Long): List<String> {
        val cursor = Calendar.getInstance().apply {
            timeInMillis = timestamp
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
        }
        return List(7) {
            val key = trackerDateKey(cursor.timeInMillis)
            cursor.add(Calendar.DAY_OF_YEAR, 1)
            key
        }
    }

    /**
     * Writes a JSON payload to [key] while preserving the previous value as a "last known
     * good" backup. If a later write is ever corrupt/partial, [readResilientJsonArray] can
     * recover from the backup instead of the data silently vanishing.
     */
    private fun commitResilientJson(key: String, value: String) {
        val previous = prefs.getString(key, null)
        prefs.edit {
            if (!previous.isNullOrEmpty()) putString("${key}__bak", previous)
            putString(key, value)
        }
    }

    /**
     * Reads a JSON array from [key], verifying it parses. If the primary value is corrupt,
     * falls back to the last-known-good backup rather than returning empty — which would look
     * to the user like their library/notes had been wiped.
     */
    private fun readResilientJsonArray(key: String): JSONArray {
        prefs.getString(key, null)?.let { raw ->
            runCatching { JSONArray(raw) }.getOrNull()?.let { return it }
        }
        val backup = prefs.getString("${key}__bak", null) ?: return JSONArray()
        return runCatching { JSONArray(backup) }.getOrDefault(JSONArray())
    }

    private fun saveDocuments(documents: List<SavedDocument>) {
        val array = JSONArray()
        documents.forEach { array.put(it.toJson()) }
        commitResilientJson(KEY_DOCUMENTS, array.toString())
    }

    private fun saveQueueEntries(entries: List<QueueEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_QUEUE, array.toString()) }
        PlaybackStateStore.queueCount = entries.size
    }

    private fun saveReadingHistory(history: List<ReadingHistoryEntry>) {
        val array = JSONArray()
        history.take(MAX_READING_HISTORY).forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_READING_HISTORY, array.toString()) }
    }

    private fun saveReadingListCatalog(catalog: VeritasReadingListCatalog): VeritasReadingListCatalog {
        val normalized = normalizeReadingListCatalog(catalog)
        prefs.edit { putString(KEY_READING_LISTS, normalized.toJsonArray().toString()) }
        return normalized
    }

    private fun normalizeReadingListCatalog(catalog: VeritasReadingListCatalog): VeritasReadingListCatalog {
        val existingIds = loadDocuments().map { it.id }.toSet()
        val now = System.currentTimeMillis()
        return VeritasReadingListCatalog(
            lists = catalog.lists.map { list ->
                val filteredItems = VeritasReadingList.normalizeItems(
                    list.items.filter { item -> item.documentId in existingIds }
                )
                if (filteredItems == list.items) list else list.copy(items = filteredItems, updatedAt = now)
            }
        )
    }

    private fun importReadingLists(
        array: JSONArray?,
        documentIdMap: Map<String, String>,
        finalDocuments: List<SavedDocument>,
        replaceExisting: Boolean
    ): Int {
        if (array == null) {
            if (replaceExisting) saveReadingListCatalog(VeritasReadingListCatalog())
            return 0
        }
        val finalDocumentIds = finalDocuments.map { it.id }.toSet()
        val existingCatalog = if (replaceExisting) VeritasReadingListCatalog() else loadReadingListCatalog()
        val usedListIds = existingCatalog.lists.map { it.id }.toMutableSet()
        val importedLists = mutableListOf<VeritasReadingList>()
        val sourceCatalog = VeritasReadingListCatalog.fromJsonArray(array)
        sourceCatalog.lists.forEach { list ->
            val targetId = uniqueReadingListId(list.id, usedListIds)
            usedListIds.add(targetId)
            val mappedItems = list.items.mapNotNull { item ->
                val mappedDocumentId = documentIdMap[item.documentId] ?: return@mapNotNull null
                if (mappedDocumentId !in finalDocumentIds) return@mapNotNull null
                item.copy(documentId = mappedDocumentId)
            }
            importedLists.add(
                list.copy(
                    id = targetId,
                    items = VeritasReadingList.normalizeItems(mappedItems),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        saveReadingListCatalog(VeritasReadingListCatalog(existingCatalog.lists + importedLists))
        return importedLists.size
    }

    private fun uniqueReadingListId(preferredId: String, usedIds: Set<String>): String {
        var candidate = preferredId.trim().ifBlank { UUID.randomUUID().toString() }
        while (candidate in usedIds) candidate = UUID.randomUUID().toString()
        return candidate
    }

    companion object {
        private const val TAG = "DocumentRepository"
        private const val MAX_READING_HISTORY = 40
        // Process-wide lock for read-modify-write of shared prefs counters that are touched
        // from multiple components/threads (UI + PlaybackService).
        private val LIBRARY_WRITE_LOCK = Any()
        private const val KEY_DOCUMENTS = "documents"
        private const val KEY_QUEUE = "reading_queue"
        private const val KEY_READING_LISTS = "reading_lists"
        private const val KEY_READING_HISTORY = "reading_history"
        private const val KEY_ANNOTATIONS = "reader_annotations"
        private const val KEY_DOCUMENT_NOTES = "document_notes"
        private const val KEY_PRONUNCIATION_RULES = "pronunciation_rules"
        private const val KEY_READER_SETTINGS = "reader_settings"
        private const val KEY_VOICE_SETTINGS = "voice_settings"
        private const val KEY_NARRATION_SETTINGS = "narration_settings"
        private const val KEY_ASK_AI_SETTINGS = "ask_ai_settings"
        private const val KEY_AI_TEMPLATES = "ai_prompt_templates"
        private const val KEY_AI_HISTORY = "ai_prompt_history"
        private const val KEY_ONBOARDING_TUTORIAL_SEEN = "onboarding_tutorial_seen"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_HAS_IMPORTED_OR_OPENED_DOCUMENT = "has_imported_or_opened_document"
        private const val KEY_QUEST_TOUR_DONE = "quest_tour_done"
        private const val KEY_QUEST_IMPORT_DONE = "quest_import_done"
        private const val KEY_QUEST_SPEED_DONE = "quest_speed_done"
        private const val KEY_QUEST_BOOKMARK_DONE = "quest_bookmark_done"
        private const val KEY_TRACKER_DAYS = "reader_tracker_days"
        private const val KEY_TRACKER_COMPLETIONS = "reader_tracker_completions"
        private const val MAX_TRACKER_DAYS = 370
        private const val MAX_TRACKER_COMPLETIONS = 500
        private const val MAX_TRACKER_SESSION_MILLIS = 12L * 60L * 60L * 1000L
    }
}

object ReaderTextModelCache {
    private const val MAX_ENTRIES = 8
    private val cache = object : LinkedHashMap<String, ReaderTextModel>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ReaderTextModel>?): Boolean {
            return size > MAX_ENTRIES
        }
    }

    fun get(documentId: String?, rawText: String, pageCount: Int): ReaderTextModel {
        val key = buildKey(documentId, rawText, pageCount)
        synchronized(cache) {
            cache[key]?.let { return it }
        }
        val model = ReaderTextIndex.build(rawText, pageCount)
        synchronized(cache) {
            cache[key] = model
        }
        return model
    }

    fun invalidate(documentId: String) {
        synchronized(cache) {
            val prefix = "${documentId.ifBlank { "anonymous" }}:"
            cache.keys.filter { it.startsWith(prefix) }.forEach { cache.remove(it) }
        }
    }

    private fun buildKey(documentId: String?, rawText: String, pageCount: Int): String {
        val id = documentId?.ifBlank { null } ?: "anonymous"
        return "$id:${pageCount.coerceAtLeast(0)}:${rawText.length}:${rawText.hashCode()}"
    }
}

fun buildReaderDocument(metadata: SavedDocument, rawText: String): ReaderDocument {
    val model = ReaderTextModelCache.get(metadata.id, rawText, metadata.pageCount)
    return ReaderDocument(
        id = metadata.id,
        title = metadata.title.ifBlank { "Untitled reading" },
        sourceLabel = metadata.sourceLabel,
        rawText = rawText,
        sentences = model.sentences.map { it.text },
        pageCount = model.pageCount
    )
}

object TextChunker {
    fun chunk(text: String): List<String> {
        return ReaderTextIndex.sentences(text)
    }
}

fun formatUpdated(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
