package com.quranapp.android.learning.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.analysis.ConceptIndex
import com.quranapp.android.learning.examples.ConceptExampleFinder
import com.quranapp.android.learning.examples.GrammarExampleFinder
import com.quranapp.android.learning.practice.ItemResult
import com.quranapp.android.learning.practice.PracticeItem
import com.quranapp.android.learning.practice.PracticeSession
import com.quranapp.android.learning.practice.Question
import com.quranapp.android.learning.practice.QuestionFactory
import com.quranapp.android.learning.practice.ReviewScheduler
import com.quranapp.android.learning.practice.WordIntroduction
import com.quranapp.android.learning.progress.ReviewRating
import com.quranapp.android.learning.letters.LetterFinder
import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.WordLocation
import com.quranapp.android.learning.words.WordForms
import com.quranapp.android.learning.words.GrammarIndex
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.repository.QuranRepository
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

    data class Finished(val right: Int, val total: Int, val results: List<ItemResult>, val mode: PracticeMode) : PracticeUiState
}

/** How many questions each item gets. */
enum class PracticeMode(val questionsPerItem: Int) {
    /** Proving an item is known (decision 7): three questions. */
    CHECK(3),

    /** A daily review: one question per due item. */
    REVIEW(1),

    /**
     * Placement (decision 11): one question per basic. Only passes are saved, so a wrong
     * guess about something never learned doesn't turn into a review.
     */
    PLACEMENT(1),
}

/**
 * Runs a practice session: builds the questions, grades each answer, and saves each item's
 * result as a review with the FSRS scheduler. A passed item is also marked known.
 */
class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        /** Enough to find the dictionary form of a word in most cases, without reading the whole Quran. */
        const val PLACES_TO_TRY = 30
    }

    var state: PracticeUiState by mutableStateOf(PracticeUiState.Loading)
        private set

    private var session: PracticeSession? = null
    private var mode = PracticeMode.CHECK
    private val results = mutableListOf<ItemResult>()
    private val reviews = DatabaseProvider.getUserDatabase(application).reviewDao()
    private val progress = DatabaseProvider.getLearningProgressRepository(application)
    private val scheduler = ReviewScheduler()

    /** [newIds] are new words: each is introduced, then checked, after the [itemIds]. */
    fun start(itemIds: List<String>, mode: PracticeMode, newIds: List<String> = emptyList()) {
        if (session != null) return // already started (e.g. after rotating the screen)
        this.mode = mode
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { buildItems(itemIds, mode) + buildNewItems(newIds) }
            val started = PracticeSession(items).also { session = it }
            state = started.current?.let { PracticeUiState.Asking(it, 1, started.gradedCount) } ?: PracticeUiState.Empty
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

    /** The learner has read a new word's introduction. */
    fun acknowledge() {
        val started = session ?: return
        if (started.current !is WordIntroduction) return
        started.acknowledge()
        next()
    }

    fun next() {
        val started = session ?: return
        val question = started.current
        state = if (question == null) {
            PracticeUiState.Finished(started.rightCount, started.gradedCount, results.toList(), mode)
        } else {
            PracticeUiState.Asking(question, started.gradedBefore + 1, started.gradedCount)
        }
    }

    private suspend fun save(result: ItemResult) {
        if (mode == PracticeMode.PLACEMENT && result.rating == ReviewRating.AGAIN) return
        val now = System.currentTimeMillis()
        val outcome = scheduler.review(result.itemId, reviews.card(result.itemId), result.rating, now)
        reviews.record(outcome.card, outcome.log)
        if (result.rating != ReviewRating.AGAIN) progress.setKnown(result.itemId, true)
    }

    private suspend fun buildNewItems(newIds: List<String>): List<PracticeItem> {
        val factory = factory()
        return newIds.mapNotNull { itemId ->
            val introduction = factory.introductionFor(itemId) ?: return@mapNotNull null
            val questions = factory.questionsFor(itemId, PracticeMode.CHECK.questionsPerItem)
            if (questions.isEmpty()) null else PracticeItem(itemId, listOf(introduction) + questions)
        }
    }

    private suspend fun buildItems(itemIds: List<String>, mode: PracticeMode): List<PracticeItem> {
        val factory = factory()
        return itemIds
            .map { PracticeItem(it, factory.questionsFor(it, mode.questionsPerItem)) }
            .filter { it.questions.isNotEmpty() }
    }

    /**
     * Where to hear a dictionary word: among its first occurrences, one written like its
     * dictionary form (قَالَ, not يَقُولُ), so the learner hears what they see; else the first.
     */
    private suspend fun placeToHear(lemma: LemmaEntity, words: WordRepository?, quran: QuranRepository): WordLocation? {
        val places = words?.occurrences(lemma.lemmaId, limit = PLACES_TO_TRY).orEmpty()
        return places.firstOrNull { place ->
            val text = quran.getWordsForAyahById(place.ayahId, QuranScriptUtils.SCRIPT_UTHMANI).getOrNull(place.wordIndex)?.text
            text != null && WordForms.sameSkeleton(text, lemma.headword)
        } ?: places.firstOrNull()
    }

    /** Al-Fātiḥah and Juz ʿAmma, loaded once per session for the letters' listening questions. */
    private var shortSurahs: List<AyahWords>? = null

    private suspend fun shortSurahs(quran: QuranRepository): List<AyahWords> = shortSurahs ?: (listOf(1) + (114 downTo 78))
        .flatMap { surah ->
            quran.getWordsForSurah(surah, QuranScriptUtils.SCRIPT_UTHMANI)
                .map { (ayahId, ayahWords) -> AyahWords(surah, ayahId % 1000, ayahWords.map { it.text }) }
        }
        .also { shortSurahs = it }

    private suspend fun factory(): QuestionFactory {
        val context = getApplication<Application>()
        val quran = DatabaseProvider.getQuranRepository(context)
        val words = WordRepository.open(context)
        val finder = ConceptExampleFinder(ayahsWith = { ConceptIndex.get(context)?.ayahsOf(it) }) { surahNo ->
            quran.getWordsForSurah(surahNo, QuranScriptUtils.SCRIPT_UTHMANI)
                .map { (ayahId, ayahWords) -> AyahWords(surahNo, ayahId % 1000, ayahWords.map { it.text }) }
        }
        val grammarFinder = GrammarExampleFinder(
            grammarOfSurah = { surahNo -> words?.grammarByAyahOfSurah(surahNo) },
            loadAyah = { ayahId -> quran.getWordsForAyahById(ayahId, QuranScriptUtils.SCRIPT_UTHMANI).map { it.text } },
            ayahsWith = { GrammarIndex.get(context)?.ayahsOf(it) },
            grammarOfAyah = { ayahId -> words?.grammarOfAyah(ayahId) },
        )
        return QuestionFactory(
            lemmaWithPool = { key -> words?.lemma(key)?.let { it to words.questionPool(it) } },
            ayahsWithConcept = { conceptId, limit -> finder.find(conceptId, limit).map { it.ayah } },
            firstPlace = { lemma -> placeToHear(lemma, words, quran) },
            letterExample = { letter -> LetterFinder.exampleOf(letter, shortSurahs(quran)) },
            grammarAyahs = grammarFinder::ayahs,
        )
    }
}
