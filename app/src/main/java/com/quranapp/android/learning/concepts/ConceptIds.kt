package com.quranapp.android.learning.concepts

/**
 * Stable IDs of all concepts. Using these constants instead of raw strings lets the
 * compiler catch typos.
 *
 * The prefix before the dot is the track; never rename an ID after it has been released.
 */
object ConceptIds {
    // Reading
    const val LETTERS = "reading.letters"
    const val SHORT_VOWELS = "reading.short_vowels"
    const val SUKUN = "reading.sukun"
    const val LONG_VOWELS = "reading.long_vowels"
    const val TANWEEN = "reading.tanween"
    const val HAMZA = "reading.hamza"
    const val TA_MARBUTA = "reading.ta_marbuta"
    const val SHADDA = "reading.shadda"
    const val DAGGER_ALIF = "reading.dagger_alif"
    const val SMALL_MADD_LETTERS = "reading.small_madd_letters"
    const val HAMZAT_WASL = "reading.hamzat_wasl"
    const val SILENT_LETTERS = "reading.silent_letters"
    const val STOP_SIGNS = "reading.stop_signs"
    const val SAJDAH = "reading.sajdah"
    const val STOPPING = "reading.stopping"

    // Tajweed: where letters are made (makhārij)
    const val MAKHRAJ_JAWF = "tajweed.makhraj_jawf"
    const val MAKHRAJ_THROAT = "tajweed.makhraj_throat"
    const val MAKHRAJ_TONGUE = "tajweed.makhraj_tongue"
    const val MAKHRAJ_LIPS = "tajweed.makhraj_lips"
    const val MAKHRAJ_NOSE = "tajweed.makhraj_nose"

    // Tajweed: letter qualities (ṣifāt)
    const val SIFA_HAMS = "tajweed.sifa_hams"
    const val SIFA_SHIDDA = "tajweed.sifa_shidda"
    const val SIFA_ISTILA = "tajweed.sifa_istila"
    const val SIFA_ITBAQ = "tajweed.sifa_itbaq"
    const val SIFA_SAFIR = "tajweed.sifa_safir"
    const val SIFA_TAKRIR = "tajweed.sifa_takrir"
    const val SIFA_TAFASHSHI = "tajweed.sifa_tafashshi"

    // Tajweed
    const val HEAVY_LETTERS = "tajweed.heavy_letters"
    const val LAM_SHAMSIYYA = "tajweed.lam_shamsiyya"
    const val LAM_QAMARIYYA = "tajweed.lam_qamariyya"
    const val LAM_OF_ALLAH = "tajweed.lam_of_allah"
    const val GHUNNAH = "tajweed.ghunnah"
    const val QALQALAH = "tajweed.qalqalah"

    // Rāʾ: an umbrella concept and its two rules
    const val RA = "tajweed.ra"
    const val RA_HEAVY = "tajweed.ra_heavy"
    const val RA_LIGHT = "tajweed.ra_light"

    // Two letters that meet, and lām sākinah outside ال
    const val IDGHAM_MITHLAYN = "tajweed.idgham_mithlayn"
    const val IDGHAM_MUTAJANISAYN = "tajweed.idgham_mutajanisayn"
    const val IDGHAM_MUTAQARIBAYN = "tajweed.idgham_mutaqaribayn"
    const val LAM_SAKINAH = "tajweed.lam_sakinah"
    const val TANWEEN_BEFORE_WASL = "tajweed.tanween_before_wasl"

    // Madds of stage 3
    const val MADD_ARID = "tajweed.madd_arid"
    const val MADD_LEEN = "tajweed.madd_leen"
    const val MADD_IWAD = "tajweed.madd_iwad"
    const val MADD_BADAL = "tajweed.madd_badal"
    const val MADD_SILA = "tajweed.madd_sila"
    const val NOON_SAKINAH = "tajweed.noon_sakinah"
    const val IZHAR = "tajweed.izhar"
    const val IDGHAM_GHUNNAH = "tajweed.idgham_ghunnah"
    const val IDGHAM_NO_GHUNNAH = "tajweed.idgham_no_ghunnah"
    const val IQLAB = "tajweed.iqlab"
    const val IKHFA = "tajweed.ikhfa"
    const val MEEM_SAKINAH = "tajweed.meem_sakinah"
    const val IZHAR_SHAFAWI = "tajweed.izhar_shafawi"
    const val IDGHAM_SHAFAWI = "tajweed.idgham_shafawi"
    const val IKHFA_SHAFAWI = "tajweed.ikhfa_shafawi"
    const val MADD_SIGN = "tajweed.madd_sign"
    const val MADD_MUTTASIL = "tajweed.madd_muttasil"
    const val MADD_MUNFASIL = "tajweed.madd_munfasil"
    const val MADD_LAZIM = "tajweed.madd_lazim"
}
