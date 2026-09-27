package com.quranapp.android.learning.practice

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * FSRS-6, the Free Spaced Repetition Scheduler (decision 15), written from its published
 * description: https://github.com/open-spaced-repetition/awesome-fsrs/wiki/The-Algorithm
 * (the FSRS-6 and FSRS-5 sections, and FSRS-4.5 for the rest). No library: the official
 * Kotlin one pulls in a JSON library the app doesn't otherwise need.
 *
 * A memory has a **stability** S, the days until the chance of recalling it falls to 90%,
 * and a **difficulty** D from 1 to 10. Grades G are 1 again, 2 hard, 3 good, 4 easy.
 */
class Fsrs(private val w: DoubleArray = DEFAULT_WEIGHTS) {
    init {
        require(w.size == 21) { "FSRS-6 has 21 weights" }
    }

    /** How the forgetting curve bends; chosen so that R(S, S) is exactly 90%. */
    private val factor = 0.9.pow(-1 / w[20]) - 1

    /** Chance of recalling a memory of stability [s] after [days]. */
    fun retrievability(days: Double, s: Double): Double = (1 + factor * days / s).pow(-w[20])

    /** Days until the chance of recall falls to [retention]; equals [s] for 90%. */
    fun interval(s: Double, retention: Double = DESIRED_RETENTION): Double =
        s / factor * (retention.pow(-1 / w[20]) - 1)

    fun initialStability(g: Int): Double = max(w[g - 1], MIN_STABILITY)

    fun initialDifficulty(g: Int): Double = clampDifficulty(w[4] - exp(w[5] * (g - 1)) + 1)

    /** Difficulty after a review: moves with the grade, less so near 10, then back toward easy. */
    fun nextDifficulty(d: Double, g: Int): Double {
        val delta = -w[6] * (g - 3)
        val damped = d + delta * (10 - d) / 9
        return clampDifficulty(w[7] * initialDifficulty(4) + (1 - w[7]) * damped)
    }

    /** Stability after a successful review (hard, good or easy) [days] after the last one. */
    fun recallStability(d: Double, s: Double, r: Double, g: Int): Double {
        val hardPenalty = if (g == 2) w[15] else 1.0
        val easyBonus = if (g == 4) w[16] else 1.0
        val increase = exp(w[8]) * (11 - d) * s.pow(-w[9]) * (exp(w[10] * (1 - r)) - 1) * hardPenalty * easyBonus
        return s * (increase + 1)
    }

    /** Stability after forgetting. Never more than before: forgetting can't strengthen a memory. */
    fun forgetStability(d: Double, s: Double, r: Double): Double {
        val next = w[11] * d.pow(-w[12]) * ((s + 1).pow(w[13]) - 1) * exp(w[14] * (1 - r))
        return max(min(next, s), MIN_STABILITY)
    }

    /** Stability after a second review on the same day. Success never lowers it. */
    fun sameDayStability(s: Double, g: Int): Double {
        val next = s * exp(w[17] * (g - 3 + w[18])) * s.pow(-w[19])
        return max(if (g >= 2) max(next, s) else next, MIN_STABILITY)
    }

    private fun clampDifficulty(d: Double) = d.coerceIn(1.0, 10.0)

    companion object {
        /** Aim to review when the chance of recall has fallen to 90%. */
        const val DESIRED_RETENTION = 0.9
        private const val MIN_STABILITY = 0.01

        /** FSRS-6's default weights, fitted on millions of Anki reviews. */
        val DEFAULT_WEIGHTS = doubleArrayOf(
            0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194, 0.001, 1.8722, 0.1666, 0.796,
            1.4835, 0.0614, 0.2629, 1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542,
        )
    }
}
