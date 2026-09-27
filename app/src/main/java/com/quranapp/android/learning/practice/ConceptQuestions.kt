package com.quranapp.android.learning.practice

import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.examples.AyahWords
import kotlin.random.Random

/** "Tap the word where the noon is hidden": any word with the concept is a right answer. */
data class TapWordQuestion(
    override val itemId: String,
    val surahNo: Int,
    val ayahNo: Int,
    /** The ayah's words, without its number. */
    val words: List<String>,
    val answers: Set<Int>,
) : Question {
    fun isRight(wordIndex: Int) = wordIndex in answers
}

/** "Which rule is on the highlighted word?" The options are concept ids. */
data class RuleQuestion(
    override val itemId: String,
    val surahNo: Int,
    val ayahNo: Int,
    val words: List<String>,
    val highlighted: Int,
    val options: List<String>,
    val answerIndex: Int,
) : Question {
    fun isRight(optionIndex: Int) = optionIndex == answerIndex
}

/**
 * Reading and tajweed questions, built from real ayahs and graded by the same detectors that
 * find concepts in the Understand sheet, so the app always knows the answer.
 *
 * A concept is practised on a different ayah each time (the ayahs come from the caller), so
 * the learner learns the rule rather than the ayah.
 */
object ConceptQuestions {
    /** Null when the ayah doesn't make a fair question: too short, or the rule is almost everywhere. */
    fun tapWord(conceptId: String, ayah: AyahWords): TapWordQuestion? {
        val words = withoutNumber(ayah.words)
        val hits = AyahAnalyzer.analyze(ayah.words).wordsByConcept[conceptId].orEmpty().filter { it < words.size }
        if (words.size < 3 || hits.isEmpty() || hits.size * 2 > words.size) return null
        return TapWordQuestion(conceptId, ayah.surahNo, ayah.ayahNo, words, hits.toSet())
    }

    /**
     * Null when there aren't at least two wrong options. A rule that also applies to the
     * highlighted word is never offered as a wrong option.
     */
    fun ruleOnWord(conceptId: String, ayah: AyahWords, random: Random): RuleQuestion? {
        val words = withoutNumber(ayah.words)
        val analysis = AyahAnalyzer.analyze(ayah.words)
        val hits = analysis.wordsByConcept[conceptId].orEmpty().filter { it < words.size }
        if (hits.isEmpty()) return null
        val word = hits.random(random)
        val onThatWord = analysis.wordsByConcept.filterValues { word in it }.keys
        val wrong = siblings(conceptId).filter { it !in onThatWord }.shuffled(random).take(3)
        if (wrong.size < 2) return null
        val options = (wrong + conceptId).shuffled(random)
        return RuleQuestion(conceptId, ayah.surahNo, ayah.ayahNo, words, word, options, options.indexOf(conceptId))
    }

    /**
     * Concepts a learner might confuse with this one: same track, sharing a prerequisite
     * (the four noon sākinah rules, the three madds after the madd sign…). Umbrella concepts
     * such as noon sākinah itself are never options: they're never the answer on a word.
     */
    fun siblings(conceptId: String): List<String> {
        val target: Concept = ConceptCatalog[conceptId] ?: return emptyList()
        return ConceptCatalog.all
            .filter { it.id != conceptId && it.track == target.track && it.id !in ConceptCatalog.umbrellaIds }
            .filter { other -> other.prerequisites.any { it in target.prerequisites } }
            .map { it.id }
    }

    /** The app stores the ayah number as a last "word"; questions leave it out. */
    private fun withoutNumber(words: List<String>): List<String> =
        if (words.lastOrNull()?.all { it.isDigit() } == true) words.dropLast(1) else words
}
