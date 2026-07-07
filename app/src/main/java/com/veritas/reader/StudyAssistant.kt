package com.veritas.reader

data class Flashcard(
    val front: String,
    val back: String
)

data class QuizQuestion(
    val question: String,
    val answer: String,
    val options: List<String>,
    val explanation: String
)

/**
 * Parses study material pasted back from an external AI app. The offline
 * generator this file used to hold was removed — the AI-app handoff plus this
 * parser is now the single study path. Tolerant of markdown bold, numbering,
 * preamble chatter, and several common Q/A layouts.
 */
object AiResultParser {

    private val frontMarkers = listOf("q:", "front:", "question:")
    private val backMarkers = listOf("a:", "back:", "answer:")
    private val optionRegex = Regex("""^([A-Da-d])[).:\-]\s*(.+)$""")
    private val answerLineRegex = Regex("""^answer\s*[:\-]?\s*(.+)$""", RegexOption.IGNORE_CASE)
    private val explanationRegex = Regex("""^explanation\s*[:\-]?\s*(.+)$""", RegexOption.IGNORE_CASE)

    /** Strips markdown emphasis, list numbering, and stray bullets from a line. */
    private fun cleanLine(raw: String): String = raw
        .replace(Regex("""\*\*|__|`"""), "")
        .replace(Regex("""^\s*(?:[-*•]|\d+[.)])\s*"""), "")
        .trim()

    private fun markerContent(line: String, markers: List<String>): String? {
        val cleaned = cleanLine(line)
        val lower = cleaned.lowercase()
        markers.forEach { marker ->
            if (lower.startsWith(marker)) return cleaned.substring(marker.length).trim()
        }
        return null
    }

    fun parseFlashcards(text: String): List<Flashcard> {
        if (text.isBlank()) return emptyList()
        val cards = mutableListOf<Flashcard>()
        var front: String? = null
        var back = StringBuilder()
        var inBack = false

        fun flush() {
            val f = front?.trim().orEmpty()
            val b = back.toString().trim()
            if (f.isNotBlank() && b.isNotBlank()) cards.add(Flashcard(f, b))
            front = null
            back = StringBuilder()
            inBack = false
        }

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            val frontContent = markerContent(line, frontMarkers)
            val backContent = markerContent(line, backMarkers)
            when {
                frontContent != null -> {
                    flush()
                    front = frontContent
                }
                backContent != null && front != null -> {
                    inBack = true
                    back.append(backContent)
                }
                line.isBlank() -> if (inBack) flush()
                inBack -> back.append(' ').append(cleanLine(line))
                front != null && markerContent(line, backMarkers) == null && line.isNotBlank() ->
                    front = "${front} ${cleanLine(line)}".trim()
            }
        }
        flush()
        return cards
    }

    fun parseQuiz(text: String): List<QuizQuestion> {
        if (text.isBlank()) return emptyList()
        val questions = mutableListOf<QuizQuestion>()
        var question: String? = null
        val options = linkedMapOf<String, String>()
        var answerKeyOrText: String? = null
        var explanation = ""

        fun flush() {
            val q = question?.trim().orEmpty()
            val answerRaw = answerKeyOrText?.trim().orEmpty()
            if (q.isNotBlank() && options.size >= 2 && answerRaw.isNotBlank()) {
                // "Answer: B" resolves through the option map; otherwise treat as literal text.
                val answerText = options[answerRaw.take(1).uppercase()]
                    ?.takeIf { answerRaw.length <= 2 }
                    ?: answerRaw
                questions.add(
                    QuizQuestion(
                        question = q,
                        answer = answerText,
                        options = options.values.toList(),
                        explanation = explanation.trim()
                    )
                )
            }
            question = null
            options.clear()
            answerKeyOrText = null
            explanation = ""
        }

        text.lineSequence().forEach { rawLine ->
            val line = cleanLine(rawLine)
            val questionContent = markerContent(rawLine.trim(), listOf("q:", "question:"))
            val optionMatch = optionRegex.matchEntire(line)
            val answerMatch = answerLineRegex.matchEntire(line)
            val explanationMatch = explanationRegex.matchEntire(line)
            when {
                questionContent != null -> {
                    flush()
                    question = questionContent
                }
                optionMatch != null && question != null ->
                    options[optionMatch.groupValues[1].uppercase()] = optionMatch.groupValues[2].trim()
                answerMatch != null && question != null -> answerKeyOrText = answerMatch.groupValues[1]
                explanationMatch != null && question != null -> explanation = explanationMatch.groupValues[1]
                question != null && options.isEmpty() && answerKeyOrText == null && line.isNotBlank() ->
                    question = "${question} $line".trim()
            }
        }
        flush()
        return questions
    }
}
