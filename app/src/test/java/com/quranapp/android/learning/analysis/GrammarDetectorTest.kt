package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.GrammarIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Segments copied from learning pack v2 (the corpus's analysis of the app's words). */
class GrammarDetectorTest {
    private fun seg(kind: String, tag: String, features: String, form: String = "", lemma: String? = null, root: String? = null) =
        GrammarSegment(kind, tag, features.split('|').filter { it.isNotEmpty() }, form, lemma, root)

    // 1:5 إِيَّاكَ نَعۡبُدُ وَإِيَّاكَ نَسۡتَعِينُ
    private val ayah1_5 = listOf(
        listOf(seg("STEM", "PRON", "2MS", "إِيَّاكَ", "<iy~aA")),
        listOf(seg("STEM", "V", "IMPF|1P", "نَعۡبُدُ", "Eabada", "Ebd")),
        listOf(seg("PREFIX", "CONJ", "w:CONJ+", "وَ"), seg("STEM", "PRON", "2MS", "إِيَّاكَ", "<iy~aA")),
        listOf(seg("STEM", "V", "IMPF|(X)|1P", "نَسۡتَعِينُ", "{sotaEaAna", "Ewn")),
    )

    // 1:6 ٱهۡدِنَا ٱلصِّرَٰطَ ٱلۡمُسۡتَقِيمَ and 1:7's أَنۡعَمۡتَ and ٱلضَّآلِّينَ
    private val hdina = listOf(seg("STEM", "V", "IMPV|2MS", "ٱهۡدِ", "hadaY", "hdy"), seg("SUFFIX", "PRON", "PRON:1P", "نَا"))
    private val mustaqim = listOf(seg("PREFIX", "DET", "Al+", "ٱلۡ"), seg("STEM", "ADJ", "ACT|PCPL|(X)|M|ACC", "مُسۡتَقِيمَ", "musotaqiym", "qwm"))
    private val anamta = listOf(seg("STEM", "V", "PERF|(IV)|2MS", "أَنۡعَمۡ", ">anoEama", "nEm"), seg("SUFFIX", "PRON", "PRON:2MS", "تَ"))
    private val dallin = listOf(seg("PREFIX", "DET", "Al+", "ٱل"), seg("STEM", "N", "ACT|PCPL|MP|GEN", "ضَّآلِّينَ", "DaA^l~", "Dll"))

    private fun conceptsOf(vararg words: List<GrammarSegment>, word: Int = 0) =
        GrammarDetector.detect(words.toList()).filterValues { word in it }.keys

    @Test
    fun wordTypes() {
        val found = GrammarDetector.detect(ayah1_5)
        assertEquals(listOf(0, 2), found[GrammarIds.ISM]) // the two إِيَّاكَ
        assertEquals(listOf(1, 3), found[GrammarIds.FIL])
        assertEquals("the وَ of وَإِيَّاكَ", listOf(2), found[GrammarIds.HARF])
    }

    @Test
    fun iyyaVerbsAndForms() {
        val found = GrammarDetector.detect(ayah1_5)
        assertEquals(listOf(0, 2), found[GrammarIds.IYYA])
        assertEquals(listOf(1, 3), found[GrammarIds.PRESENT])
        assertEquals("نَسۡتَعِينُ is form X", listOf(3), found[GrammarIds.FORMS_7_10])
        assertEquals("ع و ن is hollow", listOf(3), found[GrammarIds.HOLLOW])
        assertEquals(listOf(1, 3), found[GrammarIds.ROOT])
        assertEquals(listOf(1, 3), found[GrammarIds.PATTERN])
    }

    @Test
    fun theDoerInsideTheVerbIsNotItsObject() {
        assertTrue(GrammarIds.DOER_IN_VERB in conceptsOf(anamta)) // ـتَ is "you" who favoured
        assertTrue(GrammarIds.PAST in conceptsOf(anamta))
        val guide = conceptsOf(hdina)
        assertFalse("نَا in ٱهۡدِنَا is the object", GrammarIds.DOER_IN_VERB in guide)
        assertTrue(GrammarIds.COMMAND in guide)
        assertTrue(GrammarIds.ATTACHED_PRONOUN in guide)
        assertTrue("ه د ي is defective", GrammarIds.DEFECTIVE in guide)
    }

    @Test
    fun nounsAndParticiples() {
        val straight = conceptsOf(mustaqim)
        assertTrue(GrammarIds.ADJECTIVE in straight)
        assertTrue(GrammarIds.ACTIVE_PARTICIPLE in straight)
        assertTrue(GrammarIds.DEFINITENESS in straight)
        assertTrue(GrammarIds.CASES in straight)
        val astray = conceptsOf(dallin)
        assertTrue("ضَّآلِّينَ ends in ـِينَ", GrammarIds.SOUND_MASC_PLURAL in astray)
        assertTrue(GrammarIds.DUAL_PLURAL_ENDINGS in astray)
        assertFalse(GrammarIds.BROKEN_PLURAL in astray)
    }

    @Test
    fun brokenPluralsAndTheFiveNouns() {
        val rijal = listOf(seg("STEM", "N", "MP|NOM", "رِجَالٌ", "rajul", "rjl"))
        assertTrue(GrammarIds.BROKEN_PLURAL in conceptsOf(rijal))
        val abuhu = listOf(seg("STEM", "N", "M|NOM", "أَبُو", ">abN", "Abw"), seg("SUFFIX", "PRON", "PRON:3MS", "هُ"))
        val found = conceptsOf(abuhu)
        assertTrue(GrammarIds.FIVE_NOUNS in found)
        assertTrue("his father", GrammarIds.PRONOUN_POSSESSOR in found)
    }

    @Test
    fun theQuransSpellingAndPluralsWithoutAGender() {
        // 103:3 ٱلصَّٰلِحَٰتِ: the ā of ـَٰت is a dagger alif.
        val salihat = listOf(seg("PREFIX", "DET", "Al+", "ٱل"), seg("STEM", "N", "ACT|PCPL|FP|ACC", "صَّٰلِحَٰتِ", "S~a`liHa`t", "SlH"))
        assertTrue(GrammarIds.SOUND_FEM_PLURAL in conceptsOf(salihat))
        assertFalse(GrammarIds.BROKEN_PLURAL in conceptsOf(salihat))
        // 110:2 أَفۡوَاجًا is P: a plural, gender not given.
        val afwaj = listOf(seg("STEM", "N", "P|INDEF|ACC", "أَفۡوَاجًا", "fawoj", "fwj"))
        assertTrue(GrammarIds.BROKEN_PLURAL in conceptsOf(afwaj))
        // 111:1 أَبِى, with the pack's final ى.
        val abi = listOf(seg("STEM", "N", "MS|GEN", "أَبِى", ">abN", "Abw"))
        assertTrue(GrammarIds.FIVE_NOUNS in conceptsOf(abi))
    }

    @Test
    fun laDenyingAWholeKind() {
        // 2:2 … لَا رَيۡبَۛ فِيهِۛ: لَا, then رَيۡبَ in naṣb with neither ال nor tanwīn.
        val la = listOf(seg("STEM", "NEG", "", "لَا", "laA"))
        val rayba = listOf(seg("STEM", "N", "M|ACC", "رَيۡبَ", "rayob", "ryb"))
        val found = GrammarDetector.detect(listOf(la, rayba))
        assertEquals(listOf(0, 1), found[GrammarIds.LA_GENERIC])
        assertEquals(listOf(0), found[GrammarIds.LA])
        // With tanwīn it is an ordinary "no": لَا بَيۡعٌ (2:254).
        val bayun = listOf(seg("STEM", "N", "M|INDEF|NOM", "بَيۡعٌ", "bayoE", "byE"))
        assertFalse(GrammarIds.LA_GENERIC in GrammarDetector.detect(listOf(la, bayun)))
    }
}
