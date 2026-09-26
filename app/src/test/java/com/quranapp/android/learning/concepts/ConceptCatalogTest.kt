package com.quranapp.android.learning.concepts

import java.lang.reflect.Modifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the rules that the rest of learning mode relies on. If you add a concept and one of
 * these fails, the message tells you what to fix.
 */
class ConceptCatalogTest {
    private val concepts = ConceptCatalog.all

    @Test
    fun `concept IDs are unique`() {
        val duplicates = concepts.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue("Duplicate concept IDs: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `every prerequisite exists in the catalog`() {
        for (concept in concepts) {
            for (prerequisite in concept.prerequisites) {
                assertNotNull(
                    "${concept.id} requires unknown concept $prerequisite",
                    ConceptCatalog[prerequisite],
                )
            }
        }
    }

    @Test
    fun `every concept comes after its prerequisites`() {
        val seen = mutableSetOf<String>()

        for (concept in concepts) {
            for (prerequisite in concept.prerequisites) {
                assertTrue(
                    "${concept.id} is listed before its prerequisite $prerequisite",
                    prerequisite in seen,
                )
            }
            seen += concept.id
        }
    }

    @Test
    fun `every ID constant has a concept`() {
        // Read all `const val` strings declared in ConceptIds using reflection.
        val declaredIds = ConceptIds::class.java.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { it.get(null) as String }
            .toSet()

        assertEquals(declaredIds, concepts.map { it.id }.toSet())
    }

    @Test
    fun `ID prefix matches the track`() {
        for (concept in concepts) {
            val expectedPrefix = concept.track.name.lowercase() + "."
            assertTrue(
                "${concept.id} should start with $expectedPrefix",
                concept.id.startsWith(expectedPrefix),
            )
        }
    }
}
