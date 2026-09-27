package com.quranapp.android.learning.lessons

import androidx.annotation.ArrayRes
import com.quranapp.android.R
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.GrammarIds

/**
 * The grammar lessons, added unit by unit (#64, #66–#69, #72–#73). Text is in
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

        // ---- Unit 11 · Verb forms and weak verbs ----
        grammar(
            GrammarIds.FORMS_2_4, KeyExample(96, 4, 0..2, R.string.grammar_lesson_forms_2_4_means),
            R.array.grammar_lesson_forms_2_4_spot, R.array.grammar_lesson_forms_2_4_how,
            ConfusedWith(ConceptIds.SHADDA, R.string.grammar_lesson_forms_2_4_confused),
        ),
        grammar(
            GrammarIds.FORMS_5_6, KeyExample(78, 1, 0..1, R.string.grammar_lesson_forms_5_6_means),
            R.array.grammar_lesson_forms_5_6_spot, R.array.grammar_lesson_forms_5_6_how,
            ConfusedWith(GrammarIds.FORMS_2_4, R.string.grammar_lesson_forms_5_6_confused),
        ),
        grammar(
            GrammarIds.FORMS_7_10, KeyExample(84, 1, 0..2, R.string.grammar_lesson_forms_7_10_means),
            R.array.grammar_lesson_forms_7_10_spot, R.array.grammar_lesson_forms_7_10_how,
            ConfusedWith(ConceptIds.HAMZAT_WASL, R.string.grammar_lesson_forms_7_10_confused),
        ),
        grammar(
            GrammarIds.HOLLOW, KeyExample(112, 1, 0..0, R.string.grammar_lesson_hollow_means),
            R.array.grammar_lesson_hollow_spot, R.array.grammar_lesson_hollow_how,
            ConfusedWith(GrammarIds.DEFECTIVE, R.string.grammar_lesson_hollow_confused),
        ),
        grammar(
            GrammarIds.DEFECTIVE, KeyExample(1, 6, 0..0, R.string.grammar_lesson_defective_means),
            R.array.grammar_lesson_defective_spot, R.array.grammar_lesson_defective_how,
            ConfusedWith(GrammarIds.HOLLOW, R.string.grammar_lesson_defective_confused),
        ),
        grammar(
            GrammarIds.OTHER_WEAK, KeyExample(111, 1, 0..1, R.string.grammar_lesson_other_weak_means),
            R.array.grammar_lesson_other_weak_spot, R.array.grammar_lesson_other_weak_how,
            ConfusedWith(GrammarIds.FORMS_2_4, R.string.grammar_lesson_other_weak_confused),
        ),

        // ---- Unit 12 · Participles and verbal nouns ----
        grammar(
            GrammarIds.ACTIVE_PARTICIPLE, KeyExample(109, 4, 0..4, R.string.grammar_lesson_active_participle_means),
            R.array.grammar_lesson_active_participle_spot, R.array.grammar_lesson_active_participle_how,
            ConfusedWith(GrammarIds.PASSIVE_PARTICIPLE, R.string.grammar_lesson_active_participle_confused),
        ),
        grammar(
            GrammarIds.PASSIVE_PARTICIPLE, KeyExample(1, 7, 4..6, R.string.grammar_lesson_passive_participle_means),
            R.array.grammar_lesson_passive_participle_spot, R.array.grammar_lesson_passive_participle_how,
            ConfusedWith(GrammarIds.ACTIVE_PARTICIPLE, R.string.grammar_lesson_passive_participle_confused),
        ),
        grammar(
            GrammarIds.VERBAL_NOUN, KeyExample(94, 4, 0..2, R.string.grammar_lesson_verbal_noun_means),
            R.array.grammar_lesson_verbal_noun_spot, R.array.grammar_lesson_verbal_noun_how,
            ConfusedWith(GrammarIds.ACTIVE_PARTICIPLE, R.string.grammar_lesson_verbal_noun_confused),
        ),

        // ---- Unit 17 · Conditions and questions ----
        grammar(
            GrammarIds.CONDITIONS, KeyExample(99, 7, 0..5, R.string.grammar_lesson_conditions_means),
            R.array.grammar_lesson_conditions_spot, R.array.grammar_lesson_conditions_how,
            ConfusedWith(GrammarIds.LAW, R.string.grammar_lesson_conditions_confused),
        ),
        grammar(
            GrammarIds.LAW, KeyExample(102, 5, 1..4, R.string.grammar_lesson_law_means),
            R.array.grammar_lesson_law_spot, R.array.grammar_lesson_law_how,
            ConfusedWith(GrammarIds.CONDITIONS, R.string.grammar_lesson_law_confused),
        ),
        grammar(
            GrammarIds.QUESTIONS, KeyExample(88, 1, 0..3, R.string.grammar_lesson_questions_means),
            R.array.grammar_lesson_questions_spot, R.array.grammar_lesson_questions_how,
            ConfusedWith(GrammarIds.MA, R.string.grammar_lesson_questions_confused),
        ),
        grammar(
            GrammarIds.EXCEPTION, KeyExample(103, 3, 0..2, R.string.grammar_lesson_exception_means),
            R.array.grammar_lesson_exception_spot, R.array.grammar_lesson_exception_how,
            ConfusedWith(GrammarIds.RESTRICTION, R.string.grammar_lesson_exception_confused),
        ),
        grammar(
            GrammarIds.VOCATIVE, KeyExample(109, 1, 1..2, R.string.grammar_lesson_vocative_means),
            R.array.grammar_lesson_vocative_spot, R.array.grammar_lesson_vocative_how,
            ConfusedWith(GrammarIds.ATTACHED_PRONOUN, R.string.grammar_lesson_vocative_confused),
        ),

        // ---- Unit 18 · Particles and emphasis ----
        grammar(
            GrammarIds.WA_FA_THUMMA, KeyExample(80, 21, 0..2, R.string.grammar_lesson_wa_fa_thumma_means),
            R.array.grammar_lesson_wa_fa_thumma_spot, R.array.grammar_lesson_wa_fa_thumma_how,
            ConfusedWith(GrammarIds.EMPHASIS, R.string.grammar_lesson_wa_fa_thumma_confused),
        ),
        grammar(
            GrammarIds.MA, KeyExample(101, 10, 0..3, R.string.grammar_lesson_ma_means),
            R.array.grammar_lesson_ma_spot, R.array.grammar_lesson_ma_how,
            ConfusedWith(GrammarIds.LA, R.string.grammar_lesson_ma_confused),
        ),
        grammar(
            GrammarIds.LA, KeyExample(88, 11, 0..3, R.string.grammar_lesson_la_means),
            R.array.grammar_lesson_la_spot, R.array.grammar_lesson_la_how,
            ConfusedWith(GrammarIds.MA, R.string.grammar_lesson_la_confused),
        ),
        grammar(
            GrammarIds.IN_AN, KeyExample(96, 14, 1..4, R.string.grammar_lesson_in_an_means),
            R.array.grammar_lesson_in_an_spot, R.array.grammar_lesson_in_an_how,
            ConfusedWith(GrammarIds.INNA, R.string.grammar_lesson_in_an_confused),
        ),
        grammar(
            GrammarIds.LAM_PARTICLES, KeyExample(100, 6, 0..3, R.string.grammar_lesson_lam_particles_means),
            R.array.grammar_lesson_lam_particles_spot, R.array.grammar_lesson_lam_particles_how,
            ConfusedWith(GrammarIds.PREPOSITION, R.string.grammar_lesson_lam_particles_confused),
        ),
        grammar(
            GrammarIds.EMPHASIS, KeyExample(103, 1, 0..0, R.string.grammar_lesson_emphasis_means),
            R.array.grammar_lesson_emphasis_spot, R.array.grammar_lesson_emphasis_how,
            ConfusedWith(GrammarIds.WA_FA_THUMMA, R.string.grammar_lesson_emphasis_confused),
        ),
        grammar(
            GrammarIds.RESTRICTION, KeyExample(51, 56, 0..5, R.string.grammar_lesson_restriction_means),
            R.array.grammar_lesson_restriction_spot, R.array.grammar_lesson_restriction_how,
            ConfusedWith(GrammarIds.EXCEPTION, R.string.grammar_lesson_restriction_confused),
        ),

        // ---- Unit 4 · Iḍāfa ----
        grammar(
            GrammarIds.IDAFA, KeyExample(1, 2, 2..3, R.string.grammar_lesson_idafa_means),
            R.array.grammar_lesson_idafa_spot, R.array.grammar_lesson_idafa_how,
            ConfusedWith(GrammarIds.ADJECTIVE, R.string.grammar_lesson_idafa_confused),
        ),

        // ---- Unit 13 · Nominal sentence ----
        grammar(
            GrammarIds.MUBTADA_KHABAR, KeyExample(93, 4, 0..1, R.string.grammar_lesson_mubtada_khabar_means),
            R.array.grammar_lesson_mubtada_khabar_spot, R.array.grammar_lesson_mubtada_khabar_how,
            ConfusedWith(GrammarIds.ADJECTIVE, R.string.grammar_lesson_mubtada_khabar_confused),
        ),
        grammar(
            GrammarIds.KHABAR_PHRASE, KeyExample(1, 2, 0..1, R.string.grammar_lesson_khabar_phrase_means),
            R.array.grammar_lesson_khabar_phrase_spot, R.array.grammar_lesson_khabar_phrase_how,
            ConfusedWith(GrammarIds.KHABAR_FIRST, R.string.grammar_lesson_khabar_phrase_confused),
        ),
        grammar(
            GrammarIds.KHABAR_FIRST, KeyExample(109, 6, 0..3, R.string.grammar_lesson_khabar_first_means),
            R.array.grammar_lesson_khabar_first_spot, R.array.grammar_lesson_khabar_first_how,
            ConfusedWith(GrammarIds.KHABAR_PHRASE, R.string.grammar_lesson_khabar_first_confused),
        ),

        // ---- Unit 15 · Verbal sentence ----
        grammar(
            GrammarIds.VERB_DOER_OBJECT, KeyExample(99, 2, 0..2, R.string.grammar_lesson_verb_doer_object_means),
            R.array.grammar_lesson_verb_doer_object_spot, R.array.grammar_lesson_verb_doer_object_how,
            ConfusedWith(GrammarIds.MUBTADA_KHABAR, R.string.grammar_lesson_verb_doer_object_confused),
        ),
        grammar(
            GrammarIds.VERB_AGREEMENT, KeyExample(99, 6, 1..2, R.string.grammar_lesson_verb_agreement_means),
            R.array.grammar_lesson_verb_agreement_spot, R.array.grammar_lesson_verb_agreement_how,
            ConfusedWith(GrammarIds.DOER_IN_VERB, R.string.grammar_lesson_verb_agreement_confused),
        ),
        grammar(
            GrammarIds.HIDDEN_DOER, KeyExample(96, 2, 0..1, R.string.grammar_lesson_hidden_doer_means),
            R.array.grammar_lesson_hidden_doer_spot, R.array.grammar_lesson_hidden_doer_how,
            ConfusedWith(GrammarIds.DOER_IN_VERB, R.string.grammar_lesson_hidden_doer_confused),
        ),
    )

    private fun grammar(conceptId: String, keyExample: KeyExample, @ArrayRes spot: Int, @ArrayRes how: Int, confused: ConfusedWith) =
        Lesson(conceptId, keyExample, spotIt = spot, sayIt = how, confusedWith = confused)
}
