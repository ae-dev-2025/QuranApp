package com.quranapp.android.learning.practice

import com.quranapp.android.learning.letters.Letter
import com.quranapp.android.learning.letters.LetterExample
import com.quranapp.android.learning.letters.Letters
import kotlin.random.Random

/** What a letter question asks. The screen turns each into its own sentence. */
enum class LetterQuestionKind {
    /** The letter is shown; the options are names. */
    NAME_OF_LETTER,

    /** A name is given; the options are letters. */
    LETTER_FOR_NAME,

    /** "Which is bāʾ at the start of a word?"; the options are shapes. */
    SHAPE,

    /** A word is played; the options are letters it might start with. */
    FIRST_LETTER_OF_WORD,
}

enum class ShapePosition { START, MIDDLE, END }

/**
 * A question about a letter item (the design's letter questions). [prompt] is the letter
 * for [LetterQuestionKind.NAME_OF_LETTER] and its name otherwise.
 */
data class LetterQuestion(
    override val itemId: String,
    val kind: LetterQuestionKind,
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
    val position: ShapePosition? = null,
    /** The word to hear, for [LetterQuestionKind.FIRST_LETTER_OF_WORD]. */
    val word: LetterExample? = null,
) : Question {
    init {
        require(answerIndex in options.indices) { "the answer must be one of the options" }
        require(options.distinct().size == options.size) { "options must differ" }
    }

    /** Options in Arabic are drawn in the letter font. */
    val optionsAreLetters: Boolean get() = kind != LetterQuestionKind.NAME_OF_LETTER

    fun isRight(optionIndex: Int) = optionIndex == answerIndex
}

/**
 * Makes letter questions. Wrong options come from the letter's own group first, the letters
 * that look alike (ب ت ث ن ي), so the learner practises telling them apart.
 */
object LetterQuestions {
    private const val OPTIONS = 4

    private fun distractors(letter: Letter, random: Random, usable: (Letter) -> Boolean = { true }): List<Letter> {
        val others = Letters.all.filter { it != letter && usable(it) }
        val sameGroup = others.filter { it.group == letter.group }.shuffled(random)
        val rest = others.filter { it.group != letter.group }.shuffled(random)
        return (sameGroup + rest).take(OPTIONS - 1)
    }

    private fun <T> withAnswer(answer: T, wrong: List<T>, random: Random): Pair<List<T>, Int> {
        val options = (wrong + answer).shuffled(random)
        return options to options.indexOf(answer)
    }

    fun nameOfLetter(letter: Letter, random: Random): LetterQuestion {
        val (options, answer) = withAnswer(letter.name, distractors(letter, random).map { it.name }, random)
        return LetterQuestion(letter.id, LetterQuestionKind.NAME_OF_LETTER, letter.char.toString(), options, answer)
    }

    fun letterForName(letter: Letter, random: Random): LetterQuestion {
        val (options, answer) = withAnswer(letter.char.toString(), distractors(letter, random).map { it.char.toString() }, random)
        return LetterQuestion(letter.id, LetterQuestionKind.LETTER_FOR_NAME, letter.name, options, answer)
    }

    /** Null for hamza, which has one shape only. */
    fun shape(letter: Letter, position: ShapePosition, random: Random): LetterQuestion? {
        if (letter.id == "letter.hamza") return null
        fun shapeOf(l: Letter) = Letters.shapesOf(l).let {
            when (position) {
                ShapePosition.START -> it.start
                ShapePosition.MIDDLE -> it.middle
                ShapePosition.END -> it.end
            }
        }
        val wrong = distractors(letter, random) { it.id != "letter.hamza" }.map(::shapeOf).filter { it != shapeOf(letter) }.distinct()
        if (wrong.size < 2) return null
        val (options, answer) = withAnswer(shapeOf(letter), wrong, random)
        return LetterQuestion(letter.id, LetterQuestionKind.SHAPE, letter.name, options, answer, position = position)
    }

    /** Null for alif and hamza: a word "starting" with أ has both, so it isn't a fair question. */
    fun firstLetterOfWord(letter: Letter, example: LetterExample?, random: Random): LetterQuestion? {
        if (example == null || letter.id == "letter.alif" || letter.id == "letter.hamza") return null
        val wrong = distractors(letter, random) { it.id != "letter.alif" && it.id != "letter.hamza" }.map { it.char.toString() }
        val (options, answer) = withAnswer(letter.char.toString(), wrong, random)
        return LetterQuestion(letter.id, LetterQuestionKind.FIRST_LETTER_OF_WORD, letter.name, options, answer, word = example)
    }
}
