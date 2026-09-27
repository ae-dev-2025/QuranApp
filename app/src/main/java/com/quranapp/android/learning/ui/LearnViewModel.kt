package com.quranapp.android.learning.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.path.Curriculum
import com.quranapp.android.learning.path.Dot
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.LayerProgress
import com.quranapp.android.learning.path.PathProgress
import com.quranapp.android.learning.path.LearningPreferences
import com.quranapp.android.learning.path.PathRepository
import com.quranapp.android.learning.path.Placement
import com.quranapp.android.learning.path.Readiness
import com.quranapp.android.learning.path.Stage
import com.quranapp.android.learning.path.SurahNeeds
import com.quranapp.android.learning.practice.DailyReview
import com.quranapp.android.learning.progress.WeekSummary
import com.quranapp.android.learning.words.WordItems
import com.quranapp.android.learning.words.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A surah on the Learn tab, with a dot per layer. */
data class UnitDots(val surahNo: Int, val name: String, val dots: Map<Layer, Dot>)

/** What to do next on the path. */
sealed interface NextStep {
    /** Stage 0: a basic concept, until the letters arrive (milestone 7). */
    data class Basic(val concept: Concept, val known: Int, val total: Int) : NextStep

    /** A surah unit and the first layer with something left in it. */
    data class Unit(val unit: UnitDots, val layer: Layer?) : NextStep

    /** Every goal of the last stage is reached. */
    data object Finished : NextStep
}

/** The path part of the Learn tab: where the learner is and what comes next. */
data class PathSummary(
    val stage: Stage,
    val next: NextStep,
    val units: List<UnitDots>,
    /** Where placement started the learner. */
    val start: Int,
    /** Basics not known yet, for the placement check. */
    val unknownBasics: List<String>,
)

/** The learner's goal (journey 4): a surah to understand, and how close they are. */
sealed interface GoalState {
    data object None : GoalState

    data class Chosen(
        val surahNo: Int,
        val name: String,
        /** Null without the learning pack. */
        val words: LayerProgress?,
        /** The next dictionary words to learn, most frequent in the surah first. */
        val nextWords: List<String>,
    ) : GoalState
}

/** Today's new words and where they come from. */
data class NewWordsPlan(val surahName: String, val itemIds: List<String>)

/** Works out the learner's place on the path whenever their progress or the pack changes. */
class LearnViewModel(application: Application) : AndroidViewModel(application) {
    private val path = PathRepository.get(application)
    private val quran = DatabaseProvider.getQuranRepository(application)

    /** Null while loading; [LearningPreferences.NOT_CHOSEN] until placement is done. */
    val startStage: StateFlow<Int?> = LearningPreferences.startStage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<PathSummary?> =
        combine(
            DatabaseProvider.getLearningProgressRepository(application).knownConceptIds,
            LearningPackManager.state,
            LearningPreferences.startStage(),
        ) { known, _, start -> known to start.coerceAtLeast(0) }
            .mapLatest { (known, start) -> summarize(known, start) } // a newer tick cancels unfinished work
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Null while loading. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val goal: StateFlow<GoalState?> =
        combine(
            DatabaseProvider.getLearningProgressRepository(application).knownConceptIds,
            LearningPackManager.state,
            LearningPreferences.goalSurah(),
        ) { known, _, surah -> known to surah }
            .mapLatest { (known, surah) -> if (surah in 1..114) goalOf(surah, known) else GoalState.None }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The last seven days; recounted when progress or a review card changes. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val week: StateFlow<WeekSummary?> = run {
        val database = DatabaseProvider.getUserDatabase(application)
        combine(database.conceptProgressDao().observeKnownIds(), database.reviewDao().observeCards()) { _, _ -> Unit }
            .mapLatest {
                val since = System.currentTimeMillis() - WeekSummary.WEEK_MS
                WeekSummary.of(database.conceptProgressDao().knownSince(since), database.reviewDao().logSince(since))
            }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }

    /**
     * Up to [DailyReview.NEW_WORDS_PER_DAY] new words for today's session: from the goal
     * surah, or else the next unit, most frequent there first. Words that already have a
     * review card are left to the reviews. Null without the pack or when today's are done.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val newWords: StateFlow<NewWordsPlan?> = run {
        val reviews = DatabaseProvider.getUserDatabase(application).reviewDao()
        combine(
            DatabaseProvider.getLearningProgressRepository(application).knownConceptIds,
            reviews.observeCards(),
            LearningPackManager.state,
            LearningPreferences.goalSurah(),
            LearningPreferences.startStage(),
        ) { known, _, _, goal, start -> Triple(known, goal, start.coerceAtLeast(0)) }
            .mapLatest { (known, goal, start) -> planNewWords(known, goal, start) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    }

    private suspend fun planNewWords(known: Set<String>, goal: Int, start: Int): NewWordsPlan? {
        val reviews = DatabaseProvider.getUserDatabase(getApplication()).reviewDao()
        val left = DailyReview.newWordsLeft(reviews.wordsStartedSince(DailyReview.startOfDay(System.currentTimeMillis())))
        if (left == 0) return null
        val words = WordRepository.open(getApplication()) ?: return null
        val surah = if (goal in 1..114) {
            goal
        } else {
            val stage = PathProgress.currentStage(known, path::needs, startAt = start)
            PathProgress.nextUnit(stage, path.needs(PathProgress.goalSurahs(stage)), known) ?: 1
        }
        val candidates = Readiness.unknownWords(path.needs(surah), known)
            .filter { reviews.card(it) == null }
            .take(left * 3) // enough to skip the few without a meaning
            .mapNotNull(WordItems::lemmaKeyOf)
        val chosen = words.wordLemmas(candidates).filter { it.lemma.gloss != null }.take(left).map { it.itemId }
        return if (chosen.isEmpty()) null else NewWordsPlan(quran.getChapterName(surah), chosen)
    }

    fun setGoal(surahNo: Int) {
        viewModelScope.launch { LearningPreferences.setGoalSurah(surahNo) }
    }

    private suspend fun goalOf(surahNo: Int, known: Set<String>): GoalState.Chosen {
        val needs = path.needs(surahNo)
        val nextKeys = Readiness.unknownWords(needs, known).take(NEXT_WORDS).mapNotNull(WordItems::lemmaKeyOf)
        val nextWords = WordRepository.open(getApplication())?.wordLemmas(nextKeys)?.map { it.lemma.headword }.orEmpty()
        return GoalState.Chosen(surahNo, quran.getChapterName(surahNo), Readiness.of(listOf(needs), Layer.WORDS, known), nextWords)
    }

    fun choose(placement: Placement) {
        viewModelScope.launch { LearningPreferences.setStartStage(placement.stage) }
    }

    private suspend fun summarize(known: Set<String>, start: Int): PathSummary {
        val stage = PathProgress.currentStage(known, path::needs, startAt = start)

        // Stage 0 has no surahs yet: show where the learner is heading, stage 1.
        val pathSurahs = stage.units.ifEmpty { PathProgress.goalSurahs(stage) }.ifEmpty { Curriculum.stages[1].units }
        val needs = path.needs(pathSurahs)
        val nextSurah = PathProgress.nextUnit(stage, path.needs(PathProgress.goalSurahs(stage)), known)

        // A short window of the path: one unit before the next one, and a few after.
        val windowStart = (pathSurahs.indexOf(nextSurah) - 1).coerceAtLeast(0)
        val shown = pathSurahs.drop(windowStart).take(PATH_PREVIEW)
        val names = quran.getChapterNames((shown + listOfNotNull(nextSurah)).distinct())
        fun unitDots(surah: Int, surahNeeds: SurahNeeds) =
            UnitDots(surah, names[surah].orEmpty(), Layer.entries.associateWith { PathProgress.dot(Readiness.of(listOf(surahNeeds), it, known), it) })

        val next = when {
            stage.number == 0 -> {
                val basics = Curriculum.BASICS
                PathProgress.nextConcept(stage, known)?.let(ConceptCatalog::get)
                    ?.let { NextStep.Basic(it, basics.count { id -> id in known }, basics.size) }
                    ?: NextStep.Finished
            }
            nextSurah != null -> {
                val unit = unitDots(nextSurah, path.needs(nextSurah))
                NextStep.Unit(unit, Layer.entries.firstOrNull { unit.dots[it] != Dot.FULL })
            }
            else -> NextStep.Finished
        }
        return PathSummary(
            stage = stage,
            next = next,
            units = shown.map { unitDots(it, needs.getValue(it)) },
            start = start,
            unknownBasics = Curriculum.BASICS.filter { it !in known },
        )
    }

    private companion object {
        const val PATH_PREVIEW = 4
        const val NEXT_WORDS = 3
    }
}
