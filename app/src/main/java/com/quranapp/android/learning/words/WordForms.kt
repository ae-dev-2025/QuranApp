package com.quranapp.android.learning.words

import com.quranapp.android.learning.analysis.Arabic

/** Comparing how words are written, whatever their marks. */
object WordForms {
    /** The letters alone: no vowels or other marks, no stretching line, and ٱ as ا. */
    fun skeleton(word: String): String = buildString {
        for (c in word) {
            when {
                Arabic.isMark(c) || c == Arabic.TATWEEL -> Unit
                c == Arabic.ALIF_WASLA -> append(Arabic.ALIF)
                else -> append(c)
            }
        }
    }

    /**
     * Whether a word of the Quran is written like a dictionary form, marks aside: قَالَ in
     * 2:30 is, يَقُولُ in 2:8 isn't. Used to let learners hear the form they're shown.
     */
    fun sameSkeleton(word: String, headword: String): Boolean = skeleton(word) == skeleton(headword)
}
