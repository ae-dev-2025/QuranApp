package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.analysis.TestAyahs.AYAH_112_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_112_4
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_7
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_NO_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.LAM_OF_ALLAH
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConceptIndexTest {
    private val ayahs = mapOf(1001 to AYAH_1_1, 1007 to AYAH_1_7, 112001 to AYAH_112_1, 112004 to AYAH_112_4)
    private val index = ConceptIndex.of(ayahs)

    @Test
    fun findsWhatTheAnalyzerFinds() {
        assertArrayEquals(intArrayOf(1001, 112001), index.ayahsOf(LAM_OF_ALLAH))
        assertArrayEquals(intArrayOf(112004), index.ayahsOf(IDGHAM_NO_GHUNNAH))
        for ((ayahId, words) in ayahs) {
            for (conceptId in AyahAnalyzer.analyze(words).conceptIds) assertTrue(ayahId in index.ayahsOf(conceptId))
        }
    }

    @Test
    fun aSurahsConceptsAreAllOfItsAyahs() {
        val expected = AyahAnalyzer.analyze(AYAH_112_1).conceptIds + AyahAnalyzer.analyze(AYAH_112_4).conceptIds
        assertEquals(expected, index.conceptsOf(112))
        assertEquals(emptySet<String>(), index.conceptsOf(2))
    }

    @Test
    fun anUnknownConceptIsNowhere() {
        assertEquals(0, index.ayahsOf("reading.not_a_concept").size)
    }

    @Test
    fun consecutiveAyahsAreWrittenAsARun() {
        val text = StringBuilder().also { ConceptIndex(mapOf("a" to intArrayOf(1001, 1002, 1003, 1005, 2001, 2002))).write(it) }
        assertTrue(text.lines().contains("a 1001-1003,1005,2001-2002"))
    }

    @Test
    fun whatIsWrittenReadsBackTheSame() {
        val text = StringBuilder().also { index.write(it) }.toString()
        assertEquals(index, ConceptIndex.parse(text.lineSequence()))
    }
}
