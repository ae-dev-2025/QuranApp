package com.quranapp.android.learning.concepts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrammarCatalogTest {
    @Test
    fun seventyConceptsOnTheGrammarTrack() {
        assertEquals(70, GrammarCatalog.all.size)
        assertEquals(70, GrammarCatalog.all.map { it.id }.toSet().size)
        GrammarCatalog.all.forEach {
            assertTrue(it.id, it.id.startsWith("grammar."))
            assertEquals(it.id, Track.GRAMMAR, it.track)
            assertTrue("${it.id} has its Arabic term (decision 6)", !it.arabicTerm.isNullOrBlank())
        }
    }

    @Test
    fun theyAreInTheMainCatalog() {
        GrammarCatalog.all.forEach { assertEquals(it, ConceptCatalog[it.id]) }
    }

    @Test
    fun stagesFollowTheDesign() {
        assertEquals(1, ConceptStages.of(GrammarIds.ISM))
        assertEquals(2, ConceptStages.of(GrammarIds.CASES))
        assertEquals(3, ConceptStages.of(GrammarIds.IDAFA))
        assertEquals(4, ConceptStages.of(GrammarIds.PAST))
        assertEquals(5, ConceptStages.of(GrammarIds.HAL))
        assertEquals(setOf(1, 2, 3, 4, 5), GrammarCatalog.all.map { ConceptStages.of(it.id) }.toSet())
    }

    @Test
    fun aBeginnerMeetsTheThreeWordTypesFirst() {
        val ready = ConceptGraph().readyToLearn(known = setOf(ConceptIds.LETTERS, ConceptIds.SHORT_VOWELS))
            .filter { it.track == Track.GRAMMAR }
            .map { it.id }
        assertEquals(listOf(GrammarIds.ISM, GrammarIds.FIL, GrammarIds.HARF), ready)
    }
}
