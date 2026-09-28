package com.quranapp.android.learning.ui

import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.SegmentEntity
import com.quranapp.android.learning.words.WordRepository
import org.junit.Assert.assertEquals
import org.junit.Test

/** Al-Fatihah 1:5, إِيَّاكَ نَعۡبُدُ وَإِيَّاكَ نَسۡتَعِينُ: إِيَّا is in words 0 and 2. */
class WordEntriesTest {
    private val iyya = LemmaEntity(1, "<iy~aA", "إِيَّا", "corpus", "PRON", null, null, 1, null)
    private val worship = LemmaEntity(2, "Eabada", "عَبَدَ", "corpus", "V", null, null, 1, null)
    private val words = WordRepository.assemble(
        ayahId = 1005,
        segments = listOf(segment(0, iyya), segment(1, worship), segment(2, iyya)),
        lemmas = listOf(iyya, worship),
        roots = emptyList(),
        glosses = emptyList(),
        syntax = emptyList(),
    )

    @Test
    fun eachWrittenFormKeepsWhereItIsInTheAyah() {
        val entries = wordEntries(words, listOf("إِيَّاكَ", "نَعۡبُدُ", "وَإِيَّاكَ"))

        val first = entries.first()
        assertEquals(listOf("إِيَّاكَ", "وَإِيَّاكَ"), first.texts)
        assertEquals(listOf(0, 2), first.wordIndexes) // so the reader's script can draw words 0 and 2
        assertEquals(listOf(1), entries[1].wordIndexes)
    }

    @Test
    fun aRepeatedFormIsShownOnce() {
        val entries = wordEntries(words, listOf("إِيَّاكَ", "نَعۡبُدُ", "إِيَّاكَ"))

        assertEquals(listOf("إِيَّاكَ"), entries.first().texts)
        assertEquals(listOf(0), entries.first().wordIndexes)
    }

    private fun segment(word: Int, lemma: LemmaEntity) = SegmentEntity(1005, word, 0, "form", "STEM", "T", lemma.lemmaId, "")
}
