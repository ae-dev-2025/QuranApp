package com.quranapp.android.learning.practice

import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.words.WordItems
import kotlin.random.Random

/**
 * Questions about dictionary words (decision 5), built from the learning pack.
 *
 * Wrong options come from [pool]: words of the same kind (a verb with verbs) and of similar
 * frequency, so they're plausible. A candidate whose meaning overlaps the right one
 * ("worship" and "worshippers") is skipped, so there is only ever one right answer.
 */
object WordQuestions {
    const val OPTIONS = 4

    /** "What does عَبَدَ mean?" with four meanings, or null if there aren't enough. */
    fun meaningOfWord(target: LemmaEntity, pool: List<LemmaEntity>, random: Random): ChoiceQuestion? {
        val meaning = target.gloss ?: return null
        val wrong = distractors(target, pool, random).map { it.gloss!! }
        if (wrong.size < OPTIONS - 1) return null
        return shuffled(target, ChoiceKind.MEANING_OF_WORD, target.headword, meaning, wrong, random)
    }

    /** "Which word means worship?" with four Arabic words, or null if there aren't enough. */
    fun wordForMeaning(target: LemmaEntity, pool: List<LemmaEntity>, random: Random): ChoiceQuestion? {
        val meaning = target.gloss ?: return null
        val wrong = distractors(target, pool, random).map { it.headword }
        if (wrong.size < OPTIONS - 1) return null
        return shuffled(target, ChoiceKind.WORD_FOR_MEANING, meaning, target.headword, wrong, random)
    }

    private fun distractors(target: LemmaEntity, pool: List<LemmaEntity>, random: Random): List<LemmaEntity> {
        val targetWords = words(target.gloss!!)
        return pool
            .asSequence()
            .filter { it.lemmaId != target.lemmaId && it.gloss != null && it.headword != target.headword }
            .filter { kind(it.pos) == kind(target.pos) }
            .filter { words(it.gloss!!).none(targetWords::contains) }
            .distinctBy { it.gloss }
            .distinctBy { it.headword }
            .toList()
            .shuffled(random)
            .take(OPTIONS - 1)
    }

    private fun shuffled(
        target: LemmaEntity,
        kind: ChoiceKind,
        prompt: String,
        answer: String,
        wrong: List<String>,
        random: Random,
    ): ChoiceQuestion {
        val options = (wrong + answer).shuffled(random)
        return ChoiceQuestion(WordItems.idOf(target.lemmaKey), kind, prompt, options, options.indexOf(answer))
    }

    /** The meaningful words of a gloss, so "worship" and "worshippers" share "worship". */
    private fun words(gloss: String): Set<String> =
        gloss.lowercase().split(Regex("[^a-z]+")).filter { it.length > 2 }.map { it.take(5) }.toSet()

    /** Verbs, nouns and adjectives, names, or particles: options come from the same group. */
    private fun kind(pos: String): String = when (pos) {
        "V" -> "verb"
        "N", "ADJ" -> "noun"
        "PN" -> "name"
        else -> "particle"
    }
}
