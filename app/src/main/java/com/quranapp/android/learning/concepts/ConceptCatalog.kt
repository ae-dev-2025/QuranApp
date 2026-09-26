package com.quranapp.android.learning.concepts

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds.DAGGER_ALIF
import com.quranapp.android.learning.concepts.ConceptIds.GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.HAMZA
import com.quranapp.android.learning.concepts.ConceptIds.HAMZAT_WASL
import com.quranapp.android.learning.concepts.ConceptIds.HEAVY_LETTERS
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
import com.quranapp.android.learning.concepts.ConceptIds.LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.LONG_VOWELS
import com.quranapp.android.learning.concepts.ConceptIds.MADD_LAZIM
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUNFASIL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUTTASIL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_SIGN
import com.quranapp.android.learning.concepts.ConceptIds.MEEM_SAKINAH
import com.quranapp.android.learning.concepts.ConceptIds.NOON_SAKINAH
import com.quranapp.android.learning.concepts.ConceptIds.QALQALAH
import com.quranapp.android.learning.concepts.ConceptIds.SAJDAH
import com.quranapp.android.learning.concepts.ConceptIds.SHADDA
import com.quranapp.android.learning.concepts.ConceptIds.SHORT_VOWELS
import com.quranapp.android.learning.concepts.ConceptIds.SILENT_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.SMALL_MADD_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.STOP_SIGNS
import com.quranapp.android.learning.concepts.ConceptIds.SUKUN
import com.quranapp.android.learning.concepts.ConceptIds.TANWEEN
import com.quranapp.android.learning.concepts.ConceptIds.TA_MARBUTA

/**
 * Every concept the app can teach, listed in teaching order.
 *
 * Rule: a concept must appear *after* all of its prerequisites. `ConceptCatalogTest` checks
 * this, which also guarantees there are no cycles ("A needs B needs A").
 */
object ConceptCatalog {
    val all: List<Concept> = listOf(
        // ---- Reading ----
        reading(
            LETTERS, R.string.concept_letters_title, R.string.concept_letters_summary,
        ),
        reading(
            SHORT_VOWELS, R.string.concept_short_vowels_title, R.string.concept_short_vowels_summary,
            LETTERS,
        ),
        reading(
            SUKUN, R.string.concept_sukun_title, R.string.concept_sukun_summary,
            SHORT_VOWELS,
        ),
        reading(
            LONG_VOWELS, R.string.concept_long_vowels_title, R.string.concept_long_vowels_summary,
            SHORT_VOWELS,
        ),
        reading(
            TANWEEN, R.string.concept_tanween_title, R.string.concept_tanween_summary,
            SHORT_VOWELS,
        ),
        reading(
            HAMZA, R.string.concept_hamza_title, R.string.concept_hamza_summary,
            SHORT_VOWELS,
        ),
        reading(
            TA_MARBUTA, R.string.concept_ta_marbuta_title, R.string.concept_ta_marbuta_summary,
            SHORT_VOWELS,
        ),
        reading(
            SHADDA, R.string.concept_shadda_title, R.string.concept_shadda_summary,
            SUKUN,
        ),
        reading(
            DAGGER_ALIF, R.string.concept_dagger_alif_title, R.string.concept_dagger_alif_summary,
            LONG_VOWELS,
        ),
        reading(
            SMALL_MADD_LETTERS,
            R.string.concept_small_madd_letters_title,
            R.string.concept_small_madd_letters_summary,
            LONG_VOWELS,
        ),
        reading(
            HAMZAT_WASL, R.string.concept_hamzat_wasl_title, R.string.concept_hamzat_wasl_summary,
            HAMZA, SUKUN,
        ),
        reading(
            SILENT_LETTERS,
            R.string.concept_silent_letters_title,
            R.string.concept_silent_letters_summary,
            LONG_VOWELS,
        ),
        reading(
            STOP_SIGNS, R.string.concept_stop_signs_title, R.string.concept_stop_signs_summary,
            SUKUN, TANWEEN,
        ),
        reading(
            SAJDAH, R.string.concept_sajdah_title, R.string.concept_sajdah_summary,
            LETTERS,
        ),

        // ---- Tajweed ----
        tajweed(
            HEAVY_LETTERS,
            R.string.concept_heavy_letters_title,
            R.string.concept_heavy_letters_summary,
            SHORT_VOWELS,
        ),
        tajweed(
            LAM_SHAMSIYYA,
            R.string.concept_lam_shamsiyya_title,
            R.string.concept_lam_shamsiyya_summary,
            HAMZAT_WASL, SHADDA,
        ),
        tajweed(
            LAM_QAMARIYYA,
            R.string.concept_lam_qamariyya_title,
            R.string.concept_lam_qamariyya_summary,
            HAMZAT_WASL,
        ),
        tajweed(
            LAM_OF_ALLAH, R.string.concept_lam_of_allah_title, R.string.concept_lam_of_allah_summary,
            LAM_SHAMSIYYA, HEAVY_LETTERS,
        ),
        tajweed(
            GHUNNAH, R.string.concept_ghunnah_title, R.string.concept_ghunnah_summary,
            SHADDA,
        ),
        tajweed(
            QALQALAH, R.string.concept_qalqalah_title, R.string.concept_qalqalah_summary,
            SUKUN,
        ),

        // Noon sakinah & tanween: an umbrella concept and its four rules
        tajweed(
            NOON_SAKINAH, R.string.concept_noon_sakinah_title, R.string.concept_noon_sakinah_summary,
            SUKUN, TANWEEN,
        ),
        tajweed(
            IZHAR, R.string.concept_izhar_title, R.string.concept_izhar_summary,
            NOON_SAKINAH,
        ),
        tajweed(
            IDGHAM_GHUNNAH,
            R.string.concept_idgham_ghunnah_title,
            R.string.concept_idgham_ghunnah_summary,
            NOON_SAKINAH, GHUNNAH,
        ),
        tajweed(
            IDGHAM_NO_GHUNNAH,
            R.string.concept_idgham_no_ghunnah_title,
            R.string.concept_idgham_no_ghunnah_summary,
            NOON_SAKINAH, SHADDA,
        ),
        tajweed(
            IQLAB, R.string.concept_iqlab_title, R.string.concept_iqlab_summary,
            NOON_SAKINAH,
        ),
        tajweed(
            IKHFA, R.string.concept_ikhfa_title, R.string.concept_ikhfa_summary,
            NOON_SAKINAH,
        ),

        // Meem sakinah: an umbrella concept and its three rules
        tajweed(
            MEEM_SAKINAH, R.string.concept_meem_sakinah_title, R.string.concept_meem_sakinah_summary,
            SUKUN,
        ),
        tajweed(
            IZHAR_SHAFAWI,
            R.string.concept_izhar_shafawi_title,
            R.string.concept_izhar_shafawi_summary,
            MEEM_SAKINAH,
        ),
        tajweed(
            IDGHAM_SHAFAWI,
            R.string.concept_idgham_shafawi_title,
            R.string.concept_idgham_shafawi_summary,
            MEEM_SAKINAH, GHUNNAH,
        ),
        tajweed(
            IKHFA_SHAFAWI,
            R.string.concept_ikhfa_shafawi_title,
            R.string.concept_ikhfa_shafawi_summary,
            MEEM_SAKINAH,
        ),

        // Madd longer than two counts
        tajweed(
            MADD_SIGN, R.string.concept_madd_sign_title, R.string.concept_madd_sign_summary,
            LONG_VOWELS,
        ),
        tajweed(
            MADD_MUTTASIL,
            R.string.concept_madd_muttasil_title,
            R.string.concept_madd_muttasil_summary,
            MADD_SIGN, HAMZA,
        ),
        tajweed(
            MADD_MUNFASIL,
            R.string.concept_madd_munfasil_title,
            R.string.concept_madd_munfasil_summary,
            MADD_SIGN, HAMZA,
        ),
        tajweed(
            MADD_LAZIM, R.string.concept_madd_lazim_title, R.string.concept_madd_lazim_summary,
            MADD_SIGN, SHADDA,
        ),
    )

    /**
     * Concepts that group several rules and never occur in the text themselves. Their page
     * shows an overview of those rules instead of a lesson.
     */
    val umbrellaIds: Set<String> = setOf(NOON_SAKINAH, MEEM_SAKINAH)

    private val byId: Map<String, Concept> = all.associateBy { it.id }

    /** Looks up a concept by its ID; `null` if the ID is unknown. */
    operator fun get(id: String): Concept? = byId[id]
}

private fun reading(
    id: String,
    @StringRes title: Int,
    @StringRes summary: Int,
    vararg prerequisites: String,
) = Concept(id, Track.READING, title, summary, prerequisites.toList())

private fun tajweed(
    id: String,
    @StringRes title: Int,
    @StringRes summary: Int,
    vararg prerequisites: String,
) = Concept(id, Track.TAJWEED, title, summary, prerequisites.toList())
