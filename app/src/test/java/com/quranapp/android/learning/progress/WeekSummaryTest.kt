package com.quranapp.android.learning.progress

import com.quranapp.android.learning.concepts.ConceptIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekSummaryTest {
    private fun known(id: String) = ConceptProgressEntity(id, ConceptStatus.KNOWN, 1)
    private fun log(rating: ReviewRating) = ReviewLogEntity(itemId = "word.a", rating = rating, reviewedAt = 1, stateBefore = null)

    @Test
    fun countsWordsRulesAndReviews() {
        val week = WeekSummary.of(
            known = listOf(known("word.Eabod"), known("word.{ll~ah"), known(ConceptIds.IKHFA), ConceptProgressEntity(ConceptIds.SUKUN, ConceptStatus.LEARNING, 1)),
            log = listOf(log(ReviewRating.GOOD), log(ReviewRating.AGAIN), log(ReviewRating.EASY)),
        )
        assertEquals(WeekSummary(newWords = 2, newConcepts = 1, reviews = 3, rightReviews = 2), week)
    }

    @Test
    fun aQuietWeekIsEmpty() {
        assertTrue(WeekSummary.of(emptyList(), emptyList()).isEmpty)
    }
}
