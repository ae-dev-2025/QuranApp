package com.quranapp.android.learning.letters

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds

/** One quality of a letter and the concept that teaches it. */
data class Quality(@StringRes val labelRes: Int, val conceptId: String)

/**
 * The qualities (ṣifāt) of each letter, from the classical lists. Opposite pairs are shown
 * by their marked side's letters: a letter that isn't whispered is voiced, and so on.
 */
object LetterQualities {
    val WHISPERED = setOf('ف', 'ح', 'ث', 'ه', 'ش', 'خ', 'ص', 'س', 'ك', 'ت') // فَحَثَّهُ شَخۡصٌ سَكَتَ
    val STRONG = setOf('ء', 'ج', 'د', 'ق', 'ط', 'ب', 'ك', 'ت') // أَجِدُ قَطٍ بَكَتۡ
    val MIDDLE = setOf('ل', 'ن', 'ع', 'م', 'ر') // لِنۡ عُمَرۡ
    val RAISED = setOf('خ', 'ص', 'ض', 'غ', 'ط', 'ق', 'ظ') // خُصَّ ضَغۡطٍ قِظۡ
    val CLOSED = setOf('ص', 'ض', 'ط', 'ظ')
    val WHISTLING = setOf('ص', 'س', 'ز')
    val LEANING = setOf('ل', 'ر')
    const val ROLLING = 'ر'
    const val SPREADING = 'ش'
    const val LENGTHENING = 'ض'
    val BOUNCING = setOf('ق', 'ط', 'ب', 'ج', 'د') // قُطۡبُ جَدٍّ

    fun of(letter: Letter): List<Quality> {
        val c = letter.char
        return buildList {
            add(if (c in WHISPERED) Quality(R.string.learning_sifa_whispered, ConceptIds.SIFA_HAMS) else Quality(R.string.learning_sifa_voiced, ConceptIds.SIFA_HAMS))
            add(
                when (c) {
                    in STRONG -> Quality(R.string.learning_sifa_strong, ConceptIds.SIFA_SHIDDA)
                    in MIDDLE -> Quality(R.string.learning_sifa_middle, ConceptIds.SIFA_SHIDDA)
                    else -> Quality(R.string.learning_sifa_soft, ConceptIds.SIFA_SHIDDA)
                },
            )
            add(if (c in RAISED) Quality(R.string.learning_sifa_raised, ConceptIds.SIFA_ISTILA) else Quality(R.string.learning_sifa_lowered, ConceptIds.SIFA_ISTILA))
            add(if (c in CLOSED) Quality(R.string.learning_sifa_closed, ConceptIds.SIFA_ITBAQ) else Quality(R.string.learning_sifa_open, ConceptIds.SIFA_ITBAQ))
            if (c in WHISTLING) add(Quality(R.string.learning_sifa_whistling, ConceptIds.SIFA_SAFIR))
            if (c == ROLLING) add(Quality(R.string.learning_sifa_rolling, ConceptIds.SIFA_TAKRIR))
            if (c in LEANING) add(Quality(R.string.learning_sifa_leaning, ConceptIds.SIFA_TAKRIR))
            if (c == SPREADING) add(Quality(R.string.learning_sifa_spreading, ConceptIds.SIFA_TAFASHSHI))
            if (c == LENGTHENING) add(Quality(R.string.learning_sifa_lengthening, ConceptIds.SIFA_TAFASHSHI))
            if (c in BOUNCING) add(Quality(R.string.learning_sifa_bouncing, ConceptIds.QALQALAH))
        }
    }
}
