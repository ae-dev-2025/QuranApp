package com.quranapp.android.learning.lessons

import com.quranapp.android.learning.concepts.ConceptCatalog
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
