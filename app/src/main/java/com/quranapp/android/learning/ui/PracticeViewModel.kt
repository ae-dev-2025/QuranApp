package com.quranapp.android.learning.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.examples.ConceptExampleFinder
import com.quranapp.android.learning.practice.ItemResult
import com.quranapp.android.learning.practice.PracticeItem
import com.quranapp.android.learning.practice.PracticeSession
import com.quranapp.android.learning.practice.Question
import com.quranapp.android.learning.practice.QuestionFactory
import com.quranapp.android.learning.practice.ReviewScheduler
import com.quranapp.android.learning.progress.ReviewRating
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What the practice screen shows. */
sealed interface PracticeUiState {
    data object Loading : PracticeUiState

    /** No fair questions could be made, e.g. words without the learning pack. */
    data object Empty : PracticeUiState

    data class Asking(
        val question: Question,
        val number: Int,
        val total: Int,
        /** The learner's answer (an option or a word index), once given. */
        val answer: Int? = null,
    ) : PracticeUiState {
        val answered: Boolean get() = answer != null
    }

    data class Finished(val right: Int, val total: Int, val results: List<ItemResult>) : PracticeUiState
}

/** How many questions each item gets. */
enum class PracticeMode(val questionsPerItem: Int) {
    /** Proving an item is known (decision 7): three questions. */
    CHECK(3),

    /** A daily review: one question per due item. */
    REVIEW(1),
}

/**
 * Runs a practice session: builds the questions, grades each answer, and saves each item's
 * result as a review with the FSRS scheduler. A passed item is also marked known.
 */
class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    var state: PracticeUiState by mutableStateOf(PracticeUiState.Loading)
        private set

    private var session: PracticeSession? = null
    private val results = mutableListOf<ItemResult>()
    private val reviews = DatabaseProvider.getUserDatabase(application).reviewDao()
    private val progress = DatabaseProvider.getLearningProgressRepository(application)
    private val scheduler = ReviewScheduler()

    fun start(itemIds: List<String>, mode: PracticeMode) {
        if (session != null) return // already started (e.g. after rotating the screen)
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { buildItems(itemIds, mode) }
            val started = PracticeSession(items).also { session = it }
            state = started.current?.let { PracticeUiState.Asking(it, 1, started.size) } ?: PracticeUiState.Empty
        }
    }

    /** The learner chose [answer]: an option index, or a word index for tap questions. */
    fun answer(answer: Int, right: Boolean) {
        val current = state as? PracticeUiState.Asking ?: return
        if (current.answered) return
        state = current.copy(answer = answer)
        val result = session?.answer(right) ?: return
        results += result
        viewModelScope.launch(Dispatchers.IO) { save(result) }
    }

    fun next() {
        val started = session ?: return
        val question = started.current
        state = if (question == null) {
            PracticeUiState.Finished(started.rightCount, started.size, results.toList())
        } else {
            PracticeUiState.Asking(question, started.index + 1, started.size)
        }
    }

    private suspend fun save(result: ItemResult) {
        val now = System.currentTimeMillis()
        val outcome = scheduler.review(result.itemId, reviews.card(result.itemId), result.rating, now)
        reviews.record(outcome.card, outcome.log)
        if (result.rating != ReviewRating.AGAIN) progress.setKnown(result.itemId, true)
    }

    private suspend fun buildItems(itemIds: List<String>, mode: PracticeMode): List<PracticeItem> {
        val context = getApplication<Application>()
        val quran = DatabaseProvider.getQuranRepository(context)
        val words = WordRepository.open(context)
        val finder = ConceptExampleFinder { surahNo ->
            quran.getWordsForSurah(surahNo, QuranScriptUtils.SCRIPT_UTHMANI)
                .map { (ayahId, ayahWords) -> AyahWords(surahNo, ayahId % 1000, ayahWords.map { it.text }) }
        }
        val factory = QuestionFactory(
            lemmaWithPool = { key -> words?.lemma(key)?.let { it to words.questionPool(it) } },
            ayahsWithConcept = { conceptId, limit -> finder.find(conceptId, limit).map { it.ayah } },
        )
        return itemIds
            .map { PracticeItem(it, factory.questionsFor(it, mode.questionsPerItem)) }
            .filter { it.questions.isNotEmpty() }
    }
}
