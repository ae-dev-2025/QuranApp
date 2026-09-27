package com.quranapp.android.learning.progress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Where a card is in the FSRS cycle. Stored by name, so never rename an entry. */
enum class ReviewState {
    /** Just checked for the first time; short steps until it sticks. */
    LEARNING,

    /** Remembered; comes back after a growing interval. */
    REVIEW,

    /** Forgotten in a review; short steps again. */
    RELEARNING,
}

/** How a review went, graded by the app from the answer (decision 8). Stored by name. */
enum class ReviewRating { AGAIN, HARD, GOOD, EASY }

/**
 * The memory state of one item the learner has proved they know (decision 7: "checked"):
 * a letter, a word or a concept, by its item id (`word.Eabada`, `tajweed.ikhfa`).
 *
 * Stability is how many days until the chance of remembering drops to 90%; difficulty is
 * 1 (easy) to 10 (hard). Both come from the FSRS scheduler.
 */
@Entity(tableName = "review_cards", indices = [Index(value = ["due_at"])])
data class ReviewCardEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "state")
    val state: ReviewState,
    @ColumnInfo(name = "stability")
    val stability: Double,
    @ColumnInfo(name = "difficulty")
    val difficulty: Double,
    /** When the item should be reviewed next, in milliseconds since 1970. */
    @ColumnInfo(name = "due_at")
    val dueAt: Long,
    @ColumnInfo(name = "last_review_at")
    val lastReviewAt: Long,
    @ColumnInfo(name = "reps")
    val reps: Int,
    /** How many times it was forgotten after being learned. */
    @ColumnInfo(name = "lapses")
    val lapses: Int,
)

/**
 * One answered review. Kept so that FSRS can later fit its schedule to this learner's
 * memory. Only the item, the grade and the time are stored: nothing about the person.
 */
@Entity(tableName = "review_log", indices = [Index(value = ["item_id"])])
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "rating")
    val rating: ReviewRating,
    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long,
    /** The card's state before this review; null for the first one. */
    @ColumnInfo(name = "state_before")
    val stateBefore: ReviewState?,
)
