package com.quranapp.android.learning.practice

import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.analysis.TestAyahs
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.examples.AyahWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ConceptQuestionsTest {
    // Al-Falaq 113:3 وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ, with the ayah number as the app stores it.
    private val falaq3 = AyahWords(113, 3, TestAyahs.AYAH_113_3 + "٣")

    @Test
    fun tapWord_acceptsEveryWordWithTheRule() {
        val question = ConceptQuestions.tapWord(ConceptIds.IKHFA, falaq3)!!
        val expected = AyahAnalyzer.analyze(falaq3.words).wordsByConcept.getValue(ConceptIds.IKHFA).toSet()
        assertEquals(expected, question.answers)
        assertTrue(0 in question.answers) // the noon of وَمِن
        assertEquals(5, question.words.size) // without the ayah number
    }

    @Test
    fun tapWord_skipsAyahsWhereTheRuleIsAlmostEverywhere() {
        // Short vowels are in every word: tapping any word would be right.
        assertNull(ConceptQuestions.tapWord(ConceptIds.SHORT_VOWELS, falaq3))
        assertNull(ConceptQuestions.tapWord(ConceptIds.IKHFA, AyahWords(112, 1, TestAyahs.AYAH_112_1)))
    }

    @Test
    fun ruleOnWord_offersSiblingRulesThatAreNotOnTheWord() {
        repeat(10) { seed ->
            val question = ConceptQuestions.ruleOnWord(ConceptIds.IKHFA, falaq3, Random(seed))!!
            assertEquals(ConceptIds.IKHFA, question.options[question.answerIndex])
            val onWord = AyahAnalyzer.analyze(falaq3.words).wordsByConcept.filterValues { question.highlighted in it }.keys
            question.options.filter { it != ConceptIds.IKHFA }.forEach { assertFalse(it in onWord) }
        }
    }

    @Test
    fun siblings_areTheOtherRulesOfTheSameFamily() {
        val siblings = ConceptQuestions.siblings(ConceptIds.IKHFA)
        assertTrue(ConceptIds.IZHAR in siblings)
        assertTrue(ConceptIds.IQLAB in siblings)
        assertFalse("umbrellas are never options", siblings.any { it in ConceptCatalog.umbrellaIds })
    }
}
