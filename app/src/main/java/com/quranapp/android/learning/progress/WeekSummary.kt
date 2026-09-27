package com.quranapp.android.learning.progress

import com.quranapp.android.learning.words.WordItems

/**
 * The last seven days in a few calm numbers (decision 10): what was learned and how the
 * reviews went. No streaks or points; nothing is lost by missing a day.
 */
data class WeekSummary(
    val newWords: Int,
    val newConcepts: Int,
    val reviews: Int,
    val rightReviews: Int,
) {
    val isEmpty: Boolean get() = newWords == 0 && newConcepts == 0 && reviews == 0

    companion object {
        const val WEEK_MS = 7 * 86_400_000L

        /** [known] are items known since the week began; [log] the reviews answered since then. */
        fun of(known: List<ConceptProgressEntity>, log: List<ReviewLogEntity>): WeekSummary {
            val (words, concepts) = known.filter { it.status == ConceptStatus.KNOWN }.partition { WordItems.isWordItem(it.conceptId) }
            return WeekSummary(
                newWords = words.size,
                newConcepts = concepts.size,
                reviews = log.size,
                rightReviews = log.count { it.rating != ReviewRating.AGAIN },
            )
        }
    }
}
