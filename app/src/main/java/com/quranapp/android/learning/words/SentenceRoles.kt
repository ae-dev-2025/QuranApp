package com.quranapp.android.learning.words

import androidx.annotation.StringRes
import com.quranapp.android.R
import com.quranapp.android.learning.pack.SyntaxEntity

/**
 * A word's job in its sentence, in words a learner knows: MASAQ's role codes grouped into the
 * roles the grammar lessons teach. [arabic] is the traditional term, shown next to the name.
 */
enum class SentenceRole(@StringRes val nameRes: Int, val arabic: String) {
    SUBJECT(R.string.learning_role_subject, "مُبۡتَدَأ"),
    SUBJECT_AFTER(R.string.learning_role_subject_after, "مُبۡتَدَأ مُؤَخَّر"),
    PREDICATE(R.string.learning_role_predicate, "خَبَر"),
    PAST_VERB(R.string.learning_role_past_verb, "فِعۡل مَاضٍ"),
    PRESENT_VERB(R.string.learning_role_present_verb, "فِعۡل مُضَارِع"),
    COMMAND_VERB(R.string.learning_role_command_verb, "فِعۡل أَمۡر"),
    DOER(R.string.learning_role_doer, "فَاعِل"),
    STAND_IN_DOER(R.string.learning_role_stand_in_doer, "نَائِب فَاعِل"),
    OBJECT(R.string.learning_role_object, "مَفۡعُول بِهِ"),
    PREPOSITION(R.string.learning_role_preposition, "حَرۡف جَرّ"),
    AFTER_PREPOSITION(R.string.learning_role_after_preposition, "اسۡم مَجۡرُور"),
    OWNER(R.string.learning_role_owner, "مُضَاف إِلَيۡهِ"),
    ADJECTIVE(R.string.learning_role_adjective, "نَعۡت"),
    SUBSTITUTE(R.string.learning_role_substitute, "بَدَل"),
    JOINING(R.string.learning_role_joining, "حَرۡف عَطۡف"),
    JOINED(R.string.learning_role_joined, "مَعۡطُوف"),
    INNA(R.string.learning_role_inna, "حَرۡف نَاسِخ"),
    INNA_SUBJECT(R.string.learning_role_inna_subject, "اسۡم إِنَّ"),
    INNA_PREDICATE(R.string.learning_role_inna_predicate, "خَبَر إِنَّ"),
    KANA(R.string.learning_role_kana, "فِعۡل نَاسِخ"),
    KANA_SUBJECT(R.string.learning_role_kana_subject, "اسۡم كَانَ"),
    KANA_PREDICATE(R.string.learning_role_kana_predicate, "خَبَر كَانَ"),
    TIME(R.string.learning_role_time, "ظَرۡف زَمَان"),
    PLACE(R.string.learning_role_place, "ظَرۡف مَكَان"),
    HAL(R.string.learning_role_hal, "حَال"),
    TAMYIZ(R.string.learning_role_tamyiz, "تَمۡيِيز"),
    ABSOLUTE_OBJECT(R.string.learning_role_absolute_object, "مَفۡعُول مُطۡلَق"),
    REASON(R.string.learning_role_reason, "مَفۡعُول لِأَجۡلِهِ"),
    CONDITION(R.string.learning_role_condition, "أَدَاة شَرۡط"),
    JAZM_PARTICLE(R.string.learning_role_jazm_particle, "حَرۡف جَزۡم"),
    NASB_PARTICLE(R.string.learning_role_nasb_particle, "حَرۡف نَصۡب"),
    QUESTION(R.string.learning_role_question, "حَرۡف اسۡتِفۡهَام"),
    NEGATION(R.string.learning_role_negation, "حَرۡف نَفۡي"),
    CALLING(R.string.learning_role_calling, "حَرۡف نِدَاء"),
    CALLED(R.string.learning_role_called, "مُنَادًى"),
    EXCEPT(R.string.learning_role_except, "أَدَاة اسۡتِثۡنَاء"),
    EXCEPTED(R.string.learning_role_excepted, "مُسۡتَثۡنًى"),
    LA_WHOLE_KIND(R.string.learning_role_la_whole_kind, "لَا النَّافِيَة لِلۡجِنۡس"),
    LA_NOUN(R.string.learning_role_la_noun, "اسۡم لَا"),
    CERTAINTY(R.string.learning_role_certainty, "حَرۡف تَحۡقِيق"),
    EMPHASIS(R.string.learning_role_emphasis, "تَوۡكِيد"),
    EXTRA(R.string.learning_role_extra, "حَرۡف زَائِد"),
}

/** The case or mood a word is in: rafʿ, naṣb, jarr for nouns, and jazm for verbs. */
enum class CaseMood(@StringRes val nameRes: Int) {
    RAF(R.string.learning_case_raf),
    NASB(R.string.learning_case_nasb),
    JARR(R.string.learning_case_jarr),
    JAZM(R.string.learning_case_jazm),
}

/** What shows the case: a vowel, a letter, or something dropped. Implied signs are not written. */
enum class CaseSign(@StringRes val nameRes: Int) {
    DAMMA(R.string.learning_sign_damma),
    FATHA(R.string.learning_sign_fatha),
    KASRA(R.string.learning_sign_kasra),
    SUKUN(R.string.learning_sign_sukun),
    WAW(R.string.learning_sign_waw),
    YA(R.string.learning_sign_ya),
    ALIF(R.string.learning_sign_alif),
    NUN(R.string.learning_sign_nun),
    DROPPED_NUN(R.string.learning_sign_dropped_nun),
    DROPPED_LETTER(R.string.learning_sign_dropped_letter),
    FATHA_FOR_JARR(R.string.learning_sign_fatha_for_jarr),
    IMPLIED(R.string.learning_sign_implied),
}

/**
 * One part of a word with a role: the prefix or suffix it is on ([part], MASAQ's unvowelled
 * letters, null for the word's stem), and the case it is in when it has one.
 *
 * A word with a [fixedEnding] (a pronoun, a relative, هَٰذَا) never changes its ending: it is
 * in the *place* of its case, and has no sign.
 */
data class WordRole(
    val part: String?,
    val role: SentenceRole,
    val case: CaseMood?,
    val sign: CaseSign?,
    val fixedEnding: Boolean = false,
)

object SentenceRoles {
    private val ROLES: Map<String, SentenceRole> = mapOf(
        "SUBJ" to SentenceRole.SUBJECT,
        "SUBJ_DELA" to SentenceRole.SUBJECT_AFTER,
        "PRED" to SentenceRole.PREDICATE,
        "IV" to SentenceRole.PRESENT_VERB,
        "IV_PASS" to SentenceRole.PRESENT_VERB,
        "CV" to SentenceRole.COMMAND_VERB,
        "AGNT" to SentenceRole.DOER,
        "PASS_SUBJ" to SentenceRole.STAND_IN_DOER,
        "OBJ" to SentenceRole.OBJECT,
        "PREP" to SentenceRole.PREPOSITION,
        "PREP_OBJ" to SentenceRole.AFTER_PREPOSITION,
        "GEN_CONS" to SentenceRole.OWNER,
        "ADJ" to SentenceRole.ADJECTIVE,
        "APPOS" to SentenceRole.SUBSTITUTE,
        "CONJ" to SentenceRole.JOINING,
        "CONJ_N" to SentenceRole.JOINED,
        "ANNUL_PART" to SentenceRole.INNA,
        "SUBOR_ANN_CONJ" to SentenceRole.INNA,
        "SUBJ_COP_PART" to SentenceRole.INNA_SUBJECT,
        "PART_COP_PRED" to SentenceRole.INNA_PREDICATE,
        "IV_COP" to SentenceRole.KANA,
        "CV_COP" to SentenceRole.KANA,
        "SUBJ_COP_V" to SentenceRole.KANA_SUBJECT,
        "V_COP_PRED" to SentenceRole.KANA_PREDICATE,
        "ADV_TIME" to SentenceRole.TIME,
        "ADV_PLCE" to SentenceRole.PLACE,
        "CIRCUM" to SentenceRole.HAL,
        "ACC_SPECIF" to SentenceRole.TAMYIZ,
        "COGN" to SentenceRole.ABSOLUTE_OBJECT,
        "SUBS_COG_ACC" to SentenceRole.ABSOLUTE_OBJECT,
        "PURP" to SentenceRole.REASON,
        "PART_CONDITION" to SentenceRole.CONDITION,
        "PART_JUSSIVE" to SentenceRole.JAZM_PARTICLE,
        "SUBJUNC_PART" to SentenceRole.NASB_PARTICLE,
        "SUBOR_CONJ" to SentenceRole.NASB_PARTICLE,
        "PART_INTERROG" to SentenceRole.QUESTION,
        "NEG_MAA" to SentenceRole.NEGATION,
        "VOC_PART" to SentenceRole.CALLING,
        "VOC" to SentenceRole.CALLED,
        "PART_EXCEPT" to SentenceRole.EXCEPT,
        "EXCP" to SentenceRole.EXCEPTED,
        "NEG_CAT" to SentenceRole.LA_WHOLE_KIND,
        "SUBJ_NEG_CAT" to SentenceRole.LA_NOUN,
        "CERT_PART" to SentenceRole.CERTAINTY,
        "INTENCIF" to SentenceRole.EMPHASIS,
        "EXPLET" to SentenceRole.EXTRA,
    )

    private val CASES = mapOf(
        "NOMINATIVE" to CaseMood.RAF,
        "ACCUSATIVE" to CaseMood.NASB,
        "GENITIVE" to CaseMood.JARR,
        "JUSSIVE" to CaseMood.JAZM,
    )

    private val SIGNS = mapOf(
        "DHAMMA" to CaseSign.DAMMA,
        "FATHA" to CaseSign.FATHA,
        "KASRA" to CaseSign.KASRA,
        "SUKUN" to CaseSign.SUKUN,
        "WAW" to CaseSign.WAW,
        "YAA" to CaseSign.YA,
        "ALIF" to CaseSign.ALIF,
        "NUN" to CaseSign.NUN,
        "DEL_NUN" to CaseSign.DROPPED_NUN,
        "DEL_VOWEL" to CaseSign.DROPPED_LETTER,
        "FATHA_DIPTOTE" to CaseSign.FATHA_FOR_JARR,
        "IMP_DHAMMA" to CaseSign.IMPLIED,
        "IMP_FATHA" to CaseSign.IMPLIED,
        "IMP_KASRA" to CaseSign.IMPLIED,
    )

    /**
     * The roles of one word's parts, in reading order. Parts with no role a learner would
     * recognise are left out, as is MASAQ's analysis of what isn't written (a hidden pronoun).
     */
    fun of(syntax: List<SyntaxEntity>): List<WordRole> = syntax.mapNotNull { segment ->
        if (segment.morphType != "Prefix" && segment.morphType != "Stem" && segment.morphType != "Suffix") return@mapNotNull null
        val role = segment.role?.let(ROLES::get) ?: fallback(segment) ?: return@mapNotNull null
        val case = CASES[segment.caseMood]
        // MASAQ marks words whose ending changes as DECLN; the others keep theirs.
        val fixed = case != null && segment.declinability != "DECLN"
        WordRole(
            part = segment.text.takeIf { segment.morphType != "Stem" && it.isNotBlank() && it != "(null)" },
            role = role,
            case = case,
            // A sign only means something with a case: an invariable word's sukūn isn't one.
            sign = if (case == null || fixed) null else SIGNS[segment.caseMarker],
            fixedEnding = fixed,
        )
    }

    /** MASAQ gives past verbs and negating particles no role, but their tags say what they are. */
    private fun fallback(segment: SyntaxEntity): SentenceRole? = when {
        segment.morphType != "Stem" -> null
        segment.morphTag == "PV" || segment.morphTag == "PV_PASS" -> SentenceRole.PAST_VERB
        segment.morphTag == "NEG_PART" -> SentenceRole.NEGATION
        else -> null
    }
}
