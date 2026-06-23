package com.veritas.reader

import java.util.Locale
import kotlin.math.ln

/**
 * Lightweight offline study helper.
 *
 * This is deliberately local and deterministic. It does not call a cloud AI service.
 * It gives the reader useful study outputs immediately while leaving room for a
 * later cloud/LLM layer.
 */
data class StudyPack(
    val summary: List<String>,
    val keyPoints: List<String>,
    val keyTerms: List<String>,
    val flashcards: List<Flashcard>,
    val quiz: List<QuizQuestion>,
    val currentSectionExplanation: List<String>
)

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

object StudyAssistant {
    private const val MAX_STUDY_CHARS = 60_000
    private const val MAX_STUDY_CHUNKS = 180
    private const val MAX_SECTION_CHARS = 8_000
    private const val MAX_SENTENCES = 520

    private val stopWords = setOf(
        "the", "and", "for", "that", "this", "with", "from", "have", "has", "had", "are", "was", "were",
        "will", "would", "could", "should", "into", "onto", "about", "after", "before", "than", "then", "there",
        "their", "they", "them", "you", "your", "our", "ours", "his", "her", "hers", "its", "not", "can",
        "may", "might", "also", "such", "these", "those", "been", "being", "between", "during", "within", "without",
        "because", "therefore", "however", "using", "used", "use", "over", "under", "more", "most", "some", "any",
        "each", "other", "when", "where", "which", "what", "who", "how", "why", "through", "while", "both", "very",
        "in", "on", "at", "to", "of", "a", "an", "is", "it", "as", "by", "be", "or", "if", "so", "we", "he", "she", "i"
    )

    fun buildStudyPack(title: String, chunks: List<String>, currentIndex: Int): StudyPack {
        val text = buildStudyText(chunks, currentIndex)
        if (text.isBlank()) {
            return StudyPack(
                summary = listOf("No readable text is available for study tools."),
                keyPoints = emptyList(),
                keyTerms = emptyList(),
                flashcards = emptyList(),
                quiz = emptyList(),
                currentSectionExplanation = emptyList()
            )
        }

        val sentences = splitSentences(text)
        val frequencies = wordFrequencies(text)
        val scored = sentences
            .map { sentence -> sentence to sentenceScore(sentence, frequencies) }
            .sortedByDescending { it.second }

        val summary = preserveOriginalOrder(sentences, scored.take(4).map { it.first })
            .ifEmpty { listOf(text.take(280)) }

        val keyPoints = preserveOriginalOrder(sentences, scored.take(8).map { it.first })
            .map { cleanSentence(it) }
            .take(8)

        val terms = keyTerms(text, frequencies).take(16)
        val flashcards = buildFlashcards(terms, sentences).take(10)
        val quiz = buildQuiz(terms, sentences).take(6)
        val currentChunk = chunks.getOrNull(currentIndex.coerceIn(0, (chunks.size - 1).coerceAtLeast(0))).orEmpty()
            .take(MAX_SECTION_CHARS)
        val currentSectionExplanation = explainCurrentSection(currentChunk, title)

        return StudyPack(
            summary = summary.map { cleanSentence(it) },
            keyPoints = keyPoints,
            keyTerms = terms,
            flashcards = flashcards,
            quiz = quiz,
            currentSectionExplanation = currentSectionExplanation
        )
    }

    private fun splitSentences(text: String): List<String> {
        return text
            .replace(Regex("\\s+"), " ")
            .split(Regex("(?<=[.!?])\\s+"))
            .asSequence()
            .map { it.trim() }
            .filter { it.length >= 35 }
            .take(MAX_SENTENCES)
            .toList()
    }

    private fun buildStudyText(chunks: List<String>, currentIndex: Int): String {
        if (chunks.isEmpty()) return ""
        val safeIndex = currentIndex.coerceIn(0, chunks.lastIndex)
        val indices = linkedSetOf<Int>()
        fun addRange(range: IntRange) {
            range.forEach { index ->
                if (index in chunks.indices && indices.size < MAX_STUDY_CHUNKS) indices.add(index)
            }
        }

        addRange(0..24)
        addRange((safeIndex - 35)..(safeIndex + 35))
        addRange((chunks.lastIndex - 24)..chunks.lastIndex)
        val step = (chunks.size / MAX_STUDY_CHUNKS).coerceAtLeast(1)
        for (index in chunks.indices step step) {
            if (indices.size >= MAX_STUDY_CHUNKS) break
            indices.add(index)
        }

        val builder = StringBuilder()
        indices.sorted().forEach { index ->
            if (builder.length >= MAX_STUDY_CHARS) return@forEach
            val remaining = MAX_STUDY_CHARS - builder.length
            val chunk = chunks[index].replace(Regex("\\s+"), " ").trim()
            if (chunk.isBlank()) return@forEach
            if (builder.isNotEmpty()) builder.append("\n\n")
            builder.append(chunk.take(remaining))
        }
        return builder.toString().trim()
    }

    private fun wordFrequencies(text: String): Map<String, Int> {
        return words(text)
            .filter { it.length >= 4 && it !in stopWords && !it.all { char -> char.isDigit() } }
            .groupingBy { it }
            .eachCount()
    }

    private fun words(text: String): List<String> {
        return Regex("[A-Za-z][A-Za-z0-9'-]*")
            .findAll(text.lowercase(Locale.getDefault()))
            .map { it.value.trim('\'', '-', '’') }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun sentenceScore(sentence: String, frequencies: Map<String, Int>): Double {
        val sentenceWords = words(sentence).filter { it !in stopWords }
        if (sentenceWords.isEmpty()) return 0.0

        val base = sentenceWords.sumOf { frequencies[it] ?: 0 }.toDouble() / sentenceWords.size.coerceAtLeast(1)
        val cueBonus = when {
            sentence.contains(Regex("\\b(important|therefore|because|result|conclude|means|shows|indicates|causes|effect|purpose|function|principle|method|process)\\b", RegexOption.IGNORE_CASE)) -> 1.35
            else -> 1.0
        }
        val lengthPenalty = when {
            sentence.length < 60 -> 0.85
            sentence.length > 320 -> 0.80
            else -> 1.0
        }
        return base * cueBonus * lengthPenalty
    }

    private fun preserveOriginalOrder(allSentences: List<String>, selected: List<String>): List<String> {
        val selectedSet = selected.toSet()
        return allSentences.filter { it in selectedSet }
    }

    private fun keyTerms(text: String, frequencies: Map<String, Int>): List<String> {
        val capitalizedTerms = Regex("\\b[A-Z][A-Za-z0-9-]{3,}(?:\\s+[A-Z][A-Za-z0-9-]{3,}){0,2}\\b")
            .findAll(text)
            .map { it.value.trim() }
            .filterNot { candidate -> candidate.lowercase(Locale.getDefault()).split(" ").all { it in stopWords } }
            .toList()

        val scoredCapitalized = capitalizedTerms
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
            .map { it.key }

        val frequent = frequencies.entries
            .filter { it.value >= 2 }
            .sortedByDescending { it.value * ln((it.key.length + 1).toDouble()) }
            .map { it.key.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) } }

        return (scoredCapitalized + frequent)
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .distinctBy { it.lowercase(Locale.getDefault()) }
            .take(20)
    }

    private fun buildFlashcards(terms: List<String>, sentences: List<String>): List<Flashcard> {
        return terms.mapNotNull { term ->
            val sentence = findSentenceWithTerm(term, sentences) ?: return@mapNotNull null
            Flashcard(
                front = "What should you remember about $term?",
                back = cleanSentence(sentence)
            )
        }
    }

    private fun buildQuiz(terms: List<String>, sentences: List<String>): List<QuizQuestion> {
        if (terms.size < 3) return emptyList()
        return terms.mapIndexedNotNull { index, term ->
            val sentence = findSentenceWithTerm(term, sentences) ?: return@mapIndexedNotNull null
            val distractors = terms
                .filterNot { it.equals(term, ignoreCase = true) }
                .drop(index % terms.size)
                .take(3)
                .ifEmpty { terms.filterNot { it.equals(term, ignoreCase = true) }.take(3) }
            val options = (listOf(term) + distractors).distinct().take(4)
            if (options.size < 3) return@mapIndexedNotNull null
            QuizQuestion(
                question = "Which term best fits this idea?\n\"${blankTerm(sentence, term)}\"",
                answer = term,
                options = options,
                explanation = cleanSentence(sentence)
            )
        }
    }

    private fun findSentenceWithTerm(term: String, sentences: List<String>): String? {
        val escaped = Regex.escape(term)
        return sentences.firstOrNull { it.contains(Regex("\\b$escaped\\b", RegexOption.IGNORE_CASE)) }
            ?: sentences.firstOrNull { it.contains(term, ignoreCase = true) }
    }

    private fun blankTerm(sentence: String, term: String): String {
        return sentence.replace(Regex(Regex.escape(term), RegexOption.IGNORE_CASE), "_____").let { cleanSentence(it) }
    }

    private fun explainCurrentSection(section: String, title: String): List<String> {
        val cleaned = section.replace(Regex("\\s+"), " ").trim()
        if (cleaned.isBlank()) return listOf("No section is selected for explanation.")

        val sentences = splitSentences(cleaned).ifEmpty { listOf(cleaned) }
        val localFrequencies = wordFrequencies(cleaned)
        val localTerms = keyTerms(cleaned, localFrequencies).take(5)
        val simpleIdea = sentences.maxByOrNull { sentenceScore(it, localFrequencies) } ?: cleaned.take(240)

        val output = mutableListOf<String>()
        output.add("Main idea: ${cleanSentence(simpleIdea)}")
        if (localTerms.isNotEmpty()) {
            output.add("Key terms: ${localTerms.joinToString(", ")}")
        }
        output.add("In simpler terms: this section is giving one part of the argument or information in ${title.ifBlank { "the document" }}. Focus on what changed, what caused it, what it proves, or what action it recommends.")
        return output
    }

    private fun cleanSentence(sentence: String): String {
        return sentence.replace(Regex("\\s+"), " ").trim().trim('-', '•', '–')
    }
}
