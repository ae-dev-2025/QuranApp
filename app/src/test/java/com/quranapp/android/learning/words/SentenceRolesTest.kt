package com.quranapp.android.learning.words

import com.quranapp.android.learning.pack.SyntaxEntity
import org.junit.Assert.assertEquals
import org.junit.Test

/** MASAQ rows copied from learning pack v2. */
class SentenceRolesTest {
    private fun row(type: String, tag: String, role: String?, case: String? = null, marker: String? = null, text: String = "", declinability: String? = "DECLN") =
        SyntaxEntity(0, 0, 0, text, tag, type, declinability, role, null, case, marker, null, null)

    @Test
    fun alHamduLillah() {
        // ٱلۡحَمۡدُ: the article has no role; the stem is the subject, in rafʿ with a ḍamma.
        val alhamdu = SentenceRoles.of(listOf(row("Prefix", "DET", null, text = "ال"), row("Stem", "GERUND", "SUBJ", "NOMINATIVE", "DHAMMA")))
        assertEquals(listOf(WordRole(null, SentenceRole.SUBJECT, CaseMood.RAF, CaseSign.DAMMA)), alhamdu)

        // لِلَّهِ: the لِ is a preposition, and ٱللَّهِ is in jarr after it.
        val lillahi = SentenceRoles.of(
            listOf(row("Prefix", "PREP", "PREP", "INVARIABLE", "KASRA", "ل"), row("Stem", "NOUN_PROP", "PREP_OBJ", "GENITIVE", "KASRA")),
        )
        assertEquals(
            listOf(
                WordRole("ل", SentenceRole.PREPOSITION, null, null), // invariable: its kasra isn't a case sign
                WordRole(null, SentenceRole.AFTER_PREPOSITION, CaseMood.JARR, CaseSign.KASRA),
            ),
            lillahi,
        )

        // ٱلۡعَٰلَمِينَ: the owner in رَبِّ ٱلۡعَٰلَمِينَ, its jarr shown by a yāʾ.
        val alamin = SentenceRoles.of(listOf(row("Stem", "NOUN_ABSTRACT", "GEN_CONS", "GENITIVE", "YAA"), row("Suffix", "NSUFF_MASC_PL_GEN", null)))
        assertEquals(listOf(WordRole(null, SentenceRole.OWNER, CaseMood.JARR, CaseSign.YA)), alamin)
    }

    @Test
    fun aPronounIsInThePlaceOfItsCase() {
        // 1:5 إِيَّاكَ: the object, but a pronoun's ending never changes, so its fatḥa is no sign.
        val iyyaka = SentenceRoles.of(listOf(row("Stem", "PRON", "OBJ", "ACCUSATIVE", "FATHA", declinability = "DISJ_INV_PRON")))
        assertEquals(listOf(WordRole(null, SentenceRole.OBJECT, CaseMood.NASB, null, fixedEnding = true)), iyyaka)
    }

    @Test
    fun pastVerbsAndNegationHaveNoRoleButATag() {
        assertEquals(SentenceRole.PAST_VERB, SentenceRoles.of(listOf(row("Stem", "PV", null))).single().role)
        assertEquals(SentenceRole.NEGATION, SentenceRoles.of(listOf(row("Stem", "NEG_PART", "NON_INFLECT", "INVARIABLE"))).single().role)
    }

    @Test
    fun whatIsNotWrittenIsLeftOut() {
        // MASAQ's analysis of a hidden pronoun, and roles a learner wouldn't recognise.
        assertEquals(emptyList<WordRole>(), SentenceRoles.of(listOf(row("Other_i3rab", "None", "AGNT", "NOMINATIVE", "SUKUN"))))
        assertEquals(emptyList<WordRole>(), SentenceRoles.of(listOf(row("Stem", "REL_PRON", "COMPL", "INVARIABLE"))))
    }
}
