package com.quranapp.android.learning.concepts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConceptStagesTest {
    @Test
    fun noConceptIsTaughtBeforeWhatItBuildsOn() {
        ConceptCatalog.all.forEach { concept ->
            concept.prerequisites.forEach { prerequisite ->
                assertTrue(
                    "${concept.id} (stage ${ConceptStages.of(concept.id)}) needs $prerequisite (stage ${ConceptStages.of(prerequisite)})",
                    ConceptStages.of(prerequisite) <= ConceptStages.of(concept.id),
                )
            }
        }
    }

    @Test
    fun stagesFollowTheDesignsCatalog() {
        assertEquals(0, ConceptStages.of(ConceptIds.SUKUN))
        assertEquals(0, ConceptStages.of(ConceptIds.HEAVY_LETTERS))
        assertEquals(0, ConceptStages.of(ConceptIds.MAKHRAJ_THROAT))
        assertEquals(1, ConceptStages.of(ConceptIds.HAMZAT_WASL))
        assertEquals(1, ConceptStages.of(ConceptIds.QALQALAH))
        assertEquals(2, ConceptStages.of(ConceptIds.IKHFA))
        assertEquals(2, ConceptStages.of(ConceptIds.MADD_MUTTASIL))
    }
}
