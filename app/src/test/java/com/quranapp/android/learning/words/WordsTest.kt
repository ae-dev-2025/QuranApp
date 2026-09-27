package com.quranapp.android.learning.words

import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.RootEntity
import com.quranapp.android.learning.pack.SegmentEntity
import com.quranapp.android.learning.pack.SyntaxEntity
import com.quranapp.android.learning.pack.WordGlossEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Al-Fatihah 1:5 as the pack stores it: إِيَّاكَ نَعۡبُدُ وَإِيَّاكَ نَسۡتَعِينُ. */
class WordsTest {
    private val iyya = lemma(1, "<iy~aA", rootId = null)
    private val worship = lemma(2, "Eabada", rootId = 10)
    private val seekHelp = lemma(3, "{sotaEiynu", rootId = 11)
    private val roots = listOf(RootEntity(10, "Ebd", "ع ب د", 275), RootEntity(11, "Ewn", "ع و ن", 11))

    private val segments = listOf(
        segment(0, 0, "STEM", iyya.lemmaId),
        segment(1, 0, "STEM", worship.lemmaId),
        segment(2, 1, "STEM", iyya.lemmaId), // listed out of order on purpose
        segment(2, 0, "PREFIX", null), // وَ has no lemma
        segment(3, 0, "STEM", seekHelp.lemmaId),
    )

    private val words = WordRepository.assemble(
        ayahId = 1005,
        segments = segments,
        lemmas = listOf(iyya, worship, seekHelp),
        roots = roots,
        glosses = listOf(WordGlossEntity(1005, 1, "we worship")),
        syntax = listOf(syntax(1, 1, "IV"), syntax(1, 0, null)),
    )

    @Test
    fun assemble_makesOneWordPerPositionInOrder() {
        assertEquals(listOf(0, 1, 2, 3), words.map { it.wordIndex })
        assertEquals(listOf("PREFIX", "STEM"), words[2].segments.map { it.kind })
    }

    @Test
    fun assemble_attachesLemmasRootsGlossesAndSyntax() {
        assertEquals(listOf("<iy~aA"), words[2].lemmas.map { it.lemma.lemmaKey })
        assertEquals("ع ب د", words[1].lemmas.single().root?.letters)
        assertNull("إِيَّا has no root", words[0].lemmas.single().root)
        assertEquals("we worship", words[1].gloss)
        assertNull(words[0].gloss)
        assertEquals(listOf(0, 1), words[1].syntax.map { it.segmentIndex })
    }

    @Test
    fun itemIds_roundTrip() {
        assertEquals("word.Eabada", WordItems.idOf("Eabada"))
        assertEquals("Eabada", WordItems.lemmaKeyOf("word.Eabada"))
        assertNull(WordItems.lemmaKeyOf("tajweed.ikhfa"))
    }

    @Test
    fun coverage_countsWordsAsTheyAppear() {
        // إِيَّا occurs twice, so knowing it covers 2 of the 4 words.
        val coverage = WordCoverage.of(words, setOf(WordItems.idOf("<iy~aA")))
        assertEquals(2, coverage.knownWords)
        assertEquals(4, coverage.countedWords)
        assertEquals(50, coverage.percent)
        assertEquals(listOf("Eabada", "{sotaEiynu"), coverage.unknownLemmas.map { it.lemma.lemmaKey })
    }

    @Test
    fun coverage_ignoresWordsWithoutALemma() {
        val pronoun = AyahWord(1005, 9, listOf(segment(9, 0, "STEM", null)), emptyList(), null, emptyList())
        val coverage = WordCoverage.of(words + pronoun, emptySet())
        assertEquals(4, coverage.countedWords)
        assertEquals(0, coverage.percent)
    }

    private fun lemma(id: Int, key: String, rootId: Int?) =
        LemmaEntity(id, key, "headword", "corpus", "V", rootId, null, 1, null)

    private fun segment(word: Int, index: Int, kind: String, lemmaId: Int?) =
        SegmentEntity(1005, word, index, "form", kind, "T", lemmaId, "")

    private fun syntax(word: Int, index: Int, role: String?) =
        SyntaxEntity(1005, word, index, "text", "TAG", "Stem", null, role, null, null, null, null, null)
}
