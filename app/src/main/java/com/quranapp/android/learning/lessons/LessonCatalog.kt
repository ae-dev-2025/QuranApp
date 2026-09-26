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
    )

    private val byConceptId: Map<String, Lesson> = all.associateBy { it.conceptId }

    operator fun get(conceptId: String): Lesson? = byConceptId[conceptId]
}
