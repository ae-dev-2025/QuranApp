package com.quranapp.android.learning.examples

import com.quranapp.android.learning.analysis.TestAyahs.AYAH_112_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_112_4
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_7
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_10
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_2
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_NO_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IQLAB
import com.quranapp.android.learning.concepts.ConceptIds.LAM_OF_ALLAH
import com.quranapp.android.learning.concepts.ConceptIds.NOON_SAKINAH
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConceptExampleFinderTest {
    /** A tiny "Quran" with a few real ayahs; every other surah is empty. */
    private val fakeQuran = mapOf(
        1 to listOf(AyahWords(1, 1, AYAH_1_1), AyahWords(1, 7, AYAH_1_7)),
        2 to listOf(AyahWords(2, 2, AYAH_2_2), AyahWords(2, 10, AYAH_2_10)),
        112 to listOf(AyahWords(112, 1, AYAH_112_1), AyahWords(112, 4, AYAH_112_4)),
    )

    private val loadedSurahs = mutableListOf<Int>()

    private val finder = ConceptExampleFinder { surahNo ->
        loadedSurahs += surahNo
        fakeQuran[surahNo].orEmpty()
    }

    private fun find(conceptId: String, limit: Int) = runBlocking { finder.find(conceptId, limit) }

    private fun refs(examples: List<ConceptExample>) =
        examples.map { "${it.ayah.surahNo}:${it.ayah.ayahNo}" }

    @Test
    fun `short surahs come before Al-Baqarah`() {
        // Idgham without ghunnah occurs in 2:2 and 112:4. Juz 'Amma is searched first.
        assertEquals(listOf("112:4", "2:2"), refs(find(IDGHAM_NO_GHUNNAH, limit = 5)))
    }

    @Test
    fun `Al-Fatihah comes first`() {
        assertEquals(listOf("1:1", "112:1"), refs(find(LAM_OF_ALLAH, limit = 2)))
    }

    @Test
    fun `stops searching once the limit is reached`() {
        find(LAM_OF_ALLAH, limit = 1)

        assertEquals(listOf(1), loadedSurahs)
    }

    @Test
    fun `highlights both words of a rule that joins two words`() {
        val example = find(IDGHAM_NO_GHUNNAH, limit = 1).single()

        // 112:4 وَلَمۡ يَكُن لَّهُۥ: words 1 and 2.
        assertEquals(setOf(1, 2), example.highlightedWordIndexes)
    }

    @Test
    fun `umbrella concepts have no direct examples`() {
        // Noon sakinah is never reported itself, only its rules (iqlab etc.).
        assertTrue(find(NOON_SAKINAH, limit = 3).isEmpty())
        assertEquals(listOf("2:10"), refs(find(IQLAB, limit = 3)))
    }

    @Test
    fun `the search order covers every surah exactly once`() {
        assertEquals((1..114).toList(), ConceptExampleFinder.SEARCH_ORDER.sorted())
    }
}
