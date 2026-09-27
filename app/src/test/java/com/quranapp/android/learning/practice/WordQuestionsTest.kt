package com.quranapp.android.learning.practice

import com.quranapp.android.learning.pack.LemmaEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class WordQuestionsTest {
    private val worship = lemma(1, "Eabada", "HW-worship", "V", "worship")
    private val pool = listOf(
        lemma(2, "xalaqa", "HW-create", "V", "create"),
        lemma(3, "qaAla", "HW-say", "V", "said, say"),
        lemma(4, "kafara", "HW-disbelieve", "V", "disbelieve"),
        lemma(5, "Eabod", "HW-servant", "N", "servant, slave"), // a noun: not an option for a verb
        lemma(6, "Eaabid", "HW-worshipper", "V", "worshippers"), // overlaps "worship"
        lemma(7, "ra'aA", "HW-see", "V", "see"),
    )

    @Test
    fun meaningOfWord_hasOneRightAnswerAmongFour() {
        val question = WordQuestions.meaningOfWord(worship, pool, Random(1))!!
        assertEquals("word.Eabada", question.itemId)
        assertEquals("HW-worship", question.prompt)
        assertEquals(4, question.options.size)
        assertEquals("worship", question.options[question.answerIndex])
        assertFalse(question.optionsAreArabic)
    }

    @Test
    fun wrongOptionsAreTheSameKindAndNeverOverlap() {
        repeat(20) { seed ->
            val options = WordQuestions.meaningOfWord(worship, pool, Random(seed))!!.options
            assertFalse("servant, slave" in options) // a noun
            assertFalse("worshippers" in options) // would also be right
        }
    }

    @Test
    fun wordForMeaning_asksForTheArabic() {
        val question = WordQuestions.wordForMeaning(worship, pool, Random(2))!!
        assertEquals("worship", question.prompt)
        assertEquals("HW-worship", question.options[question.answerIndex])
        assertTrue(question.optionsAreArabic)
    }

    @Test
    fun noQuestionWithoutEnoughOptionsOrAMeaning() {
        assertNull(WordQuestions.meaningOfWord(worship, pool.take(2), Random(1)))
        assertNull(WordQuestions.meaningOfWord(worship.copy(gloss = null), pool, Random(1)))
    }

    private fun lemma(id: Int, key: String, headword: String, pos: String, gloss: String) =
        LemmaEntity(id, key, headword, "corpus", pos, null, null, 10, gloss)
}
