package com.quranapp.android.learning.practice

import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.pack.LemmaEntity
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
) {
    /** Up to [count] questions about [itemId]; fewer (or none) if fair ones can't be made. */
    suspend fun questionsFor(itemId: String, count: Int): List<Question> {
        val lemmaKey = WordItems.lemmaKeyOf(itemId)
        return when {
            lemmaKey != null -> wordQuestions(lemmaKey, count)
            ConceptCatalog[itemId] != null -> conceptQuestions(itemId, count)
            else -> emptyList() // letters and grammar get their questions in later milestones
        }
    }

    /** The introduction shown before a new word's questions, or null if it has no meaning to show. */
    suspend fun introductionFor(itemId: String): WordIntroduction? {
        val lemmaKey = WordItems.lemmaKeyOf(itemId) ?: return null
        val (lemma, _) = lemmaWithPool(lemmaKey) ?: return null
        val meaning = lemma.gloss ?: return null
        return WordIntroduction(itemId, lemma.headword, meaning, lemma.occurrences)
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

    private suspend fun conceptQuestions(conceptId: String, count: Int): List<Question> {
        // A fresh ayah for every question, taken at random from the first few dozen examples.
        val ayahs = ayahsWithConcept(conceptId, count * AYAHS_PER_QUESTION).shuffled(random)
        val questions = mutableListOf<Question>()
        for (ayah in ayahs) {
            if (questions.size == count) break
            val tapFirst = questions.size % 2 == 0
            val question = if (tapFirst) {
                ConceptQuestions.tapWord(conceptId, ayah) ?: ConceptQuestions.ruleOnWord(conceptId, ayah, random)
            } else {
                ConceptQuestions.ruleOnWord(conceptId, ayah, random) ?: ConceptQuestions.tapWord(conceptId, ayah)
            }
            if (question != null) questions += question
        }
        return questions
    }

    private companion object {
        /** How many candidate ayahs to fetch per question: some don't make a fair question. */
        const val AYAHS_PER_QUESTION = 8
    }
}
