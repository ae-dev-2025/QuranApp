package com.quranapp.android.learning.path

import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.GrammarIds
import com.quranapp.android.learning.letters.Letters
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PathProgressTest {
    private val sukun = ConceptCatalog[ConceptIds.SUKUN]!!
    private val qalqalah = ConceptCatalog[ConceptIds.QALQALAH]!! // stage 1, like the goals below
    private val ism = ConceptCatalog[GrammarIds.ISM]!! // stage 1 too

    /** Every surah needs sukūn and qalqalah, has two words, word.a and word.b, and shows a noun. */
    private fun fakeNeeds(surahs: Collection<Int>) =
        surahs.associateWith { SurahNeeds(it, listOf(sukun, qalqalah), listOf(listOf("word.a"), listOf("word.b")), listOf(ism)) }

    private val loaded = mutableListOf<Int>()
    private val load: suspend (Collection<Int>) -> Map<Int, SurahNeeds> = { surahs -> loaded += surahs; fakeNeeds(surahs) }

    @Test
    fun dots() {
        assertEquals(Dot.UNKNOWN, PathProgress.dot(null, Layer.WORDS))
        assertEquals(Dot.EMPTY, PathProgress.dot(LayerProgress(0, 4), Layer.READ))
        assertEquals(Dot.PARTIAL, PathProgress.dot(LayerProgress(3, 4), Layer.READ))
        assertEquals(Dot.FULL, PathProgress.dot(LayerProgress(4, 4), Layer.READ))
        assertEquals("95% of words is enough", Dot.FULL, PathProgress.dot(LayerProgress(19, 20), Layer.WORDS))
        assertEquals(Dot.PARTIAL, PathProgress.dot(LayerProgress(18, 20), Layer.WORDS))
    }

    @Test
    fun aBeginnerIsInStageZeroAndNothingIsLoaded() = runBlocking {
        assertEquals(0, PathProgress.currentStage(emptySet(), load).number)
        assertEquals(emptyList<Int>(), loaded)
    }

    @Test
    fun knowingTheBasicsMovesOnToStageOne() = runBlocking {
        val stage = PathProgress.currentStage(Curriculum.BASICS.toSet(), load)
        assertEquals(1, stage.number)
        assertFalse("stage 2's surahs aren't loaded yet", 78 in loaded)
    }

    @Test
    fun placementCanStartFurtherOn() = runBlocking {
        assertEquals(2, PathProgress.currentStage(Curriculum.BASICS.toSet() + ConceptIds.SUKUN, load, startAt = 2).number)
    }

    @Test
    fun stageOneIsDoneWhenItsSurahsCanBeReadAndRecitedAndAlFatihahUnderstood() = runBlocking {
        val readAndRecite = Curriculum.BASICS.toSet() + ConceptIds.SUKUN + ConceptIds.QALQALAH
        assertEquals("Al-Fātiḥah's words are still missing", 1, PathProgress.currentStage(readAndRecite, load).number)
        val withWords = readAndRecite + "word.a" + "word.b"
        assertEquals("the word types are still missing", 1, PathProgress.currentStage(withWords, load).number)
        // With everything known, the path ends at its last stage.
        assertEquals(6, PathProgress.currentStage(withWords + GrammarIds.ISM, load).number)
    }

    @Test
    fun grammarGoalsWaitForThePack() = runBlocking {
        val noPack: suspend (Collection<Int>) -> Map<Int, SurahNeeds> = { surahs ->
            surahs.associateWith { SurahNeeds(it, listOf(sukun), listOf(listOf("word.a")), grammar = null) }
        }
        val everything = Curriculum.BASICS.toSet() + ConceptIds.SUKUN + "word.a" + GrammarIds.ISM
        assertEquals("unknown grammar isn't reached grammar", 1, PathProgress.currentStage(everything, noPack).number)
    }

    @Test
    fun nextUnit_isTheFirstSurahWithAGoalLeft() {
        val stage = Curriculum.stages[1]
        val needs = fakeNeeds(PathProgress.goalSurahs(stage))
        // Read and recite are known, but Al-Fātiḥah's words aren't: stage 1 counts them for Al-Fātiḥah only.
        val known = setOf(ConceptIds.SUKUN, ConceptIds.QALQALAH)
        assertEquals(1, PathProgress.nextUnit(stage, needs, known))
        assertNull(PathProgress.nextUnit(stage, needs, known + "word.a" + "word.b" + GrammarIds.ISM))
        // Al-Fātiḥah needs only sukūn and word.a here; Al-Ikhlāṣ, next on the path, also needs qalqalah.
        val fatihahDone = needs + (1 to SurahNeeds(1, listOf(sukun), listOf(listOf("word.a")), grammar = emptyList()))
        assertEquals(112, PathProgress.nextUnit(stage, fatihahDone, setOf(ConceptIds.SUKUN, "word.a")))
    }

    @Test
    fun aStageDoesNotAskForWhatALaterStageTeaches() = runBlocking {
        // Every surah here also needs ikhfāʾ, which stage 2 teaches: stage 1 can be finished without it.
        val ikhfa = ConceptCatalog[ConceptIds.IKHFA]!!
        val withIkhfa: suspend (Collection<Int>) -> Map<Int, SurahNeeds> = { surahs ->
            surahs.associateWith { SurahNeeds(it, listOf(sukun, qalqalah, ikhfa), listOf(listOf("word.a")), listOf(ism)) }
        }
        val stageOneDone = Curriculum.BASICS.toSet() + ConceptIds.SUKUN + ConceptIds.QALQALAH + "word.a" + GrammarIds.ISM
        assertEquals(2, PathProgress.currentStage(stageOneDone, withIkhfa).number)
        // Knowing ikhfāʾ too finishes stage 2 as well.
        assertTrue(PathProgress.currentStage(stageOneDone + ConceptIds.IKHFA, withIkhfa).number > 2)
    }

    @Test
    fun goalSurahs_followThePath() {
        assertEquals(Curriculum.STAGE_ONE_SURAHS, PathProgress.goalSurahs(Curriculum.stages[1]))
        // Stage 2 counts all of Juz ʿAmma: stage 1's surahs first, as on the path.
        assertEquals(Curriculum.LAST_TEN + Curriculum.JUZ_AMMA_REST, PathProgress.goalSurahs(Curriculum.stages[2]))
    }

    @Test
    fun nextConcept_isTheFirstUnknownBasic() {
        val stage = Curriculum.stages[0]
        assertEquals("the letters come first", "letter.alif", PathProgress.nextConcept(stage, emptySet()))
        val letters = Letters.all.map { it.id }.toSet()
        assertEquals(ConceptIds.SHORT_VOWELS, PathProgress.nextConcept(stage, letters))
        assertNull(PathProgress.nextConcept(stage, Curriculum.BASICS.toSet()))
    }
}
