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
import com.quranapp.android.learning.path.PathProgress
import com.quranapp.android.learning.path.PathRepository
import com.quranapp.android.learning.path.Readiness
import com.quranapp.android.learning.path.Stage
import com.quranapp.android.learning.path.SurahNeeds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

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
data class PathSummary(val stage: Stage, val next: NextStep, val units: List<UnitDots>)

/** Works out the learner's place on the path whenever their progress or the pack changes. */
class LearnViewModel(application: Application) : AndroidViewModel(application) {
    private val path = PathRepository.get(application)
    private val quran = DatabaseProvider.getQuranRepository(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<PathSummary?> =
        combine(DatabaseProvider.getLearningProgressRepository(application).knownConceptIds, LearningPackManager.state) { known, _ -> known }
            .mapLatest(::summarize) // a newer tick cancels the unfinished work for an older one
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private suspend fun summarize(known: Set<String>): PathSummary {
        val stage = PathProgress.currentStage(known, path::needs)

        // Stage 0 has no surahs yet: show where the learner is heading, stage 1.
        val pathSurahs = stage.units.ifEmpty { PathProgress.goalSurahs(stage) }.ifEmpty { Curriculum.stages[1].units }
        val needs = path.needs(pathSurahs)
        val nextSurah = PathProgress.nextUnit(stage, path.needs(PathProgress.goalSurahs(stage)), known)

        // A short window of the path: one unit before the next one, and a few after.
        val start = (pathSurahs.indexOf(nextSurah) - 1).coerceAtLeast(0)
        val shown = pathSurahs.drop(start).take(PATH_PREVIEW)
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
        return PathSummary(stage, next, shown.map { unitDots(it, needs.getValue(it)) })
    }

    private companion object {
        const val PATH_PREVIEW = 4
    }
}
