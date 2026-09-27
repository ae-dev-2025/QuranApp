package com.quranapp.android.learning.concepts

import androidx.annotation.StringRes
import com.quranapp.android.R

/**
 * Stable ids of the grammar concepts. The prefix is the track; never rename an id after it
 * has been released.
 */
object GrammarIds {
    const val ISM = "grammar.ism"
    const val FIL = "grammar.fil"
    const val HARF = "grammar.harf"
    const val GENDER = "grammar.gender"
    const val DUAL = "grammar.dual"
    const val SOUND_MASC_PLURAL = "grammar.sound_masc_plural"
    const val SOUND_FEM_PLURAL = "grammar.sound_fem_plural"
    const val BROKEN_PLURAL = "grammar.broken_plural"
    const val DEFINITENESS = "grammar.definiteness"
    const val CASES = "grammar.cases"
    const val DUAL_PLURAL_ENDINGS = "grammar.dual_plural_endings"
    const val DIPTOTE = "grammar.diptote"
    const val FIVE_NOUNS = "grammar.five_nouns"
    const val DETACHED_PRONOUN = "grammar.detached_pronoun"
    const val ATTACHED_PRONOUN = "grammar.attached_pronoun"
    const val IYYA = "grammar.iyya"
    const val DEMONSTRATIVE = "grammar.demonstrative"
    const val RELATIVE = "grammar.relative"
    const val PREPOSITION = "grammar.preposition"
    const val IDAFA = "grammar.idafa"
    const val PRONOUN_POSSESSOR = "grammar.pronoun_possessor"
    const val ADJECTIVE = "grammar.adjective"
    const val ELATIVE = "grammar.elative"
    const val MUBTADA_KHABAR = "grammar.mubtada_khabar"
    const val KHABAR_PHRASE = "grammar.khabar_phrase"
    const val KHABAR_FIRST = "grammar.khabar_first"
    const val ROOT = "grammar.root"
    const val PATTERN = "grammar.pattern"
    const val PAST = "grammar.past"
    const val DOER_IN_VERB = "grammar.doer_in_verb"
    const val PRESENT = "grammar.present"
    const val SUBJUNCTIVE = "grammar.subjunctive"
    const val JUSSIVE = "grammar.jussive"
    const val QAD_SA = "grammar.qad_sa"
    const val COMMAND = "grammar.command"
    const val PROHIBITION = "grammar.prohibition"
    const val PASSIVE = "grammar.passive"
    const val INNA = "grammar.inna"
    const val KANA = "grammar.kana"
    const val LA_GENERIC = "grammar.la_generic"
    const val VERB_DOER_OBJECT = "grammar.verb_doer_object"
    const val VERB_AGREEMENT = "grammar.verb_agreement"
    const val HIDDEN_DOER = "grammar.hidden_doer"
    const val FORMS_2_4 = "grammar.forms_2_4"
    const val FORMS_5_6 = "grammar.forms_5_6"
    const val FORMS_7_10 = "grammar.forms_7_10"
    const val HOLLOW = "grammar.hollow"
    const val DEFECTIVE = "grammar.defective"
    const val OTHER_WEAK = "grammar.other_weak"
    const val ACTIVE_PARTICIPLE = "grammar.active_participle"
    const val PASSIVE_PARTICIPLE = "grammar.passive_participle"
    const val VERBAL_NOUN = "grammar.verbal_noun"
    const val PLACE_TIME_INTENSIVE = "grammar.place_time_intensive"
    const val ABSOLUTE_OBJECT = "grammar.absolute_object"
    const val OBJECT_OF_REASON = "grammar.object_of_reason"
    const val TIME_PLACE = "grammar.time_place"
    const val HAL = "grammar.hal"
    const val TAMYIZ = "grammar.tamyiz"
    const val CONDITIONS = "grammar.conditions"
    const val LAW = "grammar.law"
    const val QUESTIONS = "grammar.questions"
    const val EXCEPTION = "grammar.exception"
    const val VOCATIVE = "grammar.vocative"
    const val WA_FA_THUMMA = "grammar.wa_fa_thumma"
    const val MA = "grammar.ma"
    const val LA = "grammar.la"
    const val IN_AN = "grammar.in_an"
    const val LAM_PARTICLES = "grammar.lam_particles"
    const val EMPHASIS = "grammar.emphasis"
    const val RESTRICTION = "grammar.restriction"
}

/**
 * The grammar track: the design's 70 concepts in 18 units, in teaching order (a concept
 * comes after everything it builds on). Names follow the Grammar Guide of ILMHUB.org, the
 * project's Arabic review reference; the summaries are our own words, checked against it.
 *
 * Arabic terms are standard Unicode, shown in the system font next to the English name
 * (decision 6: English and Arabic), not in the Quran font.
 */
object GrammarCatalog {
    private val stages = mutableMapOf<String, Int>()

    val all: List<Concept> = listOf(
        // Unit 1 · Word types (stage 1)
        grammar(
            GrammarIds.ISM, "اِسْم",
            R.string.grammar_ism_title, R.string.grammar_ism_summary,
            1, ConceptIds.SHORT_VOWELS,
        ),
        grammar(
            GrammarIds.FIL, "فِعْل",
            R.string.grammar_fil_title, R.string.grammar_fil_summary,
            1, ConceptIds.SHORT_VOWELS,
        ),
        grammar(
            GrammarIds.HARF, "حَرْف",
            R.string.grammar_harf_title, R.string.grammar_harf_summary,
            1, ConceptIds.SHORT_VOWELS,
        ),
        // Unit 2 · Gender, number, definiteness (stage 2)
        grammar(
            GrammarIds.GENDER, "مُذَكَّر وَمُؤَنَّث",
            R.string.grammar_gender_title, R.string.grammar_gender_summary,
            2, GrammarIds.ISM,
        ),
        grammar(
            GrammarIds.DUAL, "مُثَنًّى",
            R.string.grammar_dual_title, R.string.grammar_dual_summary,
            2, GrammarIds.ISM,
        ),
        grammar(
            GrammarIds.SOUND_MASC_PLURAL, "جَمْع مُذَكَّر سَالِم",
            R.string.grammar_sound_masc_plural_title, R.string.grammar_sound_masc_plural_summary,
            2, GrammarIds.ISM,
        ),
        grammar(
            GrammarIds.SOUND_FEM_PLURAL, "جَمْع مُؤَنَّث سَالِم",
            R.string.grammar_sound_fem_plural_title, R.string.grammar_sound_fem_plural_summary,
            2, GrammarIds.ISM,
        ),
        grammar(
            GrammarIds.BROKEN_PLURAL, "جَمْع تَكْسِير",
            R.string.grammar_broken_plural_title, R.string.grammar_broken_plural_summary,
            2, GrammarIds.ISM,
        ),
        grammar(
            GrammarIds.DEFINITENESS, "مَعْرِفَة وَنَكِرَة",
            R.string.grammar_definiteness_title, R.string.grammar_definiteness_summary,
            2, GrammarIds.ISM, ConceptIds.TANWEEN,
        ),
        // Unit 3 · Case endings (stage 2)
        grammar(
            GrammarIds.CASES, "الرَّفْع وَالنَّصْب وَالْجَرّ",
            R.string.grammar_cases_title, R.string.grammar_cases_summary,
            2, GrammarIds.DEFINITENESS,
        ),
        grammar(
            GrammarIds.DUAL_PLURAL_ENDINGS, "الْعَلَامَات الْفَرْعِيَّة",
            R.string.grammar_dual_plural_endings_title, R.string.grammar_dual_plural_endings_summary,
            2, GrammarIds.CASES, GrammarIds.DUAL, GrammarIds.SOUND_MASC_PLURAL,
        ),
        grammar(
            GrammarIds.DIPTOTE, "الْمَمْنُوع مِنَ الصَّرْف",
            R.string.grammar_diptote_title, R.string.grammar_diptote_summary,
            2, GrammarIds.CASES,
        ),
        grammar(
            GrammarIds.FIVE_NOUNS, "الْأَسْمَاء الْخَمْسَة",
            R.string.grammar_five_nouns_title, R.string.grammar_five_nouns_summary,
            2, GrammarIds.CASES,
        ),
        // Unit 6 · Pronouns and relatives (stage 3)
        grammar(
            GrammarIds.DETACHED_PRONOUN, "الضَّمِير الْمُنْفَصِل",
            R.string.grammar_detached_pronoun_title, R.string.grammar_detached_pronoun_summary,
            3, GrammarIds.GENDER, GrammarIds.DUAL,
        ),
        grammar(
            GrammarIds.ATTACHED_PRONOUN, "الضَّمِير الْمُتَّصِل",
            R.string.grammar_attached_pronoun_title, R.string.grammar_attached_pronoun_summary,
            3, GrammarIds.DETACHED_PRONOUN,
        ),
        grammar(
            GrammarIds.IYYA, "إِيَّا",
            R.string.grammar_iyya_title, R.string.grammar_iyya_summary,
            3, GrammarIds.DETACHED_PRONOUN,
        ),
        grammar(
            GrammarIds.DEMONSTRATIVE, "اِسْم الْإِشَارَة",
            R.string.grammar_demonstrative_title, R.string.grammar_demonstrative_summary,
            3, GrammarIds.GENDER, GrammarIds.DEFINITENESS,
        ),
        grammar(
            GrammarIds.RELATIVE, "الِاسْم الْمَوْصُول",
            R.string.grammar_relative_title, R.string.grammar_relative_summary,
            3, GrammarIds.DEMONSTRATIVE,
        ),
        // Unit 4 · Prepositions and iḍāfa (stage 3)
        grammar(
            GrammarIds.PREPOSITION, "حَرْف الْجَرّ",
            R.string.grammar_preposition_title, R.string.grammar_preposition_summary,
            3, GrammarIds.CASES, GrammarIds.HARF,
        ),
        grammar(
            GrammarIds.IDAFA, "الْإِضَافَة",
            R.string.grammar_idafa_title, R.string.grammar_idafa_summary,
            3, GrammarIds.CASES, GrammarIds.DEFINITENESS,
        ),
        grammar(
            GrammarIds.PRONOUN_POSSESSOR, "الْمُضَاف إِلَى الضَّمِير",
            R.string.grammar_pronoun_possessor_title, R.string.grammar_pronoun_possessor_summary,
            3, GrammarIds.IDAFA, GrammarIds.ATTACHED_PRONOUN,
        ),
        // Unit 5 · Adjectives (stage 3)
        grammar(
            GrammarIds.ADJECTIVE, "النَّعْت",
            R.string.grammar_adjective_title, R.string.grammar_adjective_summary,
            3, GrammarIds.CASES, GrammarIds.GENDER, GrammarIds.DEFINITENESS,
        ),
        grammar(
            GrammarIds.ELATIVE, "اِسْم التَّفْضِيل",
            R.string.grammar_elative_title, R.string.grammar_elative_summary,
            3, GrammarIds.ADJECTIVE, GrammarIds.DIPTOTE,
        ),
        // Unit 13 · Nominal sentence (stage 3)
        grammar(
            GrammarIds.MUBTADA_KHABAR, "الْمُبْتَدَأ وَالْخَبَر",
            R.string.grammar_mubtada_khabar_title, R.string.grammar_mubtada_khabar_summary,
            3, GrammarIds.CASES, GrammarIds.IDAFA,
        ),
        grammar(
            GrammarIds.KHABAR_PHRASE, "الْخَبَر شِبْه جُمْلَة أَوْ جُمْلَة",
            R.string.grammar_khabar_phrase_title, R.string.grammar_khabar_phrase_summary,
            3, GrammarIds.MUBTADA_KHABAR, GrammarIds.PREPOSITION,
        ),
        grammar(
            GrammarIds.KHABAR_FIRST, "الْخَبَر الْمُقَدَّم",
            R.string.grammar_khabar_first_title, R.string.grammar_khabar_first_summary,
            3, GrammarIds.KHABAR_PHRASE,
        ),
        // Unit 7 · Roots and patterns (stage 4)
        grammar(
            GrammarIds.ROOT, "الْجِذْر",
            R.string.grammar_root_title, R.string.grammar_root_summary,
            4, GrammarIds.ISM, GrammarIds.FIL,
        ),
        grammar(
            GrammarIds.PATTERN, "الْوَزْن",
            R.string.grammar_pattern_title, R.string.grammar_pattern_summary,
            4, GrammarIds.ROOT,
        ),
        // Unit 8 · Past tense (stage 4)
        grammar(
            GrammarIds.PAST, "الْفِعْل الْمَاضِي",
            R.string.grammar_past_title, R.string.grammar_past_summary,
            4, GrammarIds.ATTACHED_PRONOUN, GrammarIds.PATTERN,
        ),
        grammar(
            GrammarIds.DOER_IN_VERB, "الْفَاعِل الضَّمِير",
            R.string.grammar_doer_in_verb_title, R.string.grammar_doer_in_verb_summary,
            4, GrammarIds.PAST,
        ),
        // Unit 9 · Present tense and moods (stage 4)
        grammar(
            GrammarIds.PRESENT, "الْفِعْل الْمُضَارِع",
            R.string.grammar_present_title, R.string.grammar_present_summary,
            4, GrammarIds.PAST,
        ),
        grammar(
            GrammarIds.SUBJUNCTIVE, "الْمُضَارِع الْمَنْصُوب",
            R.string.grammar_subjunctive_title, R.string.grammar_subjunctive_summary,
            4, GrammarIds.PRESENT,
        ),
        grammar(
            GrammarIds.JUSSIVE, "الْمُضَارِع الْمَجْزُوم",
            R.string.grammar_jussive_title, R.string.grammar_jussive_summary,
            4, GrammarIds.PRESENT,
        ),
        grammar(
            GrammarIds.QAD_SA, "قَدْ وَالسِّين وَسَوْفَ",
            R.string.grammar_qad_sa_title, R.string.grammar_qad_sa_summary,
            4, GrammarIds.PRESENT,
        ),
        // Unit 10 · Command, forbidding, passive (stage 4)
        grammar(
            GrammarIds.COMMAND, "فِعْل الْأَمْر",
            R.string.grammar_command_title, R.string.grammar_command_summary,
            4, GrammarIds.PRESENT,
        ),
        grammar(
            GrammarIds.PROHIBITION, "لَا النَّاهِيَة",
            R.string.grammar_prohibition_title, R.string.grammar_prohibition_summary,
            4, GrammarIds.JUSSIVE,
        ),
        grammar(
            GrammarIds.PASSIVE, "الْمَبْنِيّ لِلْمَجْهُول",
            R.string.grammar_passive_title, R.string.grammar_passive_summary,
            4, GrammarIds.PRESENT,
        ),
        // Unit 14 · Inna, kāna and sisters (stage 4)
        grammar(
            GrammarIds.INNA, "إِنَّ وَأَخَوَاتُهَا",
            R.string.grammar_inna_title, R.string.grammar_inna_summary,
            4, GrammarIds.MUBTADA_KHABAR,
        ),
        grammar(
            GrammarIds.KANA, "كَانَ وَأَخَوَاتُهَا",
            R.string.grammar_kana_title, R.string.grammar_kana_summary,
            4, GrammarIds.MUBTADA_KHABAR,
        ),
        grammar(
            GrammarIds.LA_GENERIC, "لَا النَّافِيَة لِلْجِنْس",
            R.string.grammar_la_generic_title, R.string.grammar_la_generic_summary,
            4, GrammarIds.INNA,
        ),
        // Unit 15 · Verbal sentence (stage 4)
        grammar(
            GrammarIds.VERB_DOER_OBJECT, "الْفِعْل وَالْفَاعِل وَالْمَفْعُول بِهِ",
            R.string.grammar_verb_doer_object_title, R.string.grammar_verb_doer_object_summary,
            4, GrammarIds.CASES, GrammarIds.DOER_IN_VERB,
        ),
        grammar(
            GrammarIds.VERB_AGREEMENT, "مُطَابَقَة الْفِعْل",
            R.string.grammar_verb_agreement_title, R.string.grammar_verb_agreement_summary,
            4, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.HIDDEN_DOER, "الضَّمِير الْمُسْتَتِر",
            R.string.grammar_hidden_doer_title, R.string.grammar_hidden_doer_summary,
            4, GrammarIds.VERB_DOER_OBJECT,
        ),
        // Unit 11 · Verb forms and weak verbs (stage 5)
        grammar(
            GrammarIds.FORMS_2_4, "الْأَوْزَان الثَّانِي إِلَى الرَّابِع",
            R.string.grammar_forms_2_4_title, R.string.grammar_forms_2_4_summary,
            5, GrammarIds.PRESENT, GrammarIds.PATTERN,
        ),
        grammar(
            GrammarIds.FORMS_5_6, "الْوَزْنَان الْخَامِس وَالسَّادِس",
            R.string.grammar_forms_5_6_title, R.string.grammar_forms_5_6_summary,
            5, GrammarIds.FORMS_2_4,
        ),
        grammar(
            GrammarIds.FORMS_7_10, "الْأَوْزَان السَّابِع إِلَى الْعَاشِر",
            R.string.grammar_forms_7_10_title, R.string.grammar_forms_7_10_summary,
            5, GrammarIds.FORMS_5_6,
        ),
        grammar(
            GrammarIds.HOLLOW, "الْفِعْل الْأَجْوَف",
            R.string.grammar_hollow_title, R.string.grammar_hollow_summary,
            5, GrammarIds.PRESENT, GrammarIds.ROOT,
        ),
        grammar(
            GrammarIds.DEFECTIVE, "الْفِعْل النَّاقِص",
            R.string.grammar_defective_title, R.string.grammar_defective_summary,
            5, GrammarIds.PRESENT, GrammarIds.ROOT,
        ),
        grammar(
            GrammarIds.OTHER_WEAK, "الْمِثَال وَالْمُضَعَّف وَالْمَهْمُوز",
            R.string.grammar_other_weak_title, R.string.grammar_other_weak_summary,
            5, GrammarIds.PRESENT, GrammarIds.ROOT,
        ),
        // Unit 12 · Participles and verbal nouns (stage 5)
        grammar(
            GrammarIds.ACTIVE_PARTICIPLE, "اِسْم الْفَاعِل",
            R.string.grammar_active_participle_title, R.string.grammar_active_participle_summary,
            5, GrammarIds.FORMS_2_4,
        ),
        grammar(
            GrammarIds.PASSIVE_PARTICIPLE, "اِسْم الْمَفْعُول",
            R.string.grammar_passive_participle_title, R.string.grammar_passive_participle_summary,
            5, GrammarIds.ACTIVE_PARTICIPLE,
        ),
        grammar(
            GrammarIds.VERBAL_NOUN, "الْمَصْدَر",
            R.string.grammar_verbal_noun_title, R.string.grammar_verbal_noun_summary,
            5, GrammarIds.FORMS_2_4,
        ),
        grammar(
            GrammarIds.PLACE_TIME_INTENSIVE, "اِسْمَا الزَّمَان وَالْمَكَان وَصِيَغ الْمُبَالَغَة",
            R.string.grammar_place_time_intensive_title, R.string.grammar_place_time_intensive_summary,
            5, GrammarIds.ACTIVE_PARTICIPLE,
        ),
        // Unit 16 · Objects, ḥāl, tamyīz (stage 5)
        grammar(
            GrammarIds.ABSOLUTE_OBJECT, "الْمَفْعُول الْمُطْلَق",
            R.string.grammar_absolute_object_title, R.string.grammar_absolute_object_summary,
            5, GrammarIds.VERB_DOER_OBJECT, GrammarIds.VERBAL_NOUN,
        ),
        grammar(
            GrammarIds.OBJECT_OF_REASON, "الْمَفْعُول لَهُ",
            R.string.grammar_object_of_reason_title, R.string.grammar_object_of_reason_summary,
            5, GrammarIds.VERB_DOER_OBJECT, GrammarIds.VERBAL_NOUN,
        ),
        grammar(
            GrammarIds.TIME_PLACE, "الْمَفْعُول فِيهِ",
            R.string.grammar_time_place_title, R.string.grammar_time_place_summary,
            5, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.HAL, "الْحَال",
            R.string.grammar_hal_title, R.string.grammar_hal_summary,
            5, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.TAMYIZ, "التَّمْيِيز",
            R.string.grammar_tamyiz_title, R.string.grammar_tamyiz_summary,
            5, GrammarIds.VERB_DOER_OBJECT,
        ),
        // Unit 17 · Conditions and questions (stage 5)
        grammar(
            GrammarIds.CONDITIONS, "الشَّرْط",
            R.string.grammar_conditions_title, R.string.grammar_conditions_summary,
            5, GrammarIds.JUSSIVE, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.LAW, "لَوْ وَلَوْلَا",
            R.string.grammar_law_title, R.string.grammar_law_summary,
            5, GrammarIds.CONDITIONS,
        ),
        grammar(
            GrammarIds.QUESTIONS, "الِاسْتِفْهَام",
            R.string.grammar_questions_title, R.string.grammar_questions_summary,
            5, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.EXCEPTION, "الِاسْتِثْنَاء",
            R.string.grammar_exception_title, R.string.grammar_exception_summary,
            5, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.VOCATIVE, "النِّدَاء",
            R.string.grammar_vocative_title, R.string.grammar_vocative_summary,
            5, GrammarIds.VERB_DOER_OBJECT,
        ),
        // Unit 18 · Particles and emphasis (stage 5)
        grammar(
            GrammarIds.WA_FA_THUMMA, "حَرْف عَطْف",
            R.string.grammar_wa_fa_thumma_title, R.string.grammar_wa_fa_thumma_summary,
            5, GrammarIds.MUBTADA_KHABAR, GrammarIds.VERB_DOER_OBJECT,
        ),
        grammar(
            GrammarIds.MA, "مَا",
            R.string.grammar_ma_title, R.string.grammar_ma_summary,
            5, GrammarIds.RELATIVE, GrammarIds.QUESTIONS,
        ),
        grammar(
            GrammarIds.LA, "لَا",
            R.string.grammar_la_title, R.string.grammar_la_summary,
            5, GrammarIds.PROHIBITION, GrammarIds.LA_GENERIC,
        ),
        grammar(
            GrammarIds.IN_AN, "إِنْ وَأَنْ وَإِنَّ وَأَنَّ",
            R.string.grammar_in_an_title, R.string.grammar_in_an_summary,
            5, GrammarIds.INNA, GrammarIds.SUBJUNCTIVE, GrammarIds.CONDITIONS,
        ),
        grammar(
            GrammarIds.LAM_PARTICLES, "اللَّامَات",
            R.string.grammar_lam_particles_title, R.string.grammar_lam_particles_summary,
            5, GrammarIds.PREPOSITION, GrammarIds.SUBJUNCTIVE,
        ),
        grammar(
            GrammarIds.EMPHASIS, "التَّوْكِيد وَالْقَسَم",
            R.string.grammar_emphasis_title, R.string.grammar_emphasis_summary,
            5, GrammarIds.INNA,
        ),
        grammar(
            GrammarIds.RESTRICTION, "الْحَصْر وَالتَّقْدِيم",
            R.string.grammar_restriction_title, R.string.grammar_restriction_summary,
            5, GrammarIds.KHABAR_FIRST, GrammarIds.EXCEPTION, GrammarIds.IYYA,
        ),
    )

    /** The design's stage for a grammar concept, or null for other tracks. */
    fun stageOf(id: String): Int? = stages[id]

    private fun grammar(
        id: String,
        arabicTerm: String,
        @StringRes title: Int,
        @StringRes summary: Int,
        stage: Int,
        vararg prerequisites: String,
    ): Concept {
        stages[id] = stage
        return Concept(id, Track.GRAMMAR, title, summary, prerequisites.toList(), arabicTerm)
    }
}
