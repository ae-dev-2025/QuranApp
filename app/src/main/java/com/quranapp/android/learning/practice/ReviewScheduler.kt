package com.quranapp.android.learning.practice

import com.quranapp.android.learning.progress.ReviewCardEntity
import com.quranapp.android.learning.progress.ReviewLogEntity
import com.quranapp.android.learning.progress.ReviewRating
import com.quranapp.android.learning.progress.ReviewState
import kotlin.math.roundToLong

/** A review's result: the card to save and its log entry. */
data class ReviewOutcome(val card: ReviewCardEntity, val log: ReviewLogEntity)

/**
 * Turns an answer into the item's next review (decision 15), using [Fsrs].
 *
 * - The first time an item is answered, its memory starts from the grade.
 * - A forgotten item comes back [RELEARN_MS] later, in the same session if it's still going.
 * - Otherwise it comes back when the chance of recall has fallen to 90%: whole days, at
 *   least one, at most a hundred years.
 */
class ReviewScheduler(private val fsrs: Fsrs = Fsrs()) {
    fun review(itemId: String, card: ReviewCardEntity?, rating: ReviewRating, now: Long): ReviewOutcome {
        val g = rating.ordinal + 1
        val next = if (card == null) first(itemId, g, now) else again(card, g, now)
        return ReviewOutcome(next, ReviewLogEntity(itemId = itemId, rating = rating, reviewedAt = now, stateBefore = card?.state))
    }

    private fun first(itemId: String, g: Int, now: Long): ReviewCardEntity {
        val s = fsrs.initialStability(g)
        val forgotten = g == 1
        return ReviewCardEntity(
            itemId = itemId,
            state = if (forgotten) ReviewState.LEARNING else ReviewState.REVIEW,
            stability = s,
            difficulty = fsrs.initialDifficulty(g),
            dueAt = if (forgotten) now + RELEARN_MS else now + days(s),
            lastReviewAt = now,
            reps = 1,
            lapses = 0,
        )
    }

    private fun again(card: ReviewCardEntity, g: Int, now: Long): ReviewCardEntity {
        val elapsedDays = (now - card.lastReviewAt).coerceAtLeast(0) / DAY_MS.toDouble()
        val d = fsrs.nextDifficulty(card.difficulty, g)
        val forgotten = g == 1
        val s = when {
            elapsedDays < 1 -> fsrs.sameDayStability(card.stability, g)
            forgotten -> fsrs.forgetStability(card.difficulty, card.stability, fsrs.retrievability(elapsedDays, card.stability))
            else -> fsrs.recallStability(card.difficulty, card.stability, fsrs.retrievability(elapsedDays, card.stability), g)
        }
        // A lapse is forgetting something that had been learned, not failing it while learning.
        val lapse = forgotten && card.state == ReviewState.REVIEW
        return card.copy(
            state = if (forgotten) {
                if (card.state == ReviewState.LEARNING) ReviewState.LEARNING else ReviewState.RELEARNING
            } else {
                ReviewState.REVIEW
            },
            stability = s,
            difficulty = d,
            dueAt = if (forgotten) now + RELEARN_MS else now + days(s),
            lastReviewAt = now,
            reps = card.reps + 1,
            lapses = card.lapses + if (lapse) 1 else 0,
        )
    }

    /** The interval for stability [s] in milliseconds, in whole days. */
    private fun days(s: Double): Long {
        val days = fsrs.interval(s).roundToLong().coerceIn(1, MAX_DAYS)
        return days * DAY_MS
    }

    companion object {
        const val DAY_MS = 24 * 60 * 60 * 1000L
        const val RELEARN_MS = 10 * 60 * 1000L
        private const val MAX_DAYS = 36_500L
    }
}
