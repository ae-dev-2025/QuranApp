package com.quranapp.android.learning.examples

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GrammarExampleFinderTest {
    // Al-Fātiḥah 1:5 and Al-Ikhlāṣ 112:3 both have a present-tense verb; Al-Baqarah 2:2 too.
    private val grammar = mapOf(
        1 to mapOf(1004 to mapOf("grammar.ism" to listOf(0)), 1005 to mapOf("grammar.present" to listOf(1, 3))),
        2 to mapOf(2002 to mapOf("grammar.present" to listOf(5))),
        112 to mapOf(112003 to mapOf("grammar.present" to listOf(1, 3))),
    )
    private val asked = mutableListOf<Int>()
    private val finder = GrammarExampleFinder(
        grammarOfSurah = { surahNo -> asked += surahNo; grammar[surahNo].orEmpty() },
        loadAyah = { ayahId -> listOf("word of $ayahId") },
    )

    @Test
    fun shortSurahsFirstAndTheWordsToHighlight() = runBlocking {
        val found = finder.find("grammar.present", limit = 2)
        assertEquals(listOf(1 to 5, 112 to 3), found.map { it.ayah.surahNo to it.ayah.ayahNo })
        assertEquals(setOf(1, 3), found.first().highlightedWordIndexes)
        assertEquals(listOf("word of 1005"), found.first().ayah.words)
    }

    @Test
    fun showMoreDoesNotAskThePackAgain() = runBlocking {
        finder.find("grammar.present", limit = 2)
        val before = asked.size
        assertEquals(3, finder.find("grammar.present", limit = 3).size) // goes on to Al-Baqarah
        assertEquals("only the surahs after Al-Ikhlāṣ are new", listOf(113, 114, 2), asked.drop(before))
    }

    @Test
    fun nothingWithoutThePack() = runBlocking {
        val noPack = GrammarExampleFinder(grammarOfSurah = { null }, loadAyah = { emptyList() })
        assertEquals(emptyList<ConceptExample>(), noPack.find("grammar.present", limit = 3))
    }
}
