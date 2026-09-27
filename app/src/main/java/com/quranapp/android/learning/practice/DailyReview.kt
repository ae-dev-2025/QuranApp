package com.quranapp.android.learning.practice

import com.quranapp.android.learning.progress.ReviewCardEntity
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.random.Random

/** What the Learn screen's "Today" card says. */
data class ReviewSummary(
    /** Items due now. */
    val dueNow: Int,
    /** When the next item not yet due will be, or null if nothing is waiting. */
    val nextDueAt: Long?,
    /** Every item that has a review card. */
    val total: Int,
) {
    /** How many items the next session asks about. */
    val sessionSize: Int get() = dueNow.coerceAtMost(DailyReview.SESSION_SIZE)

    /** A calm estimate for the next session, at least a minute. */
    val minutes: Int get() = ((sessionSize * DailyReview.SECONDS_PER_REVIEW + 59) / 60).coerceAtLeast(1)
}

/**
 * The daily review: items whose review is due, one question each (decision 15, FSRS).
 * Sessions are short, so a long break doesn't turn into one huge pile: the most overdue
 * items come first, and whatever is left waits for the next session.
 */
object DailyReview {
    /** At most this many items per session (about seven minutes). */
    const val SESSION_SIZE = 20

    /** A rough time per question, for the estimate on the card. */
    const val SECONDS_PER_REVIEW = 20

    /** New words a day, from the goal surah or the next unit (the design's default). */
    const val NEW_WORDS_PER_DAY = 10

    /** A new word is an introduction and a three-question check: about four steps. */
    private const val STEPS_PER_NEW_WORD = 4

    /** A calm estimate for a session of [reviews] and [newWords], at least a minute. */
    fun minutesFor(reviews: Int, newWords: Int): Int {
        val seconds = (reviews + newWords * STEPS_PER_NEW_WORD) * SECONDS_PER_REVIEW
        return ((seconds + 59) / 60).coerceAtLeast(1)
    }

    /** How many new words are still open today, after [startedToday] were introduced. */
    fun newWordsLeft(startedToday: Int, perDay: Int = NEW_WORDS_PER_DAY): Int = (perDay - startedToday).coerceAtLeast(0)

    /** Midnight at the start of [now]'s day where the learner lives. */
    fun startOfDay(now: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(now).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

    fun summarize(cards: List<ReviewCardEntity>, now: Long): ReviewSummary {
        val (due, waiting) = cards.partition { it.dueAt <= now }
        return ReviewSummary(
            dueNow = due.size,
            nextDueAt = waiting.minOfOrNull { it.dueAt },
            total = cards.size,
        )
    }

    /** Calendar days from [now] until [at] where the learner lives: 0 is later today, 1 tomorrow. */
    fun daysUntil(at: Long, now: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val day = Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
        return ChronoUnit.DAYS.between(today, day).coerceAtLeast(0)
    }

    /**
     * The item ids for one session from [dueCards], which come most overdue first. The
     * chosen items are shuffled so that words and rules are mixed rather than grouped.
     */
    fun sessionItems(dueCards: List<ReviewCardEntity>, random: Random = Random.Default): List<String> =
        dueCards.take(SESSION_SIZE).map { it.itemId }.shuffled(random)
}
