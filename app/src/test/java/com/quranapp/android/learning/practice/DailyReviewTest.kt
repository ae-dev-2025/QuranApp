package com.quranapp.android.learning.practice

import com.quranapp.android.learning.progress.ReviewCardEntity
import com.quranapp.android.learning.progress.ReviewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.random.Random

class DailyReviewTest {
    private val now = 1_000_000_000L

    private fun card(itemId: String, dueAt: Long) = ReviewCardEntity(
        itemId = itemId,
        state = ReviewState.REVIEW,
        stability = 3.0,
        difficulty = 5.0,
        dueAt = dueAt,
        lastReviewAt = 0,
        reps = 1,
        lapses = 0,
    )

    @Test
    fun summarize_countsDueCardsAndFindsTheNextOne() {
        val cards = listOf(
            card("word.a", now - 10),
            card("word.b", now), // due exactly now counts as due
            card("word.c", now + 5_000),
            card("word.d", now + 1_000),
        )
        val summary = DailyReview.summarize(cards, now)
        assertEquals(2, summary.dueNow)
        assertEquals(now + 1_000, summary.nextDueAt)
        assertEquals(4, summary.total)
    }

    @Test
    fun summarize_withNoCards() {
        val summary = DailyReview.summarize(emptyList(), now)
        assertEquals(0, summary.dueNow)
        assertNull(summary.nextDueAt)
        assertEquals(0, summary.total)
    }

    @Test
    fun minutes_areForOneSessionAndAtLeastOne() {
        assertEquals(1, ReviewSummary(dueNow = 1, nextDueAt = null, total = 1).minutes)
        assertEquals(4, ReviewSummary(dueNow = 12, nextDueAt = null, total = 12).minutes) // 240 s
        // 500 due, but a session is 20 items: about 7 minutes, not 167.
        assertEquals(20, ReviewSummary(dueNow = 500, nextDueAt = null, total = 500).sessionSize)
        assertEquals(7, ReviewSummary(dueNow = 500, nextDueAt = null, total = 500).minutes)
    }

    @Test
    fun daysUntil_countsCalendarDaysNotHours() {
        val zone = ZoneId.of("Asia/Almaty")
        fun at(day: Int, hour: Int) = ZonedDateTime.of(2026, 9, day, hour, 0, 0, 0, zone).toInstant().toEpochMilli()
        val evening = at(27, 22)
        assertEquals(0, DailyReview.daysUntil(at(27, 23), evening, zone))
        assertEquals("two hours later, but tomorrow", 1, DailyReview.daysUntil(at(28, 0), evening, zone))
        assertEquals(1, DailyReview.daysUntil(at(28, 21), evening, zone))
        assertEquals(3, DailyReview.daysUntil(at(30, 8), evening, zone))
        assertEquals("already due", 0, DailyReview.daysUntil(at(26, 8), evening, zone))
    }

    @Test
    fun sessionItems_takesTheMostOverdueAndMixesThem() {
        // The DAO returns due cards most overdue first.
        val due = (0 until 30).map { card("item.$it", now - 1_000 + it) }
        val items = DailyReview.sessionItems(due, Random(1))
        assertEquals(DailyReview.SESSION_SIZE, items.size)
        assertEquals((0 until 20).map { "item.$it" }.toSet(), items.toSet())
    }
}
