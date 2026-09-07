package com.veritas.reader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class AiProvider(
    val id: String,
    val label: String,
    val defaultModel: String,
    val defaultEndpoint: String,
    val recommendedModels: List<String> = emptyList()
) {
    GEMINI(
        "gemini",
        "Google Gemini",
        "gemini-2.5-flash",
        "https://generativelanguage.googleapis.com/v1beta/models",
        listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash", "gemini-1.5-pro")
    ),
    OPENAI(
        "openai",
        "OpenAI",
        "gpt-4o-mini",
        "https://api.openai.com/v1/chat/completions",
        listOf("gpt-4o-mini", "gpt-4o", "gpt-4.5-preview", "o3-mini", "o1")
    ),
    ANTHROPIC(
        "anthropic",
        "Anthropic Claude",
        "claude-3-7-sonnet-latest",
        "https://api.anthropic.com/v1/messages",
        listOf("claude-3-7-sonnet-latest", "claude-3-5-haiku-latest", "claude-3-5-sonnet-latest", "claude-fable-preview")
    ),
    GROQ(
        "groq",
        "Groq (Fast Inference)",
        "llama-3.3-70b-versatile",
        "https://api.groq.com/openai/v1/chat/completions",
        listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768")
    ),
    OPENROUTER(
        "openrouter",
        "OpenRouter",
        "google/gemini-2.5-flash",
        "https://openrouter.ai/api/v1/chat/completions",
        listOf("google/gemini-2.5-flash", "anthropic/claude-3.7-sonnet", "openai/gpt-4o", "meta-llama/llama-3.3-70b-instruct")
    ),
    CUSTOM(
        "custom",
        "Custom OpenAI-Compatible",
        "",
        "",
        emptyList()
    )
}

object GeminiStudyService {

    private const val PREFS_KEY = "gemini_api_key"
    private const val PREFS_UNIVERSAL_KEY = "ai_api_key"
    private const val PREFS_PROVIDER = "ai_provider"
    private const val PREFS_MODEL = "ai_model"
    private const val PREFS_CUSTOM_ENDPOINT = "ai_custom_endpoint"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    @Volatile
    var activeProvider: AiProvider = AiProvider.GEMINI
    @Volatile
    var activeCustomModel: String = ""
    @Volatile
    var activeCustomEndpoint: String = ""

    fun init(context: Context) {
        activeProvider = getProvider(context)
        activeCustomModel = getModel(context)
        activeCustomEndpoint = getCustomEndpoint(context)
    }

    fun getProvider(context: Context): AiProvider {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        val id = prefs.getString(PREFS_PROVIDER, AiProvider.GEMINI.id) ?: AiProvider.GEMINI.id
        return AiProvider.entries.firstOrNull { it.id == id } ?: AiProvider.GEMINI
    }

    fun saveProvider(context: Context, provider: AiProvider) {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        prefs.edit().putString(PREFS_PROVIDER, provider.id).apply()
        activeProvider = provider
    }

    fun getModel(context: Context): String {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        return prefs.getString(PREFS_MODEL, "").orEmpty()
    }

    fun saveModel(context: Context, model: String) {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        prefs.edit().putString(PREFS_MODEL, model.trim()).apply()
        activeCustomModel = model.trim()
    }

    fun getCustomEndpoint(context: Context): String {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        return prefs.getString(PREFS_CUSTOM_ENDPOINT, "").orEmpty()
    }

    fun saveCustomEndpoint(context: Context, endpoint: String) {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        prefs.edit().putString(PREFS_CUSTOM_ENDPOINT, endpoint.trim()).apply()
        activeCustomEndpoint = endpoint.trim()
    }

    fun getApiKey(context: Context): String {
        init(context)
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        val universal = prefs.getString(PREFS_UNIVERSAL_KEY, "").orEmpty().trim()
        if (universal.isNotBlank()) return universal
        return prefs.getString(PREFS_KEY, "").orEmpty().trim()
    }

    fun saveApiKey(context: Context, apiKey: String) {
        val prefs = context.getSharedPreferences("veritas_reader_library", Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREFS_UNIVERSAL_KEY, apiKey.trim())
            .putString(PREFS_KEY, apiKey.trim())
            .apply()
    }

    fun hasApiKey(context: Context): Boolean = getApiKey(context).isNotBlank()

    suspend fun generateFlashcards(
        apiKey: String,
        documentTitle: String,
        textContext: String,
        cardCount: Int = 10
    ): Result<List<Flashcard>> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = """
                You are an expert study assistant for Veritas Reader.
                Analyze the following document content from '$documentTitle' and create exactly $cardCount high-yield, active recall flashcards.
                Focus on fundamental concepts, key definitions, cause-and-effect relationships, and important facts.
                
                Content:
                ${textContext.take(25000)}
            """.trimIndent()

            val responseJson = callUniversalAiApi(
                apiKey = apiKey,
                prompt = prompt,
                responseSchema = JSONObject()
                    .put("type", "ARRAY")
                    .put(
                        "items",
                        JSONObject()
                            .put("type", "OBJECT")
                            .put("properties", JSONObject()
                                .put("front", JSONObject().put("type", "STRING").put("description", "Prompt, question, or term"))
                                .put("back", JSONObject().put("type", "STRING").put("description", "Clear, concise answer or definition"))
                            )
                            .put("required", JSONArray().put("front").put("back"))
                    )
            )

            val parsedCards = AiResultParser.parseFlashcards(responseJson)
            if (parsedCards.isEmpty()) {
                throw IllegalStateException("No flashcards could be generated from this text.")
            }
            parsedCards
        }
    }

    suspend fun generateQuiz(
        apiKey: String,
        documentTitle: String,
        textContext: String,
        questionCount: Int = 10
    ): Result<List<QuizQuestion>> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = """
                You are an expert academic examiner for Veritas Reader creating an exam-style multiple-choice quiz of $questionCount questions based on '$documentTitle'.
                
                CRITICAL ANTI-AI GIVEAWAY RULES:
                1. UNIFORM OPTION LENGTH & DETAIL: All 4 multiple-choice options (A, B, C, D) MUST be approximately equal in length, tone, syntax, and level of detail. NEVER make the correct option noticeably longer, more nuanced, or more specific than the distractors.
                2. NO BRACKETS OR PARENTHETICAL HINTS: Do NOT include parenthetical explanations, brackets, or qualifiers inside options that act as giveaway cues.
                3. PLAUSIBLE, CONTEXT-GROUNDED DISTRACTORS: Distractors must be realistic and relevant to the text content so answers cannot be guessed by simple elimination.
                4. EXACT FORMAT: Each question must have exactly 4 options, exactly one unambiguous correct answer, and a short 1-sentence explanation of why the answer is correct.
                5. Evenly distribute correct answers across all option positions (A, B, C, D).
                
                Content:
                ${textContext.take(25000)}
            """.trimIndent()

            val responseJson = callUniversalAiApi(
                apiKey = apiKey,
                prompt = prompt,
                responseSchema = JSONObject()
                    .put("type", "ARRAY")
                    .put(
                        "items",
                        JSONObject()
                            .put("type", "OBJECT")
                            .put("properties", JSONObject()
                                .put("question", JSONObject().put("type", "STRING"))
                                .put("options", JSONObject().put("type", "ARRAY").put("items", JSONObject().put("type", "STRING")))
                                .put("answer", JSONObject().put("type", "STRING").put("description", "The exact text of the correct option"))
                                .put("explanation", JSONObject().put("type", "STRING"))
                            )
                            .put("required", JSONArray().put("question").put("options").put("answer").put("explanation"))
                    )
            )

            val parsedQuiz = AiResultParser.parseQuiz(responseJson)
            if (parsedQuiz.isEmpty()) {
                throw IllegalStateException("No quiz questions could be generated from this text.")
            }
            parsedQuiz
        }
    }

    suspend fun generateStudySummary(
        apiKey: String,
        documentTitle: String,
        textContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = """
                You are a master study coach. Summarize the key concepts of '$documentTitle' for maximum retention.
                Structure your response with:
                # 📌 Executive Overview
                # 💡 Key Concepts & Takeaways
                # ⚠️ Common Pitfalls & Traps
                # 🎯 Quick Self-Check Checklist
                
                Content:
                ${textContext.take(30000)}
            """.trimIndent()

            callUniversalAiApi(apiKey = apiKey, prompt = prompt, responseSchema = null)
        }
    }

    suspend fun generateExplanation(
        apiKey: String,
        documentTitle: String,
        textContext: String,
        targetPassage: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = """
                You are an expert tutor using the Feynman technique.
                Explain the following passage or ideas from '$documentTitle' so clearly that anyone can immediately grasp it.
                ${if (targetPassage.isNotBlank()) "Focus especially on: \"$targetPassage\"" else ""}
                
                Structure your response with:
                # 💡 The Core Idea (In plain English)
                # 🔍 Real-World Analogy
                # 📖 Breakdown of Difficult Terms & Jargon
                # 🔑 Why It Matters
                
                Context:
                ${textContext.take(25000)}
            """.trimIndent()

            callUniversalAiApi(apiKey = apiKey, prompt = prompt, responseSchema = null)
        }
    }

    suspend fun generateStudyGuide(
        apiKey: String,
        documentTitle: String,
        textContext: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = """
                You are an elite academic coach. Create an organized, high-yield study guide from this excerpt of '$documentTitle'.
                Structure your response with:
                # 📚 Study Cheatsheet & Summary
                # 🗝️ Core Terminology & Definitions
                # ⚡ Key Principles, Takeaways & Cause-and-Effect
                # 📝 Quick Self-Review Questions
                
                Content:
                ${textContext.take(30000)}
            """.trimIndent()

            callUniversalAiApi(apiKey = apiKey, prompt = prompt, responseSchema = null)
        }
    }

    private fun callUniversalAiApi(
        apiKey: String,
        prompt: String,
        responseSchema: JSONObject? = null
    ): String {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("AI API key is missing. Please configure your key in Study Hub Settings.")
        }

        return when (activeProvider) {
            AiProvider.GEMINI -> callGeminiApi(apiKey, prompt, responseSchema, activeCustomModel)
            AiProvider.ANTHROPIC -> callAnthropicApi(apiKey, prompt, responseSchema, activeCustomModel)
            else -> callOpenAiCompatibleApi(apiKey, prompt, responseSchema, activeProvider, activeCustomModel, activeCustomEndpoint)
        }
    }

    private fun callGeminiApi(
        apiKey: String,
        prompt: String,
        responseSchema: JSONObject? = null,
        customModel: String = ""
    ): String {
        val model = customModel.ifBlank { AiProvider.GEMINI.defaultModel }
        val endpointUrl = "$BASE_URL/$model:generateContent?key=$apiKey"
        val url = URL(endpointUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connectTimeout = 20000
            readTimeout = 30000
            doOutput = true
        }

        val requestBody = JSONObject()
        val contentsArray = JSONArray()
        val partsArray = JSONArray().put(JSONObject().put("text", prompt))
        contentsArray.put(JSONObject().put("parts", partsArray))
        requestBody.put("contents", contentsArray)

        if (responseSchema != null) {
            val genConfig = JSONObject()
                .put("response_mime_type", "application/json")
                .put("response_schema", responseSchema)
            requestBody.put("generationConfig", genConfig)
        }

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val statusCode = conn.responseCode
        if (statusCode !in 200..299) {
            val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val errorMessage = runCatching {
                JSONObject(errorBody).optJSONObject("error")?.optString("message", "")
            }.getOrNull()?.ifBlank { null } ?: "Gemini API Error ($statusCode): $errorBody"
            throw IllegalStateException(errorMessage)
        }

        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseText)
        val candidates = root.optJSONArray("candidates")
        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val textResult = parts?.optJSONObject(0)?.optString("text", "") ?: ""

        if (textResult.isBlank()) {
            throw IllegalStateException("Empty response returned from Gemini API.")
        }

        return textResult
    }

    private fun callOpenAiCompatibleApi(
        apiKey: String,
        prompt: String,
        responseSchema: JSONObject?,
        provider: AiProvider,
        customModel: String,
        customEndpoint: String
    ): String {
        val endpoint = if (provider == AiProvider.CUSTOM && customEndpoint.isNotBlank()) {
            customEndpoint
        } else {
            provider.defaultEndpoint
        }
        val model = customModel.ifBlank { provider.defaultModel }

        val systemInstruction = if (responseSchema != null) {
            "\nYou must respond ONLY with a raw JSON array matching the requested structure. Do not include markdown fences (no ```json) or explanation outside the JSON."
        } else ""

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Authorization", "Bearer $apiKey")
            connectTimeout = 25000
            readTimeout = 35000
            doOutput = true
        }

        val messages = JSONArray()
        messages.put(JSONObject().put("role", "user").put("content", prompt + systemInstruction))

        val requestBody = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.3)

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val statusCode = conn.responseCode
        if (statusCode !in 200..299) {
            val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val errorMessage = runCatching {
                JSONObject(errorBody).optJSONObject("error")?.optString("message", "")
            }.getOrNull()?.ifBlank { null } ?: "${provider.label} Error ($statusCode): $errorBody"
            throw IllegalStateException(errorMessage)
        }

        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseText)
        val choices = root.optJSONArray("choices")
        val message = choices?.optJSONObject(0)?.optJSONObject("message")
        val content = message?.optString("content", "") ?: ""

        if (content.isBlank()) {
            throw IllegalStateException("Empty response returned from ${provider.label}.")
        }

        return content
    }

    private fun callAnthropicApi(
        apiKey: String,
        prompt: String,
        responseSchema: JSONObject?,
        customModel: String
    ): String {
        val model = customModel.ifBlank { AiProvider.ANTHROPIC.defaultModel }
        val endpoint = AiProvider.ANTHROPIC.defaultEndpoint

        val systemInstruction = if (responseSchema != null) {
            "\nYou must respond ONLY with a raw JSON array matching the requested schema. Do not include markdown fences or any explanation outside the JSON."
        } else ""

        val url = URL(endpoint)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", "2023-06-01")
            connectTimeout = 25000
            readTimeout = 35000
            doOutput = true
        }

        val messages = JSONArray()
        messages.put(JSONObject().put("role", "user").put("content", prompt + systemInstruction))

        val requestBody = JSONObject()
            .put("model", model)
            .put("max_tokens", 4096)
            .put("messages", messages)

        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        val statusCode = conn.responseCode
        if (statusCode !in 200..299) {
            val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val errorMessage = runCatching {
                JSONObject(errorBody).optJSONObject("error")?.optString("message", "")
            }.getOrNull()?.ifBlank { null } ?: "Anthropic Error ($statusCode): $errorBody"
            throw IllegalStateException(errorMessage)
        }

        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(responseText)
        val contentArray = root.optJSONArray("content")
        val textResult = contentArray?.optJSONObject(0)?.optString("text", "") ?: ""

        if (textResult.isBlank()) {
            throw IllegalStateException("Empty response returned from Anthropic Claude.")
        }

        return textResult
    }

    suspend fun defineInContext(
        apiKey: String,
        word: String,
        contextSentence: String,
        bookTitle: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val titlePart = if (bookTitle.isNotBlank()) " from '$bookTitle'" else ""
            val prompt = """
                You are a literary reading and vocabulary assistant in Veritas Reader.
                Define the word or phrase '$word' specifically as it is used in this sentence$titlePart:
                "$contextSentence"

                Requirements:
                - Give a concise, precise, context-based definition (1-2 sentences).
                - Explain what the author means by '$word' in this specific usage and context.
                - State the part of speech in brackets at the beginning if helpful, e.g. "(adj.) ...".
                - Do NOT include conversational filler, preamble, or repetition like "In this context, the word means". Provide only the direct, rich definition.
            """.trimIndent()

            callUniversalAiApi(
                apiKey = apiKey,
                prompt = prompt
            ).trim()
        }
    }
}
