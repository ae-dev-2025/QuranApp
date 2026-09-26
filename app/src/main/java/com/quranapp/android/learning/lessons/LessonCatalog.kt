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
    )

    private val byConceptId: Map<String, Lesson> = all.associateBy { it.conceptId }

    operator fun get(conceptId: String): Lesson? = byConceptId[conceptId]
}
