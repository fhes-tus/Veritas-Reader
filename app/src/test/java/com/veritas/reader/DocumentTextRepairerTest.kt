package com.veritas.reader

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentTextRepairerTest {

    @Test
    fun `repairPseudoTables restores corrupted Atomic Habits text to clean paragraphs`() {
        val corrupted = """
            | me | to | a | larger | hospital | in | Cincinnati. |
            | I | was | rolled | out | of | the | emergency | room | doors | and | toward | the |
            | helipad | across | the | street. | The | stretcher | rattled | on | a | bumpy |
            sidewalk | as |
            | one | nurse | pushed | me | along | while | another | pumped | each | breath |
            into | me |
            | by | hand. | My | mother, | who | had | arrived | at | the | hospital | a | few |
            moments |
            | before, | climbed | into | the | helicopter | beside | me. | I | remained |
            unconscious |
            | and | unable | to | breathe | on | my | own | as | she | held | my | hand | during |
            the |

            flight.
        """.trimIndent()

        val repaired = DocumentTextRepairer.repairPseudoTables(corrupted)

        // Must NOT contain any pipe symbols
        assertFalse("Repaired text must not contain pipe symbols", repaired.contains("|"))

        // Must restore continuous running sentences
        assertTrue("Must contain restored Cincinnati sentence", repaired.contains("me to a larger hospital in Cincinnati."))
        assertTrue(
            "Must merge wrapped lines into smooth paragraph",
            repaired.contains("I was rolled out of the emergency room doors and toward the helipad across the street. The stretcher rattled on a bumpy sidewalk as one nurse pushed me along while another pumped each breath into me by hand. My mother, who had arrived at the hospital a few moments before, climbed into the helicopter beside me. I remained unconscious and unable to breathe on my own as she held my hand during the flight.")
        )
    }

    @Test
    fun `repairPseudoTables leaves real markdown tables untouched`() {
        val realTable = """
            # Summary Table

            | Header 1 | Header 2 | Header 3 |
            | --- | --- | --- |
            | Value A | Value B | Value C |
            | Value D | Value E | Value F |

            This is regular text following the table.
        """.trimIndent()

        val repaired = DocumentTextRepairer.repairPseudoTables(realTable)

        assertTrue("Real markdown table with divider must be preserved", repaired.contains("| Header 1 | Header 2 | Header 3 |"))
        assertTrue("Real table divider must be preserved", repaired.contains("| --- | --- | --- |"))
    }
}
