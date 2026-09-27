package com.quranapp.android.learning.lessons

import androidx.annotation.ArrayRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.GrammarIds

/**
 * The grammar lessons, added unit by unit (#64–#68, #71–#74). Text is in
 * res/values/learning_grammar_lessons.xml.
 *
 * Key examples come from Al-Fātiḥah and Juz ʿAmma where possible. GrammarLessonExamplesTest
 * checks, against the learning pack, that each one shows its concept in those words.
 */
object GrammarLessons {
    val all: List<Lesson> = listOf(
        // ---- Unit 1 · Word types ----
        grammar(
            GrammarIds.ISM, KeyExample(1, 2, 0..0, R.string.grammar_lesson_ism_means),
            R.array.grammar_lesson_ism_spot, R.array.grammar_lesson_ism_how,
            ConfusedWith(GrammarIds.FIL, R.string.grammar_lesson_ism_confused),
        ),
        grammar(
            GrammarIds.FIL, KeyExample(112, 3, 0..1, R.string.grammar_lesson_fil_means),
            R.array.grammar_lesson_fil_spot, R.array.grammar_lesson_fil_how,
            ConfusedWith(GrammarIds.ISM, R.string.grammar_lesson_fil_confused),
        ),
        grammar(
            GrammarIds.HARF, KeyExample(97, 1, 2..4, R.string.grammar_lesson_harf_means),
            R.array.grammar_lesson_harf_spot, R.array.grammar_lesson_harf_how,
            ConfusedWith(GrammarIds.ISM, R.string.grammar_lesson_harf_confused),
        ),

        // ---- Unit 2 · Gender, number, definiteness ----
        grammar(
            GrammarIds.GENDER, KeyExample(89, 27, 0..2, R.string.grammar_lesson_gender_means),
            R.array.grammar_lesson_gender_spot, R.array.grammar_lesson_gender_how,
            ConfusedWith(ConceptIds.TA_MARBUTA, R.string.grammar_lesson_gender_confused),
        ),
        grammar(
            GrammarIds.DUAL, KeyExample(111, 1, 1..3, R.string.grammar_lesson_dual_means),
            R.array.grammar_lesson_dual_spot, R.array.grammar_lesson_dual_how,
            ConfusedWith(GrammarIds.SOUND_MASC_PLURAL, R.string.grammar_lesson_dual_confused),
        ),
        grammar(
            GrammarIds.SOUND_MASC_PLURAL, KeyExample(109, 1, 2..2, R.string.grammar_lesson_sound_masc_plural_means),
            R.array.grammar_lesson_sound_masc_plural_spot, R.array.grammar_lesson_sound_masc_plural_how,
            ConfusedWith(GrammarIds.DUAL, R.string.grammar_lesson_sound_masc_plural_confused),
        ),
        grammar(
            GrammarIds.SOUND_FEM_PLURAL, KeyExample(103, 3, 3..4, R.string.grammar_lesson_sound_fem_plural_means),
            R.array.grammar_lesson_sound_fem_plural_spot, R.array.grammar_lesson_sound_fem_plural_how,
            ConfusedWith(GrammarIds.BROKEN_PLURAL, R.string.grammar_lesson_sound_fem_plural_confused),
        ),
        grammar(
            GrammarIds.BROKEN_PLURAL, KeyExample(110, 2, 6..6, R.string.grammar_lesson_broken_plural_means),
            R.array.grammar_lesson_broken_plural_spot, R.array.grammar_lesson_broken_plural_how,
            ConfusedWith(GrammarIds.SOUND_MASC_PLURAL, R.string.grammar_lesson_broken_plural_confused),
        ),
        grammar(
            GrammarIds.DEFINITENESS, KeyExample(94, 5, 1..3, R.string.grammar_lesson_definiteness_means),
            R.array.grammar_lesson_definiteness_spot, R.array.grammar_lesson_definiteness_how,
            ConfusedWith(ConceptIds.TANWEEN, R.string.grammar_lesson_definiteness_confused),
        ),
    )

    private fun grammar(conceptId: String, keyExample: KeyExample, @ArrayRes spot: Int, @ArrayRes how: Int, confused: ConfusedWith) =
        Lesson(conceptId, keyExample, spotIt = spot, sayIt = how, confusedWith = confused)
}
