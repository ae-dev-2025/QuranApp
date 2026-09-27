package com.quranapp.android.learning.lessons

import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds

/**
 * Every written lesson. A concept without one still has its page (explanation and examples).
 *
 * Key examples come from Al-Fātiḥah and Juz ʿAmma where possible, and were checked with the
 * analyzer to contain the concept at exactly those words.
 */
object LessonCatalog {
    val all: List<Lesson> = listOf(
        // ---- Reading ----
        Lesson(
            conceptId = ConceptIds.LETTERS,
            keyExample = KeyExample(1, 1, 0..0, R.string.lesson_letters_say),
            spotIt = R.array.lesson_letters_spot,
            sayIt = R.array.lesson_letters_how,
        ),
        Lesson(
            conceptId = ConceptIds.SHORT_VOWELS,
            keyExample = KeyExample(80, 17, 0..0, R.string.lesson_short_vowels_say),
            spotIt = R.array.lesson_short_vowels_spot,
            sayIt = R.array.lesson_short_vowels_how,
            confusedWith = ConfusedWith(ConceptIds.TANWEEN, R.string.lesson_short_vowels_confused),
        ),
        Lesson(
            conceptId = ConceptIds.SUKUN,
            keyExample = KeyExample(
                1, 5, 1..1, R.string.lesson_sukun_say, R.string.lesson_sukun_not_say,
            ),
            spotIt = R.array.lesson_sukun_spot,
            sayIt = R.array.lesson_sukun_how,
            confusedWith = ConfusedWith(ConceptIds.SILENT_LETTERS, R.string.lesson_sukun_confused),
        ),
        Lesson(
            conceptId = ConceptIds.LONG_VOWELS,
            keyExample = KeyExample(
                78, 24, 2..2, R.string.lesson_long_vowels_say, R.string.lesson_long_vowels_not_say,
            ),
            spotIt = R.array.lesson_long_vowels_spot,
            sayIt = R.array.lesson_long_vowels_how,
            confusedWith = ConfusedWith(ConceptIds.MADD_SIGN, R.string.lesson_long_vowels_confused),
        ),
        Lesson(
            conceptId = ConceptIds.TANWEEN,
            keyExample = KeyExample(
                101, 11, 0..1, R.string.lesson_tanween_say, R.string.lesson_tanween_not_say,
            ),
            spotIt = R.array.lesson_tanween_spot,
            sayIt = R.array.lesson_tanween_how,
            confusedWith = ConfusedWith(ConceptIds.SHORT_VOWELS, R.string.lesson_tanween_confused),
        ),
        Lesson(
            conceptId = ConceptIds.HAMZA,
            keyExample = KeyExample(
                78, 2, 1..1, R.string.lesson_hamza_say, R.string.lesson_hamza_not_say,
            ),
            spotIt = R.array.lesson_hamza_spot,
            sayIt = R.array.lesson_hamza_how,
            confusedWith = ConfusedWith(ConceptIds.HAMZAT_WASL, R.string.lesson_hamza_confused),
        ),
        Lesson(
            conceptId = ConceptIds.TA_MARBUTA,
            keyExample = KeyExample(79, 6, 2..2, R.string.lesson_ta_marbuta_say),
            spotIt = R.array.lesson_ta_marbuta_spot,
            sayIt = R.array.lesson_ta_marbuta_how,
        ),
        Lesson(
            conceptId = ConceptIds.SHADDA,
            keyExample = KeyExample(
                1, 2, 2..2, R.string.lesson_shadda_say, R.string.lesson_shadda_not_say,
            ),
            spotIt = R.array.lesson_shadda_spot,
            sayIt = R.array.lesson_shadda_how,
            confusedWith = ConfusedWith(ConceptIds.SUKUN, R.string.lesson_shadda_confused),
        ),
        Lesson(
            conceptId = ConceptIds.DAGGER_ALIF,
            keyExample = KeyExample(
                1, 4, 0..0, R.string.lesson_dagger_alif_say, R.string.lesson_dagger_alif_not_say,
            ),
            spotIt = R.array.lesson_dagger_alif_spot,
            sayIt = R.array.lesson_dagger_alif_how,
            confusedWith = ConfusedWith(
                ConceptIds.LONG_VOWELS, R.string.lesson_dagger_alif_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.SMALL_MADD_LETTERS,
            keyExample = KeyExample(
                78, 15, 1..1, R.string.lesson_small_madd_say, R.string.lesson_small_madd_not_say,
            ),
            spotIt = R.array.lesson_small_madd_spot,
            sayIt = R.array.lesson_small_madd_how,
            confusedWith = ConfusedWith(
                ConceptIds.LONG_VOWELS, R.string.lesson_small_madd_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.HAMZAT_WASL,
            keyExample = KeyExample(1, 6, 0..0, R.string.lesson_hamzat_wasl_say),
            spotIt = R.array.lesson_hamzat_wasl_spot,
            sayIt = R.array.lesson_hamzat_wasl_how,
            confusedWith = ConfusedWith(ConceptIds.HAMZA, R.string.lesson_hamzat_wasl_confused),
        ),
        Lesson(
            conceptId = ConceptIds.SILENT_LETTERS,
            keyExample = KeyExample(
                79, 12, 0..0, R.string.lesson_silent_say, R.string.lesson_silent_not_say,
            ),
            spotIt = R.array.lesson_silent_spot,
            sayIt = R.array.lesson_silent_how,
            confusedWith = ConfusedWith(ConceptIds.SUKUN, R.string.lesson_silent_confused),
        ),
        Lesson(
            conceptId = ConceptIds.STOP_SIGNS,
            keyExample = KeyExample(78, 37, 5..6, R.string.lesson_stop_signs_say),
            spotIt = R.array.lesson_stop_signs_spot,
            sayIt = R.array.lesson_stop_signs_how,
        ),
        Lesson(
            conceptId = ConceptIds.SAJDAH,
            keyExample = KeyExample(84, 21, 4..5, R.string.lesson_sajdah_say),
            spotIt = R.array.lesson_sajdah_spot,
            sayIt = R.array.lesson_sajdah_how,
            confusedWith = ConfusedWith(ConceptIds.STOP_SIGNS, R.string.lesson_sajdah_confused),
        ),

        // ---- Tajweed: noon sakinah and tanween ----
        Lesson(
            conceptId = ConceptIds.IZHAR,
            keyExample = KeyExample(1, 7, 2..2, R.string.lesson_izhar_say),
            spotIt = R.array.lesson_izhar_spot,
            sayIt = R.array.lesson_izhar_how,
            confusedWith = ConfusedWith(ConceptIds.IKHFA, R.string.lesson_izhar_confused),
        ),
        Lesson(
            conceptId = ConceptIds.IDGHAM_GHUNNAH,
            keyExample = KeyExample(
                78, 13, 1..2, R.string.lesson_idgham_ghunnah_say,
                R.string.lesson_idgham_ghunnah_not_say,
            ),
            spotIt = R.array.lesson_idgham_ghunnah_spot,
            sayIt = R.array.lesson_idgham_ghunnah_how,
            confusedWith = ConfusedWith(
                ConceptIds.IDGHAM_NO_GHUNNAH, R.string.lesson_idgham_ghunnah_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.IDGHAM_NO_GHUNNAH,
            keyExample = KeyExample(
                78, 36, 1..2, R.string.lesson_idgham_no_ghunnah_say,
                R.string.lesson_idgham_no_ghunnah_not_say,
            ),
            spotIt = R.array.lesson_idgham_no_ghunnah_spot,
            sayIt = R.array.lesson_idgham_no_ghunnah_how,
            confusedWith = ConfusedWith(
                ConceptIds.IDGHAM_GHUNNAH, R.string.lesson_idgham_no_ghunnah_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.IQLAB,
            keyExample = KeyExample(
                80, 27, 0..0, R.string.lesson_iqlab_say, R.string.lesson_iqlab_not_say,
            ),
            spotIt = R.array.lesson_iqlab_spot,
            sayIt = R.array.lesson_iqlab_how,
            confusedWith = ConfusedWith(ConceptIds.IKHFA_SHAFAWI, R.string.lesson_iqlab_confused),
        ),
        Lesson(
            conceptId = ConceptIds.IKHFA,
            keyExample = KeyExample(
                113, 3, 0..1, R.string.lesson_ikhfa_say, R.string.lesson_ikhfa_not_say,
            ),
            spotIt = R.array.lesson_ikhfa_spot,
            sayIt = R.array.lesson_ikhfa_how,
            confusedWith = ConfusedWith(ConceptIds.IZHAR, R.string.lesson_ikhfa_confused),
        ),

        // ---- Tajweed: meem sakinah ----
        Lesson(
            conceptId = ConceptIds.IZHAR_SHAFAWI,
            keyExample = KeyExample(112, 3, 0..1, R.string.lesson_izhar_shafawi_say),
            spotIt = R.array.lesson_izhar_shafawi_spot,
            sayIt = R.array.lesson_izhar_shafawi_how,
            confusedWith = ConfusedWith(ConceptIds.IZHAR, R.string.lesson_izhar_shafawi_confused),
        ),
        Lesson(
            conceptId = ConceptIds.IDGHAM_SHAFAWI,
            keyExample = KeyExample(97, 4, 5..6, R.string.lesson_idgham_shafawi_say),
            spotIt = R.array.lesson_idgham_shafawi_spot,
            sayIt = R.array.lesson_idgham_shafawi_how,
            confusedWith = ConfusedWith(
                ConceptIds.IDGHAM_GHUNNAH, R.string.lesson_idgham_shafawi_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.IKHFA_SHAFAWI,
            keyExample = KeyExample(
                84, 24, 0..1, R.string.lesson_ikhfa_shafawi_say,
                R.string.lesson_ikhfa_shafawi_not_say,
            ),
            spotIt = R.array.lesson_ikhfa_shafawi_spot,
            sayIt = R.array.lesson_ikhfa_shafawi_how,
            confusedWith = ConfusedWith(ConceptIds.IQLAB, R.string.lesson_ikhfa_shafawi_confused),
        ),

        // ---- Tajweed: where letters are made ----
        Lesson(
            conceptId = ConceptIds.MAKHRAJ_JAWF,
            keyExample = KeyExample(1, 2, 3..3, R.string.lesson_makhraj_jawf_say, R.string.lesson_makhraj_jawf_not_say),
            spotIt = R.array.lesson_makhraj_jawf_spot,
            sayIt = R.array.lesson_makhraj_jawf_how,
        ),
        Lesson(
            conceptId = ConceptIds.MAKHRAJ_THROAT,
            keyExample = KeyExample(1, 2, 0..0, R.string.lesson_makhraj_throat_say, R.string.lesson_makhraj_throat_not_say),
            spotIt = R.array.lesson_makhraj_throat_spot,
            sayIt = R.array.lesson_makhraj_throat_how,
        ),
        Lesson(
            conceptId = ConceptIds.MAKHRAJ_TONGUE,
            keyExample = KeyExample(112, 1, 0..0, R.string.lesson_makhraj_tongue_say, R.string.lesson_makhraj_tongue_not_say),
            spotIt = R.array.lesson_makhraj_tongue_spot,
            sayIt = R.array.lesson_makhraj_tongue_how,
        ),
        Lesson(
            conceptId = ConceptIds.MAKHRAJ_LIPS,
            keyExample = KeyExample(1, 1, 0..0, R.string.lesson_makhraj_lips_say),
            spotIt = R.array.lesson_makhraj_lips_spot,
            sayIt = R.array.lesson_makhraj_lips_how,
        ),
        Lesson(
            conceptId = ConceptIds.MAKHRAJ_NOSE,
            keyExample = KeyExample(78, 1, 0..0, R.string.lesson_makhraj_nose_say, R.string.lesson_makhraj_nose_not_say),
            spotIt = R.array.lesson_makhraj_nose_spot,
            sayIt = R.array.lesson_makhraj_nose_how,
            confusedWith = ConfusedWith(ConceptIds.GHUNNAH, R.string.lesson_makhraj_nose_confused),
        ),

        // ---- Tajweed: letter qualities ----
        Lesson(
            conceptId = ConceptIds.SIFA_HAMS,
            keyExample = KeyExample(1, 6, 2..2, R.string.lesson_sifa_hams_say),
            spotIt = R.array.lesson_sifa_hams_spot,
            sayIt = R.array.lesson_sifa_hams_how,
        ),
        Lesson(
            conceptId = ConceptIds.SIFA_SHIDDA,
            keyExample = KeyExample(112, 3, 1..1, R.string.lesson_sifa_shidda_say, R.string.lesson_sifa_shidda_not_say),
            spotIt = R.array.lesson_sifa_shidda_spot,
            sayIt = R.array.lesson_sifa_shidda_how,
        ),
        Lesson(
            conceptId = ConceptIds.SIFA_ISTILA,
            keyExample = KeyExample(1, 7, 0..0, R.string.lesson_sifa_istila_say, R.string.lesson_sifa_istila_not_say),
            spotIt = R.array.lesson_sifa_istila_spot,
            sayIt = R.array.lesson_sifa_istila_how,
        ),
        Lesson(
            conceptId = ConceptIds.SIFA_ITBAQ,
            keyExample = KeyExample(94, 3, 2..2, R.string.lesson_sifa_itbaq_say, R.string.lesson_sifa_itbaq_not_say),
            spotIt = R.array.lesson_sifa_itbaq_spot,
            sayIt = R.array.lesson_sifa_itbaq_how,
        ),
        Lesson(
            conceptId = ConceptIds.SIFA_SAFIR,
            keyExample = KeyExample(114, 4, 2..2, R.string.lesson_sifa_safir_say),
            spotIt = R.array.lesson_sifa_safir_spot,
            sayIt = R.array.lesson_sifa_safir_how,
        ),
        Lesson(
            conceptId = ConceptIds.SIFA_TAKRIR,
            keyExample = KeyExample(1, 2, 2..2, R.string.lesson_sifa_takrir_say, R.string.lesson_sifa_takrir_not_say),
            spotIt = R.array.lesson_sifa_takrir_spot,
            sayIt = R.array.lesson_sifa_takrir_how,
        ),
        Lesson(
            conceptId = ConceptIds.SIFA_TAFASHSHI,
            keyExample = KeyExample(1, 7, 8..8, R.string.lesson_sifa_tafashshi_say, R.string.lesson_sifa_tafashshi_not_say),
            spotIt = R.array.lesson_sifa_tafashshi_spot,
            sayIt = R.array.lesson_sifa_tafashshi_how,
        ),

        // ---- Tajweed: letters and lām ----
        Lesson(
            conceptId = ConceptIds.HEAVY_LETTERS,
            keyExample = KeyExample(
                1, 6, 1..1, R.string.lesson_heavy_letters_say,
                R.string.lesson_heavy_letters_not_say,
            ),
            spotIt = R.array.lesson_heavy_letters_spot,
            sayIt = R.array.lesson_heavy_letters_how,
        ),
        Lesson(
            conceptId = ConceptIds.LAM_SHAMSIYYA,
            keyExample = KeyExample(
                1, 1, 2..2, R.string.lesson_lam_shamsiyya_say,
                R.string.lesson_lam_shamsiyya_not_say,
            ),
            spotIt = R.array.lesson_lam_shamsiyya_spot,
            sayIt = R.array.lesson_lam_shamsiyya_how,
            confusedWith = ConfusedWith(
                ConceptIds.LAM_QAMARIYYA, R.string.lesson_lam_shamsiyya_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.LAM_QAMARIYYA,
            keyExample = KeyExample(
                1, 2, 0..0, R.string.lesson_lam_qamariyya_say,
                R.string.lesson_lam_qamariyya_not_say,
            ),
            spotIt = R.array.lesson_lam_qamariyya_spot,
            sayIt = R.array.lesson_lam_qamariyya_how,
            confusedWith = ConfusedWith(
                ConceptIds.LAM_SHAMSIYYA, R.string.lesson_lam_qamariyya_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.LAM_OF_ALLAH,
            keyExample = KeyExample(112, 1, 1..2, R.string.lesson_lam_of_allah_say),
            spotIt = R.array.lesson_lam_of_allah_spot,
            sayIt = R.array.lesson_lam_of_allah_how,
            confusedWith = ConfusedWith(
                ConceptIds.HEAVY_LETTERS, R.string.lesson_lam_of_allah_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.GHUNNAH,
            keyExample = KeyExample(
                114, 1, 3..3, R.string.lesson_ghunnah_say, R.string.lesson_ghunnah_not_say,
            ),
            spotIt = R.array.lesson_ghunnah_spot,
            sayIt = R.array.lesson_ghunnah_how,
            confusedWith = ConfusedWith(
                ConceptIds.IDGHAM_GHUNNAH, R.string.lesson_ghunnah_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.QALQALAH,
            keyExample = KeyExample(113, 1, 3..3, R.string.lesson_qalqalah_say),
            spotIt = R.array.lesson_qalqalah_spot,
            sayIt = R.array.lesson_qalqalah_how,
        ),

        // ---- Tajweed: rāʾ ----
        Lesson(
            conceptId = ConceptIds.RA_HEAVY,
            keyExample = KeyExample(1, 2, 2..2, R.string.lesson_ra_heavy_say),
            spotIt = R.array.lesson_ra_heavy_spot,
            sayIt = R.array.lesson_ra_heavy_how,
            confusedWith = ConfusedWith(ConceptIds.HEAVY_LETTERS, R.string.lesson_ra_heavy_confused),
        ),
        Lesson(
            conceptId = ConceptIds.RA_LIGHT,
            keyExample = KeyExample(1, 7, 4..4, R.string.lesson_ra_light_say),
            spotIt = R.array.lesson_ra_light_spot,
            sayIt = R.array.lesson_ra_light_how,
            confusedWith = ConfusedWith(ConceptIds.RA_HEAVY, R.string.lesson_ra_light_confused),
        ),

        // ---- Tajweed: madd ----
        Lesson(
            conceptId = ConceptIds.MADD_SIGN,
            keyExample = KeyExample(
                110, 1, 1..1, R.string.lesson_madd_sign_say, R.string.lesson_madd_sign_not_say,
            ),
            spotIt = R.array.lesson_madd_sign_spot,
            sayIt = R.array.lesson_madd_sign_how,
            confusedWith = ConfusedWith(ConceptIds.LONG_VOWELS, R.string.lesson_madd_sign_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_MUTTASIL,
            keyExample = KeyExample(82, 1, 1..1, R.string.lesson_madd_muttasil_say),
            spotIt = R.array.lesson_madd_muttasil_spot,
            sayIt = R.array.lesson_madd_muttasil_how,
            confusedWith = ConfusedWith(
                ConceptIds.MADD_MUNFASIL, R.string.lesson_madd_muttasil_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_MUNFASIL,
            keyExample = KeyExample(109, 2, 0..1, R.string.lesson_madd_munfasil_say),
            spotIt = R.array.lesson_madd_munfasil_spot,
            sayIt = R.array.lesson_madd_munfasil_how,
            confusedWith = ConfusedWith(
                ConceptIds.MADD_MUTTASIL, R.string.lesson_madd_munfasil_confused,
            ),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_LAZIM,
            keyExample = KeyExample(1, 7, 8..8, R.string.lesson_madd_lazim_say),
            spotIt = R.array.lesson_madd_lazim_spot,
            sayIt = R.array.lesson_madd_lazim_how,
            confusedWith = ConfusedWith(
                ConceptIds.MADD_MUTTASIL, R.string.lesson_madd_lazim_confused,
            ),
        ),

        // ---- Tajweed: two letters that meet ----
        Lesson(
            conceptId = ConceptIds.IDGHAM_MITHLAYN,
            keyExample = KeyExample(26, 63, 4..5, R.string.lesson_idgham_mithlayn_say, R.string.lesson_idgham_mithlayn_not_say),
            spotIt = R.array.lesson_idgham_mithlayn_spot,
            sayIt = R.array.lesson_idgham_mithlayn_how,
            confusedWith = ConfusedWith(ConceptIds.IDGHAM_MUTAJANISAYN, R.string.lesson_idgham_mithlayn_confused),
        ),
        Lesson(
            conceptId = ConceptIds.IDGHAM_MUTAJANISAYN,
            keyExample = KeyExample(2, 256, 4..5, R.string.lesson_idgham_mutajanisayn_say, R.string.lesson_idgham_mutajanisayn_not_say),
            spotIt = R.array.lesson_idgham_mutajanisayn_spot,
            sayIt = R.array.lesson_idgham_mutajanisayn_how,
            confusedWith = ConfusedWith(ConceptIds.IDGHAM_MUTAQARIBAYN, R.string.lesson_idgham_mutajanisayn_confused),
        ),
        Lesson(
            conceptId = ConceptIds.IDGHAM_MUTAQARIBAYN,
            keyExample = KeyExample(4, 158, 0..1, R.string.lesson_idgham_mutaqaribayn_say, R.string.lesson_idgham_mutaqaribayn_not_say),
            spotIt = R.array.lesson_idgham_mutaqaribayn_spot,
            sayIt = R.array.lesson_idgham_mutaqaribayn_how,
            confusedWith = ConfusedWith(ConceptIds.IDGHAM_NO_GHUNNAH, R.string.lesson_idgham_mutaqaribayn_confused),
        ),
        Lesson(
            conceptId = ConceptIds.LAM_SAKINAH,
            keyExample = KeyExample(112, 1, 0..1, R.string.lesson_lam_sakinah_say),
            spotIt = R.array.lesson_lam_sakinah_spot,
            sayIt = R.array.lesson_lam_sakinah_how,
            confusedWith = ConfusedWith(ConceptIds.LAM_SHAMSIYYA, R.string.lesson_lam_sakinah_confused),
        ),
        Lesson(
            conceptId = ConceptIds.TANWEEN_BEFORE_WASL,
            keyExample = KeyExample(2, 180, 8..9, R.string.lesson_tanween_before_wasl_say, R.string.lesson_tanween_before_wasl_not_say),
            spotIt = R.array.lesson_tanween_before_wasl_spot,
            sayIt = R.array.lesson_tanween_before_wasl_how,
            confusedWith = ConfusedWith(ConceptIds.HAMZAT_WASL, R.string.lesson_tanween_before_wasl_confused),
        ),

        // ---- Stopping, and the madds of stage 3 ----
        Lesson(
            conceptId = ConceptIds.STOPPING,
            keyExample = KeyExample(97, 5, 4..4, R.string.lesson_stopping_say, R.string.lesson_stopping_not_say),
            spotIt = R.array.lesson_stopping_spot,
            sayIt = R.array.lesson_stopping_how,
            confusedWith = ConfusedWith(ConceptIds.STOP_SIGNS, R.string.lesson_stopping_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_ARID,
            keyExample = KeyExample(1, 5, 3..3, R.string.lesson_madd_arid_say, R.string.lesson_madd_arid_not_say),
            spotIt = R.array.lesson_madd_arid_spot,
            sayIt = R.array.lesson_madd_arid_how,
            confusedWith = ConfusedWith(ConceptIds.MADD_LAZIM, R.string.lesson_madd_arid_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_LEEN,
            keyExample = KeyExample(106, 3, 3..3, R.string.lesson_madd_leen_say),
            spotIt = R.array.lesson_madd_leen_spot,
            sayIt = R.array.lesson_madd_leen_how,
            confusedWith = ConfusedWith(ConceptIds.MADD_ARID, R.string.lesson_madd_leen_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_IWAD,
            keyExample = KeyExample(110, 2, 6..6, R.string.lesson_madd_iwad_say, R.string.lesson_madd_iwad_not_say),
            spotIt = R.array.lesson_madd_iwad_spot,
            sayIt = R.array.lesson_madd_iwad_how,
            confusedWith = ConfusedWith(ConceptIds.TANWEEN, R.string.lesson_madd_iwad_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_BADAL,
            keyExample = KeyExample(103, 3, 2..2, R.string.lesson_madd_badal_say),
            spotIt = R.array.lesson_madd_badal_spot,
            sayIt = R.array.lesson_madd_badal_how,
            confusedWith = ConfusedWith(ConceptIds.MADD_MUTTASIL, R.string.lesson_madd_badal_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_SILA,
            keyExample = KeyExample(112, 4, 2..3, R.string.lesson_madd_sila_say, R.string.lesson_madd_sila_not_say),
            spotIt = R.array.lesson_madd_sila_spot,
            sayIt = R.array.lesson_madd_sila_how,
            confusedWith = ConfusedWith(ConceptIds.SMALL_MADD_LETTERS, R.string.lesson_madd_sila_confused),
        ),

        // ---- The muṣḥaf's own spellings and marks ----
        Lesson(
            conceptId = ConceptIds.DISJOINTED_LETTERS,
            keyExample = KeyExample(2, 1, 0..0, R.string.lesson_disjointed_letters_say, R.string.lesson_disjointed_letters_not_say),
            spotIt = R.array.lesson_disjointed_letters_spot,
            sayIt = R.array.lesson_disjointed_letters_how,
            confusedWith = ConfusedWith(ConceptIds.LETTERS, R.string.lesson_disjointed_letters_confused),
        ),
        Lesson(
            conceptId = ConceptIds.WASL_START,
            keyExample = KeyExample(1, 6, 0..0, R.string.lesson_wasl_start_say, R.string.lesson_wasl_start_not_say),
            spotIt = R.array.lesson_wasl_start_spot,
            sayIt = R.array.lesson_wasl_start_how,
            confusedWith = ConfusedWith(ConceptIds.HAMZAT_WASL, R.string.lesson_wasl_start_confused),
        ),
        Lesson(
            conceptId = ConceptIds.SPECIAL_SPELLINGS,
            keyExample = KeyExample(98, 5, 10..10, R.string.lesson_special_spellings_say, R.string.lesson_special_spellings_not_say),
            spotIt = R.array.lesson_special_spellings_spot,
            sayIt = R.array.lesson_special_spellings_how,
            confusedWith = ConfusedWith(ConceptIds.SILENT_LETTERS, R.string.lesson_special_spellings_confused),
        ),
        Lesson(
            conceptId = ConceptIds.MADD_MUQATTAAT,
            keyExample = KeyExample(68, 1, 0..0, R.string.lesson_madd_muqattaat_say, R.string.lesson_madd_muqattaat_not_say),
            spotIt = R.array.lesson_madd_muqattaat_spot,
            sayIt = R.array.lesson_madd_muqattaat_how,
            confusedWith = ConfusedWith(ConceptIds.MADD_LAZIM, R.string.lesson_madd_muqattaat_confused),
        ),
        Lesson(
            conceptId = ConceptIds.SAKTA,
            keyExample = KeyExample(75, 27, 1..2, R.string.lesson_sakta_say, R.string.lesson_sakta_not_say),
            spotIt = R.array.lesson_sakta_spot,
            sayIt = R.array.lesson_sakta_how,
            confusedWith = ConfusedWith(ConceptIds.STOP_SIGNS, R.string.lesson_sakta_confused),
        ),
        Lesson(
            conceptId = ConceptIds.HAFS_WORDS,
            keyExample = KeyExample(11, 41, 5..5, R.string.lesson_hafs_words_say, R.string.lesson_hafs_words_not_say),
            spotIt = R.array.lesson_hafs_words_spot,
            sayIt = R.array.lesson_hafs_words_how,
            confusedWith = ConfusedWith(ConceptIds.SILENT_LETTERS, R.string.lesson_hafs_words_confused),
        ),
    ) + GrammarLessons.all

    private val byConceptId: Map<String, Lesson> = all.associateBy { it.conceptId }

    operator fun get(conceptId: String): Lesson? = byConceptId[conceptId]
}
