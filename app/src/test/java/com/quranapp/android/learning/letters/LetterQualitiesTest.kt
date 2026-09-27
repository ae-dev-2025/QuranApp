package com.quranapp.android.learning.letters

import com.quranapp.android.R
import com.quranapp.android.learning.analysis.SifatDetector
import com.quranapp.android.learning.analysis.TestAyahs
import com.quranapp.android.learning.analysis.parseClusters
import com.quranapp.android.learning.concepts.ConceptIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LetterQualitiesTest {
    private fun labels(id: String) = LetterQualities.of(Letters[id]!!).map { it.labelRes }

    @Test
    fun everyLetterHasOneSideOfEachPair() {
        Letters.all.forEach { letter ->
            val concepts = LetterQualities.of(letter).map { it.conceptId }
            listOf(ConceptIds.SIFA_HAMS, ConceptIds.SIFA_SHIDDA, ConceptIds.SIFA_ISTILA, ConceptIds.SIFA_ITBAQ).forEach {
                assertEquals(letter.name + " " + it, 1, concepts.count { c -> c == it })
            }
        }
    }

    @Test
    fun baIsVoicedStrongLoweredOpenAndBounces() {
        assertEquals(
            listOf(R.string.learning_sifa_voiced, R.string.learning_sifa_strong, R.string.learning_sifa_lowered, R.string.learning_sifa_open, R.string.learning_sifa_bouncing),
            labels("letter.ba"),
        )
    }

    @Test
    fun sadIsWhisperedSoftRaisedClosedAndWhistles() {
        assertEquals(
            listOf(R.string.learning_sifa_whispered, R.string.learning_sifa_soft, R.string.learning_sifa_raised, R.string.learning_sifa_closed, R.string.learning_sifa_whistling),
            labels("letter.sad"),
        )
    }

    @Test
    fun raRollsAndLeans() {
        val ra = labels("letter.ra")
        assertTrue(R.string.learning_sifa_middle in ra)
        assertTrue(R.string.learning_sifa_rolling in ra)
        assertTrue(R.string.learning_sifa_leaning in ra)
    }

    @Test
    fun theDetectorFindsTheMarkedSide() {
        // بِسۡمِ: ب strong, س whispered and whistling; nothing raised or closed.
        val found = SifatDetector.detect(parseClusters(TestAyahs.AYAH_1_1[0]))
        assertEquals(setOf(ConceptIds.SIFA_SHIDDA, ConceptIds.SIFA_HAMS, ConceptIds.SIFA_SAFIR), found)
    }
}
