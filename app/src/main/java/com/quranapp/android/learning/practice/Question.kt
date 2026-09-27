package com.quranapp.android.learning.practice

/**
 * One practice question. Every question has a known answer, so the app grades it itself
 * (decision 8) and nothing is left to the learner's judgement.
 */
sealed interface Question {
    /** The item this question checks: `word.Eabada`, `tajweed.ikhfa`, … */
    val itemId: String
}

/** What a multiple-choice question asks. The UI turns each kind into its own sentence. */
enum class ChoiceKind {
    /** "What does this word mean?" The prompt is Arabic, the options are meanings. */
    MEANING_OF_WORD,

    /** "Which word means …?" The prompt is a meaning, the options are Arabic words. */
    WORD_FOR_MEANING,
}

data class ChoiceQuestion(
    override val itemId: String,
    val kind: ChoiceKind,
    /** Arabic for MEANING_OF_WORD, a meaning for WORD_FOR_MEANING. */
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int,
) : Question {
    init {
        require(answerIndex in options.indices) { "the answer must be one of the options" }
        require(options.distinct().size == options.size) { "options must differ" }
    }

    /** Arabic options are shown in the Quran font. */
    val optionsAreArabic: Boolean get() = kind == ChoiceKind.WORD_FOR_MEANING

    fun isRight(optionIndex: Int) = optionIndex == answerIndex
}
