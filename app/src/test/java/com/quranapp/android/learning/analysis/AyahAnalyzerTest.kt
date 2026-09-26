package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_1
import com.quranapp.android.learning.concepts.ConceptIds.LAM_OF_ALLAH
import com.quranapp.android.learning.concepts.ConceptIds.LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.SUKUN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AyahAnalyzerTest {
    @Test
    fun `combines reading and tajweed concepts with their word indexes`() {
        val analysis = AyahAnalyzer.analyze(AYAH_1_1)

        assertEquals(listOf(0, 1, 2, 3), analysis.wordsByConcept[LETTERS])
        assertEquals(listOf(0, 2), analysis.wordsByConcept[SUKUN])
        assertEquals(listOf(1), analysis.wordsByConcept[LAM_OF_ALLAH])
    }

    @Test
    fun `the ayah number marker is ignored`() {
        val withMarker = AyahAnalyzer.analyze(AYAH_1_1 + "١")

        assertEquals(AyahAnalyzer.analyze(AYAH_1_1), withMarker)
    }

    @Test
    fun `an empty ayah has no concepts`() {
        assertTrue(AyahAnalyzer.analyze(emptyList()).conceptIds.isEmpty())
    }
}
