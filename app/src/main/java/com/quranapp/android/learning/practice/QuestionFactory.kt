package com.quranapp.android.learning.practice

import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.Track
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.letters.Letter
import com.quranapp.android.learning.letters.LetterExample
import com.quranapp.android.learning.letters.Letters
import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.WordLocation
import com.quranapp.android.learning.words.WordItems
import kotlin.random.Random

/**
 * Makes questions for any item id. The data comes in through functions, so the factory is
 * tested without a database; the app passes the learning pack and the Quran text.
 */
class QuestionFactory(
    /** A dictionary word and candidates for its wrong options, or null without the pack. */
    private val lemmaWithPool: suspend (lemmaKey: String) -> Pair<LemmaEntity, List<LemmaEntity>>?,
    /** Ayahs where a concept occurs, short surahs first. */
    private val ayahsWithConcept: suspend (conceptId: String, limit: Int) -> List<AyahWords>,
    private val random: Random = Random.Default,
    /** Where to hear a dictionary word, for "Hear it" on its introduction. */
    private val firstPlace: suspend (lemma: LemmaEntity) -> WordLocation? = { null },
    /** A word where a letter is heard first, for the listening question. */
    private val letterExample: suspend (Letter) -> LetterExample? = { null },
    /** Ayahs where a grammar concept occurs, with all their grammar; none without the pack. */
    private val grammarAyahs: suspend (conceptId: String, limit: Int) -> List<GrammarAyah> = { _, _ -> emptyList() },
) {
    /** Up to [count] questions about [itemId]; fewer (or none) if fair ones can't be made. */
    suspend fun questionsFor(itemId: String, count: Int): List<Question> {
        val lemmaKey = WordItems.lemmaKeyOf(itemId)
        val letter = Letters[itemId]
        return when {
            lemmaKey != null -> wordQuestions(lemmaKey, count)
            letter != null -> letterQuestions(letter, count)
            ConceptCatalog[itemId] != null -> conceptQuestions(itemId, count)
            else -> emptyList()
        }
    }

    /** The introduction shown before a new word's questions, or null if it has no meaning to show. */
    suspend fun introductionFor(itemId: String): WordIntroduction? {
        val lemmaKey = WordItems.lemmaKeyOf(itemId) ?: return null
        val (lemma, _) = lemmaWithPool(lemmaKey) ?: return null
        val meaning = lemma.gloss ?: return null
        return WordIntroduction(itemId, lemma.headword, meaning, lemma.occurrences, firstPlace(lemma))
    }

    private suspend fun wordQuestions(lemmaKey: String, count: Int): List<Question> {
        val (lemma, pool) = lemmaWithPool(lemmaKey) ?: return emptyList()
        // Alternate the two directions: meaning of the word, then the word for the meaning.
        return (0 until count).mapNotNull { n ->
            if (n % 2 == 0) {
                WordQuestions.meaningOfWord(lemma, pool, random)
            } else {
                WordQuestions.wordForMeaning(lemma, pool, random)
            }
        }
    }

    /** The kinds take turns, starting from a random one, so reviews don't always ask the same. */
    private suspend fun letterQuestions(letter: Letter, count: Int): List<Question> {
        val example = letterExample(letter)
        val makers: List<() -> Question?> = listOf(
            { LetterQuestions.nameOfLetter(letter, random) },
            { LetterQuestions.letterForName(letter, random) },
            { LetterQuestions.shape(letter, ShapePosition.entries.random(random), random) },
            { LetterQuestions.firstLetterOfWord(letter, example, random) },
        )
        val start = random.nextInt(makers.size)
        return makers.indices.asSequence()
            .mapNotNull { makers[(start + it) % makers.size]() }
            .take(count)
            .toList()
    }

    private suspend fun conceptQuestions(conceptId: String, count: Int): List<Question> {
        // A fresh ayah for every question, taken at random from the first few dozen examples.
        // Grammar is found in the pack; reading and tajweed in the text's marks.
        val limit = count * AYAHS_PER_QUESTION
        val ayahs = if (ConceptCatalog[conceptId]?.track == Track.GRAMMAR) {
            grammarAyahs(conceptId, limit).map { it.ayah to it.wordsByConcept }
        } else {
            ayahsWithConcept(conceptId, limit).map { it to AyahAnalyzer.analyze(it.words).wordsByConcept }
        }.shuffled(random)
        val questions = mutableListOf<Question>()
        for ((ayah, found) in ayahs) {
            if (questions.size == count) break
            val tap = { ConceptQuestions.tapWord(conceptId, ayah, found) }
            val rule = { ConceptQuestions.ruleOnWord(conceptId, ayah, random, found) }
            val question = if (questions.size % 2 == 0) tap() ?: rule() else rule() ?: tap()
            if (question != null) questions += question
        }
        return questions
    }

    private companion object {
        /** How many candidate ayahs to fetch per question: some don't make a fair question. */
        const val AYAHS_PER_QUESTION = 8
    }
}
