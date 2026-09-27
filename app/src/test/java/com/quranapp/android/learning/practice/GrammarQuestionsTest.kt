package com.quranapp.android.learning.practice

import com.quranapp.android.learning.concepts.GrammarIds
import com.quranapp.android.learning.examples.AyahWords
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Grammar questions use the pack's grammar of each ayah, not the text's marks. */
class GrammarQuestionsTest {
    // 1:5 إِيَّاكَ نَعۡبُدُ وَإِيَّاكَ نَسۡتَعِينُ, with what the pack finds in it (a few concepts).
    private val ayah1_5 = GrammarAyah(
        AyahWords(1, 5, listOf("إِيَّاكَ", "نَعۡبُدُ", "وَإِيَّاكَ", "نَسۡتَعِينُ", "٥")),
        mapOf(
            GrammarIds.ISM to listOf(0, 2),
            GrammarIds.FIL to listOf(1, 3),
            GrammarIds.HARF to listOf(2),
            GrammarIds.IYYA to listOf(0, 2),
            GrammarIds.PRESENT to listOf(1, 3),
            GrammarIds.HIDDEN_DOER to listOf(1, 3),
            GrammarIds.FORMS_7_10 to listOf(3),
        ),
    )

    @Test
    fun tapTheWordWithTheConcept() {
        val question = ConceptQuestions.tapWord(GrammarIds.FORMS_7_10, ayah1_5.ayah, ayah1_5.wordsByConcept)!!
        assertEquals("نَسۡتَعِينُ is form X", setOf(3), question.answers)
        assertEquals(4, question.words.size)
    }

    @Test
    fun theOtherOptionsAreNotOnTheWord() {
        repeat(10) { seed ->
            val question = ConceptQuestions.ruleOnWord(GrammarIds.FIL, ayah1_5.ayah, Random(seed), ayah1_5.wordsByConcept) ?: return@repeat
            val onWord = ayah1_5.wordsByConcept.filterValues { question.highlighted in it }.keys
            assertEquals(GrammarIds.FIL, question.options[question.answerIndex])
            question.options.filter { it != GrammarIds.FIL }.forEach { assertFalse("$it is on the word too", it in onWord) }
        }
    }

    @Test
    fun aPresentVerbCanBeTakenForAPastOne() {
        // The past is what the present builds on, but it isn't on نَعۡبُدُ: a fair wrong option.
        val questions = (0 until 20).mapNotNull { ConceptQuestions.ruleOnWord(GrammarIds.PRESENT, ayah1_5.ayah, Random(it), ayah1_5.wordsByConcept) }
        assertTrue("a present verb makes rule questions", questions.isNotEmpty())
        assertTrue(questions.any { GrammarIds.PAST in it.options })
        questions.forEach { question ->
            assertFalse("the hidden doer is on the word too", GrammarIds.HIDDEN_DOER in question.options)
        }
    }

    @Test
    fun theFactoryAsksThePackForGrammar() = runBlocking {
        val asked = mutableListOf<String>()
        val factory = QuestionFactory(
            lemmaWithPool = { null },
            ayahsWithConcept = { _, _ -> error("grammar doesn't search the text") },
            random = Random(1),
            grammarAyahs = { conceptId, _ -> asked += conceptId; listOf(ayah1_5) },
        )
        val questions = factory.questionsFor(GrammarIds.PRESENT, 2)
        assertEquals(listOf(GrammarIds.PRESENT), asked)
        assertTrue(questions.isNotEmpty())
        assertTrue(questions.all { it.itemId == GrammarIds.PRESENT })
        // Without the pack there is nothing to ask.
        val noPack = QuestionFactory(lemmaWithPool = { null }, ayahsWithConcept = { _, _ -> emptyList() })
        assertEquals(emptyList<Question>(), noPack.questionsFor(GrammarIds.PRESENT, 2))
    }
}
