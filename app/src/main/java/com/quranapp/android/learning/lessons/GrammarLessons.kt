package com.quranapp.android.learning.lessons

import androidx.annotation.ArrayRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.GrammarIds

/**
 * The grammar lessons, added unit by unit (#64, #66–#69, #72–#75). Text is in
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

        // ---- Unit 3 · Case endings ----
        grammar(
            GrammarIds.CASES, KeyExample(99, 2, 0..2, R.string.grammar_lesson_cases_means),
            R.array.grammar_lesson_cases_spot, R.array.grammar_lesson_cases_how,
            ConfusedWith(ConceptIds.SHORT_VOWELS, R.string.grammar_lesson_cases_confused),
        ),
        grammar(
            GrammarIds.DUAL_PLURAL_ENDINGS, KeyExample(90, 10, 0..1, R.string.grammar_lesson_dual_plural_endings_means),
            R.array.grammar_lesson_dual_plural_endings_spot, R.array.grammar_lesson_dual_plural_endings_how,
            ConfusedWith(GrammarIds.SOUND_FEM_PLURAL, R.string.grammar_lesson_dual_plural_endings_confused),
        ),
        grammar(
            GrammarIds.FIVE_NOUNS, KeyExample(111, 1, 2..3, R.string.grammar_lesson_five_nouns_means),
            R.array.grammar_lesson_five_nouns_spot, R.array.grammar_lesson_five_nouns_how,
            ConfusedWith(GrammarIds.DUAL_PLURAL_ENDINGS, R.string.grammar_lesson_five_nouns_confused),
        ),

        // ---- Unit 6 · Pronouns and relatives ----
        grammar(
            GrammarIds.DETACHED_PRONOUN, KeyExample(112, 1, 1..2, R.string.grammar_lesson_detached_pronoun_means),
            R.array.grammar_lesson_detached_pronoun_spot, R.array.grammar_lesson_detached_pronoun_how,
            ConfusedWith(GrammarIds.ATTACHED_PRONOUN, R.string.grammar_lesson_detached_pronoun_confused),
        ),
        grammar(
            GrammarIds.ATTACHED_PRONOUN, KeyExample(108, 1, 1..1, R.string.grammar_lesson_attached_pronoun_means),
            R.array.grammar_lesson_attached_pronoun_spot, R.array.grammar_lesson_attached_pronoun_how,
            ConfusedWith(GrammarIds.DETACHED_PRONOUN, R.string.grammar_lesson_attached_pronoun_confused),
        ),
        grammar(
            GrammarIds.IYYA, KeyExample(1, 5, 0..1, R.string.grammar_lesson_iyya_means),
            R.array.grammar_lesson_iyya_spot, R.array.grammar_lesson_iyya_how,
            ConfusedWith(GrammarIds.DETACHED_PRONOUN, R.string.grammar_lesson_iyya_confused),
        ),
        grammar(
            GrammarIds.DEMONSTRATIVE, KeyExample(95, 3, 0..1, R.string.grammar_lesson_demonstrative_means),
            R.array.grammar_lesson_demonstrative_spot, R.array.grammar_lesson_demonstrative_how,
            ConfusedWith(GrammarIds.RELATIVE, R.string.grammar_lesson_demonstrative_confused),
        ),
        grammar(
            GrammarIds.RELATIVE, KeyExample(1, 7, 1..3, R.string.grammar_lesson_relative_means),
            R.array.grammar_lesson_relative_spot, R.array.grammar_lesson_relative_how,
            ConfusedWith(GrammarIds.DEMONSTRATIVE, R.string.grammar_lesson_relative_confused),
        ),

        // ---- Unit 4 · Prepositions ----
        grammar(
            GrammarIds.PREPOSITION, KeyExample(114, 1, 1..3, R.string.grammar_lesson_preposition_means),
            R.array.grammar_lesson_preposition_spot, R.array.grammar_lesson_preposition_how,
            ConfusedWith(GrammarIds.RELATIVE, R.string.grammar_lesson_preposition_confused),
        ),
        grammar(
            GrammarIds.PRONOUN_POSSESSOR, KeyExample(108, 2, 1..1, R.string.grammar_lesson_pronoun_possessor_means),
            R.array.grammar_lesson_pronoun_possessor_spot, R.array.grammar_lesson_pronoun_possessor_how,
            ConfusedWith(GrammarIds.IDAFA, R.string.grammar_lesson_pronoun_possessor_confused),
        ),

        // ---- Unit 5 · Adjectives ----
        grammar(
            GrammarIds.ADJECTIVE, KeyExample(1, 6, 1..2, R.string.grammar_lesson_adjective_means),
            R.array.grammar_lesson_adjective_spot, R.array.grammar_lesson_adjective_how,
            ConfusedWith(GrammarIds.IDAFA, R.string.grammar_lesson_adjective_confused),
        ),

        // ---- Unit 7 · Roots and patterns ----
        grammar(
            GrammarIds.ROOT, KeyExample(96, 5, 0..4, R.string.grammar_lesson_root_means),
            R.array.grammar_lesson_root_spot, R.array.grammar_lesson_root_how,
            ConfusedWith(GrammarIds.PATTERN, R.string.grammar_lesson_root_confused),
        ),
        grammar(
            GrammarIds.PATTERN, KeyExample(1, 4, 0..0, R.string.grammar_lesson_pattern_means),
            R.array.grammar_lesson_pattern_spot, R.array.grammar_lesson_pattern_how,
            ConfusedWith(GrammarIds.ROOT, R.string.grammar_lesson_pattern_confused),
        ),

        // ---- Unit 8 · Past tense ----
        grammar(
            GrammarIds.PAST, KeyExample(105, 1, 2..4, R.string.grammar_lesson_past_means),
            R.array.grammar_lesson_past_spot, R.array.grammar_lesson_past_how,
            ConfusedWith(GrammarIds.PRESENT, R.string.grammar_lesson_past_confused),
        ),
        grammar(
            GrammarIds.DOER_IN_VERB, KeyExample(1, 7, 2..2, R.string.grammar_lesson_doer_in_verb_means),
            R.array.grammar_lesson_doer_in_verb_spot, R.array.grammar_lesson_doer_in_verb_how,
            ConfusedWith(GrammarIds.ATTACHED_PRONOUN, R.string.grammar_lesson_doer_in_verb_confused),
        ),

        // ---- Unit 9 · Present tense and moods ----
        grammar(
            GrammarIds.PRESENT, KeyExample(109, 2, 0..3, R.string.grammar_lesson_present_means),
            R.array.grammar_lesson_present_spot, R.array.grammar_lesson_present_how,
            ConfusedWith(GrammarIds.PAST, R.string.grammar_lesson_present_confused),
        ),
        grammar(
            GrammarIds.SUBJUNCTIVE, KeyExample(90, 5, 2..5, R.string.grammar_lesson_subjunctive_means),
            R.array.grammar_lesson_subjunctive_spot, R.array.grammar_lesson_subjunctive_how,
            ConfusedWith(GrammarIds.IN_AN, R.string.grammar_lesson_subjunctive_confused),
        ),
        grammar(
            GrammarIds.JUSSIVE, KeyExample(94, 1, 0..1, R.string.grammar_lesson_jussive_means),
            R.array.grammar_lesson_jussive_spot, R.array.grammar_lesson_jussive_how,
            ConfusedWith(ConceptIds.SUKUN, R.string.grammar_lesson_jussive_confused),
        ),
        grammar(
            GrammarIds.QAD_SA, KeyExample(87, 14, 0..1, R.string.grammar_lesson_qad_sa_means),
            R.array.grammar_lesson_qad_sa_spot, R.array.grammar_lesson_qad_sa_how,
            ConfusedWith(GrammarIds.PRESENT, R.string.grammar_lesson_qad_sa_confused),
        ),

        // ---- Unit 10 · Command, forbidding, passive ----
        grammar(
            GrammarIds.COMMAND, KeyExample(96, 1, 0..0, R.string.grammar_lesson_command_means),
            R.array.grammar_lesson_command_spot, R.array.grammar_lesson_command_how,
            ConfusedWith(GrammarIds.PROHIBITION, R.string.grammar_lesson_command_confused),
        ),
        grammar(
            GrammarIds.PROHIBITION, KeyExample(93, 9, 0..3, R.string.grammar_lesson_prohibition_means),
            R.array.grammar_lesson_prohibition_spot, R.array.grammar_lesson_prohibition_how,
            ConfusedWith(GrammarIds.LA, R.string.grammar_lesson_prohibition_confused),
        ),
        grammar(
            GrammarIds.PASSIVE, KeyExample(112, 3, 2..3, R.string.grammar_lesson_passive_means),
            R.array.grammar_lesson_passive_spot, R.array.grammar_lesson_passive_how,
            ConfusedWith(GrammarIds.PASSIVE_PARTICIPLE, R.string.grammar_lesson_passive_confused),
        ),

        // ---- Unit 14 · Inna, kāna and sisters ----
        grammar(
            GrammarIds.INNA, KeyExample(103, 2, 0..3, R.string.grammar_lesson_inna_means),
            R.array.grammar_lesson_inna_spot, R.array.grammar_lesson_inna_how,
            ConfusedWith(GrammarIds.KANA, R.string.grammar_lesson_inna_confused),
        ),
        grammar(
            GrammarIds.KANA, KeyExample(112, 4, 0..4, R.string.grammar_lesson_kana_means),
            R.array.grammar_lesson_kana_spot, R.array.grammar_lesson_kana_how,
            ConfusedWith(GrammarIds.INNA, R.string.grammar_lesson_kana_confused),
        ),
        grammar(
            GrammarIds.LA_GENERIC, KeyExample(2, 2, 2..4, R.string.grammar_lesson_la_generic_means),
            R.array.grammar_lesson_la_generic_spot, R.array.grammar_lesson_la_generic_how,
            ConfusedWith(GrammarIds.LA, R.string.grammar_lesson_la_generic_confused),
        ),
    )

    private fun grammar(conceptId: String, keyExample: KeyExample, @ArrayRes spot: Int, @ArrayRes how: Int, confused: ConfusedWith) =
        Lesson(conceptId, keyExample, spotIt = spot, sayIt = how, confusedWith = confused)
}
