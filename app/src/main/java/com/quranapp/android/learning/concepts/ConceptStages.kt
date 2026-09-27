package com.quranapp.android.learning.concepts

import com.quranapp.android.learning.concepts.ConceptIds.DAGGER_ALIF
import com.quranapp.android.learning.concepts.ConceptIds.GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.HAMZAT_WASL
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_NO_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_SHAFAWI
import com.quranapp.android.learning.concepts.ConceptIds.IKHFA
import com.quranapp.android.learning.concepts.ConceptIds.IKHFA_SHAFAWI
import com.quranapp.android.learning.concepts.ConceptIds.IQLAB
import com.quranapp.android.learning.concepts.ConceptIds.IZHAR
import com.quranapp.android.learning.concepts.ConceptIds.IZHAR_SHAFAWI
import com.quranapp.android.learning.concepts.ConceptIds.LAM_OF_ALLAH
import com.quranapp.android.learning.concepts.ConceptIds.LAM_QAMARIYYA
import com.quranapp.android.learning.concepts.ConceptIds.LAM_SHAMSIYYA
import com.quranapp.android.learning.concepts.ConceptIds.MADD_LAZIM
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUNFASIL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUTTASIL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_SIGN
import com.quranapp.android.learning.concepts.ConceptIds.MEEM_SAKINAH
import com.quranapp.android.learning.concepts.ConceptIds.NOON_SAKINAH
import com.quranapp.android.learning.concepts.ConceptIds.QALQALAH
import com.quranapp.android.learning.concepts.ConceptIds.SAJDAH
import com.quranapp.android.learning.concepts.ConceptIds.SILENT_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.SMALL_MADD_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.STOP_SIGNS

/**
 * The stage that teaches each concept, from the "Stage" column of the design's concept
 * catalog. A stage's goals count only concepts up to that stage: reading Al-Fātiḥah smoothly
 * (stage 1) doesn't need the noon sākinah rules yet (stage 2), even though its text has them.
 * Concepts not listed are stage 0: the letters, vowel marks, where letters are made and the
 * heavy letters.
 */
object ConceptStages {
    private val STAGE_1 = setOf(
        DAGGER_ALIF, SMALL_MADD_LETTERS, HAMZAT_WASL, SILENT_LETTERS, STOP_SIGNS, SAJDAH,
        LAM_SHAMSIYYA, LAM_QAMARIYYA, LAM_OF_ALLAH, GHUNNAH, QALQALAH,
    )
    private val STAGE_2 = setOf(
        NOON_SAKINAH, IZHAR, IDGHAM_GHUNNAH, IDGHAM_NO_GHUNNAH, IQLAB, IKHFA,
        MEEM_SAKINAH, IZHAR_SHAFAWI, IDGHAM_SHAFAWI, IKHFA_SHAFAWI,
        MADD_SIGN, MADD_MUTTASIL, MADD_MUNFASIL, MADD_LAZIM,
    )

    fun of(conceptId: String): Int = when (conceptId) {
        in STAGE_2 -> 2
        in STAGE_1 -> 1
        else -> 0
    }
}
