package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.GrammarIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** MASAQ rows and corpus segments copied from learning pack v2. */
class SentenceDetectorTest {
    private fun stem(tag: String, role: String?, construct: String? = null, marker: String? = null, phrase: String? = null, function: String? = null) =
        SyntaxSegment("Stem", tag, role, construct, marker, phrase, function)

    private fun prefix(tag: String, role: String? = null, phrase: String? = null, function: String? = null) =
        SyntaxSegment("Prefix", tag, role, phrase = phrase, phraseFunction = function)

    private fun form(tag: String, features: String) = GrammarSegment("STEM", tag, features.split('|').filter { it.isNotEmpty() }, "")

    // 1:2 ٱلۡحَمۡدُ لِلَّهِ رَبِّ ٱلۡعَٰلَمِينَ
    private val ayah1_2 = listOf(
        listOf(prefix("DET"), stem("GERUND", "SUBJ", marker = "DHAMMA")),
        listOf(prefix("PREP", "PREP", "PHRASE", "PRED"), stem("NOUN_PROP", "PREP_OBJ", marker = "KASRA")),
        listOf(stem("NOUN_CONCRETE", "ADJ", "CONSTRUCT", "KASRA")),
        listOf(prefix("DET"), stem("NOUN_ABSTRACT", "GEN_CONS", marker = "YAA")),
    )

    @Test
    fun alHamduLillah() {
        val found = SentenceDetector.detect(ayah1_2, emptyList())
        assertEquals("رَبِّ ٱلۡعَٰلَمِينَ", listOf(2, 3), found[GrammarIds.IDAFA])
        assertEquals("ٱلۡحَمۡدُ is the subject, لِلَّهِ the predicate", listOf(0, 1), found[GrammarIds.MUBTADA_KHABAR])
        assertEquals("the predicate is a phrase", listOf(1), found[GrammarIds.KHABAR_PHRASE])
        assertNull(found[GrammarIds.KHABAR_FIRST])
    }

    @Test
    fun comparativesAndTwoIdafas() {
        // 97:3 لَيۡلَةُ ٱلۡقَدۡرِ خَيۡرٞ مِّنۡ أَلۡفِ شَهۡرٖ
        val ayah = listOf(
            listOf(stem("NOUN_ABSTRACT", "SUBJ", "CONSTRUCT", "DHAMMA")),
            listOf(prefix("DET"), stem("NOUN_PROP", "GEN_CONS", marker = "KASRA")),
            listOf(stem("ADJ_COMP", "PRED", marker = "DHAMMA")),
            listOf(stem("PREP", null)),
            listOf(stem("NOUN_NUM", "PREP_OBJ", "CONSTRUCT", "KASRA")),
            listOf(stem("NOUN_ABSTRACT", "GEN_CONS", marker = "KASRA")),
        )
        val found = SentenceDetector.detect(ayah, emptyList())
        assertEquals(listOf(0, 1, 4, 5), found[GrammarIds.IDAFA])
        assertEquals("خَيۡرٞ “better”", listOf(2), found[GrammarIds.ELATIVE])
        assertEquals(listOf(0, 2), found[GrammarIds.MUBTADA_KHABAR])
    }

    @Test
    fun thePredicatePlacedFirst() {
        // 79:43 فِيمَ أَنتَ: the predicate phrase, then the subject.
        val ayah = listOf(
            listOf(SyntaxSegment("Stem", "PREP", null, phrase = "PHRASE", phraseFunction = "PRED")),
            listOf(stem("PRON_2MS", "SUBJ_DELA")),
        )
        assertEquals(listOf(0, 1), SentenceDetector.detect(ayah, emptyList())[GrammarIds.KHABAR_FIRST])
        // 84:23 وَٱللَّهُ أَعۡلَمُ: MASAQ marks ٱللَّهُ as a delayed subject, but nothing comes before it.
        val noPredicateBefore = listOf(listOf(stem("NOUN_PROP", "SUBJ_DELA")), listOf(stem("ADJ_COMP", "PRED")))
        assertNull(SentenceDetector.detect(noPredicateBefore, emptyList())[GrammarIds.KHABAR_FIRST])
    }

    @Test
    fun objectsTamyizAndHiddenDoers() {
        // 99:7 فَمَن يَعۡمَلۡ مِثۡقَالَ ذَرَّةٍ خَيۡرٗا يَرَهُۥ
        val syntax = listOf(
            listOf(stem("INTERROG_PART", "SUBJ")),
            listOf(stem("IV", "IV")),
            listOf(stem("NOUN_ABSTRACT", "OBJ", "CONSTRUCT", "FATHA")),
            listOf(stem("NOUN_CONCRETE", "GEN_CONS", marker = "KASRA")),
            listOf(stem("GERUND", "ACC_SPECIF", marker = "FATHA")),
            listOf(stem("IV", "IV")),
        )
        val forms = listOf(
            listOf(form("COND", "")),
            listOf(form("V", "IMPF|3MS|MOOD:JUS")),
            listOf(form("N", "M|ACC")),
            listOf(form("N", "F|INDEF|GEN")),
            listOf(form("N", "M|INDEF|ACC")),
            listOf(form("V", "IMPF|3MS|MOOD:JUS")),
        )
        val found = SentenceDetector.detect(syntax, forms)
        assertEquals("مِثۡقَالَ is the object", listOf(2), found[GrammarIds.VERB_DOER_OBJECT])
        assertEquals("خَيۡرٗا says what the weight is of", listOf(4), found[GrammarIds.TAMYIZ])
        assertEquals("both verbs’ “he” is hidden", listOf(1, 5), found[GrammarIds.HIDDEN_DOER])
    }

    @Test
    fun hiddenDoersOfWeAndOfTheCommand() {
        // 1:5 إِيَّاكَ نَعۡبُدُ and 112:1 قُلۡ
        val forms = listOf(listOf(form("PRON", "2MS")), listOf(form("V", "IMPF|1P")), listOf(form("V", "IMPV|2MS")))
        assertEquals(listOf(1, 2), SentenceDetector.detect(emptyList(), forms)[GrammarIds.HIDDEN_DOER])
        // قَالُوا: the doer is the written ـوا, and a passive verb has no doer at all.
        val written = listOf(listOf(form("V", "PERF|3MP")), listOf(form("V", "PERF|PASS|3MS")))
        assertNull(SentenceDetector.detect(emptyList(), written)[GrammarIds.HIDDEN_DOER])
    }

    @Test
    fun agreementWithTheDoerAfterTheVerb() {
        // 79:6 يَوۡمَ تَرۡجُفُ ٱلرَّاجِفَةُ: a feminine verb for a feminine doer.
        val syntax = listOf(listOf(stem("ADV", "ADV_TIME", "CONSTRUCT")), listOf(stem("IV", "IV")), listOf(stem("NOUN_ABSTRACT", "AGNT")))
        val forms = listOf(listOf(form("T", "M|ACC")), listOf(form("V", "IMPF|3FS")), listOf(form("N", "ACT|PCPL|F|NOM")))
        val found = SentenceDetector.detect(syntax, forms)
        assertEquals(listOf(1, 2), found[GrammarIds.VERB_AGREEMENT])
        assertEquals(listOf(0), found[GrammarIds.TIME_PLACE])
        assertFalse("the doer is written", 1 in found[GrammarIds.HIDDEN_DOER].orEmpty())
        // A masculine singular doer after a masculine verb shows nothing new: وَجَآءَ رَبُّكَ.
        val plain = listOf(listOf(form("V", "PERF|3MS")), listOf(form("N", "M|NOM")))
        val plainSyntax = listOf(listOf(stem("PV", "PV")), listOf(stem("NOUN_CONCRETE", "AGNT")))
        assertNull(SentenceDetector.detect(plainSyntax, plain)[GrammarIds.VERB_AGREEMENT])
    }

    @Test
    fun wordShapesMasaqNames() {
        val ayah = listOf(
            listOf(stem("NOUN_PROP_FOREIGN", "PREP_OBJ", marker = "FATHA_DIPTOTE")), // إِبۡرَٰهِيمَ after a preposition
            listOf(stem("NOUN_TIME_PLACE", "OBJ")), // مَعَاشٗا
            listOf(stem("ADJ_INTENS", "ADJ")), // وَهَّاجٗا
            listOf(stem("GERUND", "COGN")), // جَزَآءٗ
            listOf(stem("NOUN_ABSTRACT", "PURP")), // مَّتَٰعٗا
            listOf(stem("NOUN_CONCRETE", "CIRCUM")), // أَفۡوَاجٗا
        )
        val found = SentenceDetector.detect(ayah, emptyList())
        assertEquals(listOf(0), found[GrammarIds.DIPTOTE])
        assertEquals(listOf(1, 2), found[GrammarIds.PLACE_TIME_INTENSIVE])
        assertEquals(listOf(3), found[GrammarIds.ABSOLUTE_OBJECT])
        assertEquals(listOf(4), found[GrammarIds.OBJECT_OF_REASON])
        assertEquals(listOf(5), found[GrammarIds.HAL])
        assertTrue(1 in found[GrammarIds.VERB_DOER_OBJECT].orEmpty())
    }
}
