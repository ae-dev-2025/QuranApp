package com.quranapp.android.learning.practice

import com.quranapp.android.learning.progress.ReviewRating
import com.quranapp.android.learning.progress.ReviewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The properties that define FSRS, checked with its default weights. */
class FsrsTest {
    private val fsrs = Fsrs()

    @Test
    fun recallIs90PercentWhenTheStabilityHasPassed() {
        assertEquals(0.9, fsrs.retrievability(days = 7.0, s = 7.0), 1e-9)
        assertEquals(7.0, fsrs.interval(s = 7.0), 1e-9)
        assertTrue(fsrs.retrievability(1.0, 7.0) > fsrs.retrievability(20.0, 7.0))
    }

    @Test
    fun firstGradeSetsTheStartingMemory() {
        assertEquals(2.3065, fsrs.initialStability(3), 1e-9) // good = w2
        assertTrue(fsrs.initialDifficulty(1) > fsrs.initialDifficulty(3))
        assertTrue(fsrs.initialDifficulty(4) >= 1.0)
    }

    @Test
    fun betterGradesGiveLongerStability() {
        val d = 5.0
        val s = 10.0
        val r = fsrs.retrievability(10.0, s)
        val hard = fsrs.recallStability(d, s, r, 2)
        val good = fsrs.recallStability(d, s, r, 3)
        val easy = fsrs.recallStability(d, s, r, 4)
        assertTrue(hard < good && good < easy)
        assertTrue("a success never weakens a memory", hard >= s)
    }

    @Test
    fun forgettingNeverStrengthensAMemory() {
        assertTrue(fsrs.forgetStability(d = 5.0, s = 40.0, r = 0.8) < 40.0)
        assertTrue(fsrs.forgetStability(d = 1.0, s = 0.5, r = 0.99) <= 0.5)
    }

    @Test
    fun difficultyRisesWhenForgottenAndStaysInRange() {
        assertTrue(fsrs.nextDifficulty(5.0, 1) > 5.0)
        assertTrue(fsrs.nextDifficulty(5.0, 4) < 5.0)
        assertTrue(fsrs.nextDifficulty(10.0, 1) <= 10.0)
        assertTrue(fsrs.nextDifficulty(1.0, 4) >= 1.0)
    }

    @Test
    fun aSameDaySuccessNeverLowersStability() {
        assertTrue(fsrs.sameDayStability(3.0, 2) >= 3.0)
        assertTrue(fsrs.sameDayStability(3.0, 3) >= 3.0)
    }
}

class ReviewSchedulerTest {
    private val scheduler = ReviewScheduler()
    private val day = ReviewScheduler.DAY_MS
    private val start = 1_000_000_000_000L

    @Test
    fun aFirstGoodAnswerSchedulesAReviewInDays() {
        val outcome = scheduler.review("word.Eabada", null, ReviewRating.GOOD, start)
        assertEquals(ReviewState.REVIEW, outcome.card.state)
        assertEquals(start + 2 * day, outcome.card.dueAt) // stability 2.3 days, rounded
        assertNull(outcome.log.stateBefore)
    }

    @Test
    fun aFirstWrongAnswerComesBackSoon() {
        val card = scheduler.review("word.Eabada", null, ReviewRating.AGAIN, start).card
        assertEquals(ReviewState.LEARNING, card.state)
        assertEquals(start + ReviewScheduler.RELEARN_MS, card.dueAt)
    }

    @Test
    fun intervalsGrowWithEachSuccess() {
        var card = scheduler.review("x", null, ReviewRating.GOOD, start).card
        var interval = card.dueAt - card.lastReviewAt
        repeat(4) {
            val next = scheduler.review("x", card, ReviewRating.GOOD, card.dueAt).card
            val nextInterval = next.dueAt - next.lastReviewAt
            assertTrue(nextInterval > interval)
            card = next
            interval = nextInterval
        }
        assertEquals(5, card.reps)
    }

    @Test
    fun forgettingALearnedItemCountsALapse() {
        val learned = scheduler.review("x", null, ReviewRating.GOOD, start).card
        val forgotten = scheduler.review("x", learned, ReviewRating.AGAIN, learned.dueAt).card
        assertEquals(ReviewState.RELEARNING, forgotten.state)
        assertEquals(1, forgotten.lapses)
        assertTrue(forgotten.stability < learned.stability)
        assertEquals(learned.dueAt + ReviewScheduler.RELEARN_MS, forgotten.dueAt)
    }
}
