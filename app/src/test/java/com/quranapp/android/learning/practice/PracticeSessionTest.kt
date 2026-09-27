package com.quranapp.android.learning.practice

import com.quranapp.android.learning.analysis.TestAyahs
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.WordLocation
import com.quranapp.android.learning.progress.ReviewRating
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PracticeSessionTest {
    private fun question(itemId: String) = ChoiceQuestion(itemId, ChoiceKind.MEANING_OF_WORD, "p", listOf("a", "b"), 0)

    @Test
    fun anItemIsGradedAfterItsLastQuestion() {
        val session = PracticeSession(listOf(PracticeItem("word.Eabada", List(3) { question("word.Eabada") })))
        assertNull(session.answer(true))
        assertNull(session.answer(true))
        val result = session.answer(true)!!
        assertEquals(ItemResult("word.Eabada", ReviewRating.GOOD, 3, 3), result)
        assertTrue(session.isFinished)
        assertEquals(3, session.rightCount)
    }

    @Test
    fun aWrongAnswerInAThreeQuestionCheckFailsIt() {
        val session = PracticeSession(listOf(PracticeItem("x", List(3) { question("x") })))
        session.answer(true)
        session.answer(false)
        assertEquals(ReviewRating.AGAIN, session.answer(true)!!.rating)
    }

    @Test
    fun threeOfFourPasses() {
        assertTrue(PracticeSession.passes(3, 4))
        assertFalse(PracticeSession.passes(2, 3))
        assertTrue(PracticeSession.passes(1, 1))
        assertFalse(PracticeSession.passes(0, 1))
    }

    @Test
    fun anIntroductionIsAcknowledgedAndNotGraded() {
        val intro = WordIntroduction("word.Eabada", "عَبَدَ", "worship", 122)
        val session = PracticeSession(listOf(PracticeItem("word.Eabada", listOf(intro) + List(3) { question("word.Eabada") })))
        assertEquals(4, session.size)
        assertEquals(3, session.gradedCount)
        assertThrows(IllegalStateException::class.java) { session.answer(true) }
        session.acknowledge()
        assertEquals("the introduction isn't counted as a question", 0, session.gradedBefore)
        assertNull(session.answer(true))
        assertEquals(1, session.gradedBefore)
        assertNull(session.answer(true))
        assertEquals(ItemResult("word.Eabada", ReviewRating.GOOD, 3, 3), session.answer(true))
    }

    @Test
    fun eachReviewItemIsGradedOnItsOwn() {
        val session = PracticeSession(listOf(PracticeItem("a", listOf(question("a"))), PracticeItem("b", listOf(question("b")))))
        assertEquals(ReviewRating.GOOD, session.answer(true)!!.rating)
        assertEquals(ReviewRating.AGAIN, session.answer(false)!!.rating)
    }
}

class QuestionFactoryTest {
    private val worship = LemmaEntity(1, "Eabada", "HW", "corpus", "V", null, null, 10, "worship")
    private val pool = listOf("create", "say", "see", "hear").mapIndexed { i, gloss ->
        LemmaEntity(i + 2, "k$i", "HW$i", "corpus", "V", null, null, 10, gloss)
    }
    private val falaq3 = AyahWords(113, 3, TestAyahs.AYAH_113_3)
    private val factory = QuestionFactory(
        lemmaWithPool = { key -> if (key == "Eabada") worship to pool else null },
        ayahsWithConcept = { _, _ -> List(5) { falaq3 } },
        random = Random(3),
    )

    @Test
    fun wordItemsAlternateBothDirections() = runBlocking {
        val kinds = factory.questionsFor("word.Eabada", 3).map { (it as ChoiceQuestion).kind }
        assertEquals(listOf(ChoiceKind.MEANING_OF_WORD, ChoiceKind.WORD_FOR_MEANING, ChoiceKind.MEANING_OF_WORD), kinds)
    }

    @Test
    fun conceptItemsGetTapAndRuleQuestions() = runBlocking {
        val questions = factory.questionsFor(ConceptIds.IKHFA, 2)
        assertEquals(2, questions.size)
        assertTrue(questions.all { it.itemId == ConceptIds.IKHFA })
    }

    @Test
    fun introductionsShowTheDictionaryFormAndMeaning() = runBlocking {
        assertEquals(WordIntroduction("word.Eabada", "HW", "worship", 10), factory.introductionFor("word.Eabada"))
        // With where it first occurs, so it can be heard.
        val withPlace = QuestionFactory(
            lemmaWithPool = { worship to pool },
            ayahsWithConcept = { _, _ -> emptyList() },
            firstPlace = { lemma -> WordLocation(ayahId = 1005, wordIndex = lemma.occurrences - 9) },
        )
        assertEquals(WordLocation(1005, 1), withPlace.introductionFor("word.Eabada")?.firstPlace)
        assertNull("concepts have lessons instead", factory.introductionFor(ConceptIds.IKHFA))
    }

    @Test
    fun unknownItemsGetNoQuestions() = runBlocking {
        assertEquals(emptyList<Question>(), factory.questionsFor("word.missing", 3))
        assertEquals(emptyList<Question>(), factory.questionsFor("grammar.idafa", 3))
    }
}
