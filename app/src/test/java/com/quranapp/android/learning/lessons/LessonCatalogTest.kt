package com.quranapp.android.learning.lessons

import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonCatalogTest {
    private val lessons = LessonCatalog.all

    @Test
    fun `every lesson belongs to a known concept`() {
        for (lesson in lessons) {
            assertNotNull("Unknown concept ${lesson.conceptId}", ConceptCatalog[lesson.conceptId])
        }
    }

    @Test
    fun `every concept has a lesson, except the umbrella ones`() {
        // Umbrella concepts get a rule overview instead of a lesson (design decision 5).
        val missing = ConceptCatalog.all.map { it.id }
            .filter { it !in ConceptCatalog.umbrellaIds && LessonCatalog[it] == null }
            // Grammar lessons are added unit by unit (#64–#68, #71–#74).
            .filter { ConceptCatalog[it]?.track != Track.GRAMMAR }

        assertTrue("Concepts without a lesson: $missing", missing.isEmpty())
    }

    @Test
    fun `a concept has at most one lesson`() {
        assertEquals(lessons.size, lessons.map { it.conceptId }.toSet().size)
    }

    @Test
    fun `confused-with points to another known concept`() {
        for (lesson in lessons) {
            val other = lesson.confusedWith?.conceptId ?: continue
            assertNotNull("${lesson.conceptId}: unknown concept $other", ConceptCatalog[other])
            assertNotEquals("${lesson.conceptId} is confused with itself", lesson.conceptId, other)
        }
    }

    @Test
    fun `key examples point at a real place in the Quran`() {
        for (lesson in lessons) {
            val example = lesson.keyExample
            assertTrue("${lesson.conceptId}: surah", example.surahNo in 1..114)
            assertTrue("${lesson.conceptId}: ayah", example.ayahNo >= 1)
            assertTrue("${lesson.conceptId}: words", !example.wordIndexes.isEmpty() && example.wordIndexes.first >= 0)
        }
    }
}
