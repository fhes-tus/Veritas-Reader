package com.veritas.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResultParserTest {

    @Test
    fun `parses Q A pairs with preamble chatter`() {
        val cards = AiResultParser.parseFlashcards(
            """
            Sure! Here are your flashcards:

            Q: What is photosynthesis?
            A: The process plants use to convert light into chemical energy.

            Q: Where does it occur?
            A: In the chloroplasts.
            """.trimIndent()
        )
        assertEquals(2, cards.size)
        assertEquals("What is photosynthesis?", cards[0].front)
        assertTrue(cards[1].back.contains("chloroplasts"))
    }

    @Test
    fun `parses Front Back and markdown bold variants`() {
        val cards = AiResultParser.parseFlashcards(
            """
            **Q:** What is osmosis?
            **A:** Movement of water across a membrane.

            Front: Define diffusion
            Back: Movement of particles from high to low concentration.
            """.trimIndent()
        )
        assertEquals(2, cards.size)
        assertEquals("What is osmosis?", cards[0].front)
        assertEquals("Define diffusion", cards[1].front)
    }

    @Test
    fun `multi-line answers are joined`() {
        val cards = AiResultParser.parseFlashcards(
            "Q: List the states of matter\nA: Solid, liquid,\ngas, and plasma."
        )
        assertEquals(1, cards.size)
        assertTrue(cards[0].back.contains("plasma"))
    }

    @Test
    fun `garbage input produces no cards`() {
        assertTrue(AiResultParser.parseFlashcards("just some prose with no structure").isEmpty())
        assertTrue(AiResultParser.parseFlashcards("").isEmpty())
    }

    @Test
    fun `parses quiz with letter answer resolution`() {
        val quiz = AiResultParser.parseQuiz(
            """
            Q: Which organelle produces ATP?
            A) Nucleus
            B) Mitochondria
            C) Ribosome
            D) Golgi body
            Answer: B
            Explanation: Mitochondria are the powerhouse of the cell.
            """.trimIndent()
        )
        assertEquals(1, quiz.size)
        assertEquals("Mitochondria", quiz[0].answer)
        assertEquals(4, quiz[0].options.size)
        assertTrue(quiz[0].explanation.contains("powerhouse"))
    }

    @Test
    fun `parses multiple quiz questions separated by blank lines`() {
        val quiz = AiResultParser.parseQuiz(
            """
            Q: First question?
            A) one
            B) two
            Answer: A

            Q: Second question?
            A) alpha
            B) beta
            C) gamma
            Answer: C
            Explanation: Because gamma.
            """.trimIndent()
        )
        assertEquals(2, quiz.size)
        assertEquals("one", quiz[0].answer)
        assertEquals("gamma", quiz[1].answer)
    }

    @Test
    fun `quiz without enough options is dropped`() {
        val quiz = AiResultParser.parseQuiz("Q: Broken?\nA) only one option\nAnswer: A")
        assertTrue(quiz.isEmpty())
    }
}
