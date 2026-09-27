package com.quranapp.android.learning.practice

import com.quranapp.android.learning.letters.LetterExample
import com.quranapp.android.learning.letters.Letters
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LetterQuestionsTest {
    private val ba = Letters["letter.ba"]!!
    private val random = Random(7)

    @Test
    fun nameOfLetter_offersLookAlikesFromItsGroup() {
        val question = LetterQuestions.nameOfLetter(ba, random)
        assertEquals("ب", question.prompt)
        assertEquals("bāʾ", question.options[question.answerIndex])
        assertEquals(4, question.options.size)
        // ب's group is ا ب ت ث ن ي: every wrong option is one of them.
        val groupNames = Letters.groups.getValue(1).map { it.name }
        question.options.forEach { assertTrue(it, it in groupNames) }
    }

    @Test
    fun letterForName_answersWithTheLetter() {
        val question = LetterQuestions.letterForName(ba, random)
        assertEquals("bāʾ", question.prompt)
        assertEquals("ب", question.options[question.answerIndex])
    }

    @Test
    fun shape_asksForTheRightPositionAndSkipsHamza() {
        val question = LetterQuestions.shape(ba, ShapePosition.START, random)!!
        assertEquals(Letters.shapesOf(ba).start, question.options[question.answerIndex])
        assertNull(LetterQuestions.shape(Letters["letter.hamza"]!!, ShapePosition.END, random))
        // د never joins the next letter: its "start" is its lone shape, and the options still differ.
        val dal = LetterQuestions.shape(Letters["letter.dal"]!!, ShapePosition.START, random)!!
        assertEquals(dal.options.size, dal.options.toSet().size)
    }

    @Test
    fun firstLetterOfWord_needsAWordAndSkipsAlifAndHamza() {
        val example = LetterExample(1, 1, 0, "بِسۡمِ")
        val question = LetterQuestions.firstLetterOfWord(ba, example, random)!!
        assertEquals("ب", question.options[question.answerIndex])
        assertEquals(example, question.word)
        assertNull(LetterQuestions.firstLetterOfWord(ba, null, random))
        assertNull(LetterQuestions.firstLetterOfWord(Letters["letter.alif"]!!, example, random))
    }

    @Test
    fun theFactoryAsksDifferentKindsAboutALetter() = runBlocking {
        val factory = QuestionFactory(
            lemmaWithPool = { null },
            ayahsWithConcept = { _, _ -> emptyList() },
            random = Random(3),
            letterExample = { LetterExample(1, 1, 0, "بِسۡمِ") },
        )
        val questions = factory.questionsFor("letter.ba", 3).map { it as LetterQuestion }
        assertEquals(3, questions.size)
        assertEquals(3, questions.map { it.kind }.toSet().size)
    }
}
