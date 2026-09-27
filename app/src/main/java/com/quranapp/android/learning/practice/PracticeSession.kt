package com.quranapp.android.learning.practice

import com.quranapp.android.learning.progress.ReviewRating

/** An item to practise and the questions about it. */
data class PracticeItem(val itemId: String, val questions: List<Question>)

/** How an item went once all its questions were answered. */
data class ItemResult(val itemId: String, val rating: ReviewRating, val right: Int, val total: Int)

/**
 * A run of questions, graded as it goes. Pure Kotlin: the screen asks it for the current
 * question and tells it whether the answer was right.
 *
 * Each item is graded once, when its last question has been answered (decision 8):
 * - a **check** (decision 7) asks three questions: all right, or three of four, passes;
 * - a **review** asks one question: right passes.
 * Passing is "good" for the scheduler, failing is "again".
 */
class PracticeSession(items: List<PracticeItem>) {
    private val questions: List<Question> = items.flatMap { it.questions }
    private val questionCount: Map<String, Int> = items.associate { it.itemId to it.questions.size }
    private val answers = mutableMapOf<String, MutableList<Boolean>>()

    var index: Int = 0
        private set

    val size: Int get() = questions.size
    val current: Question? get() = questions.getOrNull(index)
    val isFinished: Boolean get() = index >= questions.size

    /** How many answers were right so far. */
    val rightCount: Int get() = answers.values.sumOf { list -> list.count { it } }

    /**
     * Records the answer to the current question and moves on. Returns the item's result
     * when this was its last question, so the caller can schedule its next review.
     */
    fun answer(right: Boolean): ItemResult? {
        val question = checkNotNull(current) { "the session is finished" }
        val itemAnswers = answers.getOrPut(question.itemId) { mutableListOf() }
        itemAnswers += right
        index++
        val expected = questionCount.getValue(question.itemId)
        if (itemAnswers.size < expected) return null
        val rightAnswers = itemAnswers.count { it }
        val rating = if (passes(rightAnswers, expected)) ReviewRating.GOOD else ReviewRating.AGAIN
        return ItemResult(question.itemId, rating, rightAnswers, expected)
    }

    companion object {
        /** All right, or at most one wrong out of four or more. */
        fun passes(right: Int, total: Int): Boolean = right == total || (total >= 4 && right >= total - 1)
    }
}
