package com.quranapp.android.learning.examples

import com.quranapp.android.learning.pack.WordLocation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LemmaExampleFinderTest {
    // A word that occurs twice in 2:21, once in 112:1 and once in 1:5.
    private val locations = listOf(
        WordLocation(2021, 3),
        WordLocation(2021, 7),
        WordLocation(112001, 1),
        WordLocation(1005, 1),
    )
    private val loaded = mutableListOf<Int>()
    private val finder = LemmaExampleFinder(
        occurrences = { locations },
        loadAyah = { ayahId -> loaded += ayahId; listOf("words of $ayahId") },
    )

    @Test
    fun shortSurahsComeFirst() = runBlocking {
        val examples = finder.find(lemmaId = 1, limit = 10)
        assertEquals(listOf(1 to 5, 112 to 1, 2 to 21), examples.map { it.ayah.surahNo to it.ayah.ayahNo })
    }

    @Test
    fun everyOccurrenceInAnAyahIsHighlighted() = runBlocking {
        val examples = finder.find(lemmaId = 1, limit = 10)
        assertEquals(setOf(3, 7), examples.last().highlightedWordIndexes)
    }

    @Test
    fun onlyTheShownAyahsAreLoaded() = runBlocking {
        finder.find(lemmaId = 1, limit = 2)
        assertEquals(listOf(1005, 112001), loaded)
    }
}
