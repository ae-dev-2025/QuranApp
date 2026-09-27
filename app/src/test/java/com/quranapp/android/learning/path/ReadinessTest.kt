package com.quranapp.android.learning.path

import com.quranapp.android.learning.analysis.TestAyahs
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessTest {
    // Al-Ikhlāṣ 112:1 and Al-Fātiḥah 1:1 as one-ayah "surahs", with the ayah number the app stores.
    private val ikhlas = SurahNeeds.conceptsOf(listOf(TestAyahs.AYAH_112_1 + "١"))
    private val fatihah = SurahNeeds.conceptsOf(listOf(TestAyahs.AYAH_1_1 + "١"))

    private fun needs(surahNo: Int, concepts: List<Concept>, words: List<List<String>>? = null) =
        SurahNeeds(surahNo, concepts, words)

    @Test
    fun conceptsOf_includesPrerequisitesInLearningOrder() {
        val ids = ikhlas.map { it.id }
        assertTrue(ConceptIds.SUKUN in ids) // قُلۡ
        assertTrue("sukūn needs the letters first", ids.indexOf(ConceptIds.LETTERS) < ids.indexOf(ConceptIds.SUKUN))
    }

    @Test
    fun readAndRecite_countTheirOwnTrack() {
        val surah = needs(112, ikhlas)
        val read = Readiness.of(listOf(surah), Layer.READ, known = setOf(ConceptIds.LETTERS))!!
        assertEquals(ikhlas.count { it.track == Track.READING }, read.total)
        assertEquals(1, read.known)
        val recite = Readiness.of(listOf(surah), Layer.RECITE, known = emptySet())!!
        assertEquals(ikhlas.count { it.track == Track.TAJWEED }, recite.total)
    }

    @Test
    fun upToStage_leavesOutWhatLaterStagesTeach() {
        val surah = needs(113, listOf(ConceptCatalog[ConceptIds.QALQALAH]!!, ConceptCatalog[ConceptIds.IKHFA]!!))
        assertEquals(LayerProgress(0, 2), Readiness.of(listOf(surah), Layer.RECITE, emptySet()))
        assertEquals("ikhfāʾ is stage 2", LayerProgress(0, 1), Readiness.of(listOf(surah), Layer.RECITE, emptySet(), upToStage = 1))
    }

    @Test
    fun aConceptTwoSurahsNeedCountsOnce() {
        val both = Readiness.of(listOf(needs(1, fatihah), needs(112, ikhlas)), Layer.READ, emptySet())!!
        val union = (fatihah + ikhlas).filter { it.track == Track.READING }.map { it.id }.toSet()
        assertEquals(union.size, both.total)
    }

    @Test
    fun words_countEveryWordAndNeedEveryDictionaryWordInIt() {
        // وَمِمَّا-like word needing two dictionary words, and a word repeated twice.
        val words = listOf(listOf("word.a"), listOf("word.min", "word.maA"), listOf("word.a"))
        val surah = needs(2, emptyList(), words)
        assertEquals(LayerProgress(2, 3), Readiness.of(listOf(surah), Layer.WORDS, setOf("word.a")))
        assertEquals(LayerProgress(3, 3), Readiness.of(listOf(surah), Layer.WORDS, setOf("word.a", "word.min", "word.maA")))
    }

    @Test
    fun words_areUnknownWithoutThePack() {
        assertNull(Readiness.of(listOf(needs(112, ikhlas, words = null)), Layer.WORDS, emptySet()))
    }

    @Test
    fun goals_areReachedAtTheirPercent() {
        val words = List(19) { listOf("word.known") } + listOf(listOf("word.new"))
        val bySurah = mapOf(2 to needs(2, emptyList(), words))
        val known = setOf("word.known") // 19 of 20 = 95%
        assertTrue(Readiness.isReached(StageGoal.Surahs(Layer.WORDS, listOf(2), percent = 95), bySurah, known))
        assertFalse(Readiness.isReached(StageGoal.Surahs(Layer.WORDS, listOf(2), percent = 98), bySurah, known))
        assertFalse("no pack, not reached", Readiness.isReached(StageGoal.Surahs(Layer.WORDS, listOf(3), 95), emptyMap(), known))
    }

    @Test
    fun conceptGoals_countTheirConcepts() {
        val goal = StageGoal.Concepts(listOf(ConceptIds.LETTERS, ConceptIds.SUKUN))
        assertEquals(LayerProgress(1, 2), Readiness.of(goal, emptyMap(), setOf(ConceptIds.SUKUN)))
        assertTrue(Readiness.isReached(goal, emptyMap(), setOf(ConceptIds.SUKUN, ConceptIds.LETTERS)))
    }

    @Test
    fun unknownWords_mostFrequentInTheSurahFirst() {
        val words = listOf(listOf("word.b"), listOf("word.a"), listOf("word.a"), listOf("word.c", "word.a"), listOf("word.k"))
        val surah = needs(2, emptyList(), words)
        assertEquals(listOf("word.a", "word.b", "word.c"), Readiness.unknownWords(surah, known = setOf("word.k")))
    }

    @Test
    fun layerProgress_ofNothingIsComplete() {
        assertEquals(100, LayerProgress(0, 0).percent)
        assertTrue(LayerProgress(0, 0).reaches(100))
    }
}
