package com.quranapp.android.learning.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.path.Curriculum
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.LayerProgress
import com.quranapp.android.learning.path.LearningPreferences
import com.quranapp.android.learning.path.PathProgress
import com.quranapp.android.learning.path.PathRepository
import com.quranapp.android.learning.path.Readiness
import com.quranapp.android.learning.path.Stage
import com.quranapp.android.learning.path.StageGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

enum class StageStatus {
    DONE,
    CURRENT,
    LATER,

    /** Before where placement started the learner, and not done: still open to them. */
    SKIPPED,
}

/** One goal of a stage and how far along it is; null progress while unknown (no pack). */
data class GoalProgress(val layer: Layer?, val progress: LayerProgress?)

/** A stage on the path screen. Goals are counted up to the current stage only. */
data class StageRow(
    val stage: Stage,
    val status: StageStatus,
    val goals: List<GoalProgress>?,
    /** Null until this stage's units are loaded. */
    val units: List<UnitDots>?,
)

/** The whole path, filled in stage by stage as the surahs load. */
class PathViewModel(application: Application) : AndroidViewModel(application) {
    private val path = PathRepository.get(application)
    private val quran = DatabaseProvider.getQuranRepository(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val stages: StateFlow<List<StageRow>?> =
        combine(
            DatabaseProvider.getLearningProgressRepository(application).knownConceptIds,
            LearningPackManager.state,
            LearningPreferences.startStage(),
        ) { known, _, start -> known to start.coerceAtLeast(0) }
            .transformLatest { (known, start) -> emitAll(rows(known, start)) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Sends the learner back to placement; their progress is kept. [onSaved] runs once the
     * choice is written, so closing the screen then can't cancel the write.
     */
    fun changeStart(onSaved: () -> Unit) {
        viewModelScope.launch {
            LearningPreferences.setStartStage(LearningPreferences.NOT_CHOSEN)
            onSaved()
        }
    }

    private fun rows(known: Set<String>, start: Int): Flow<List<StageRow>> = flow {
        val current = PathProgress.currentStage(known, path::needs, startAt = start).number
        val rows = Curriculum.stages.map { stage ->
            val status = when {
                stage.number > current -> StageStatus.LATER
                stage.number == current -> StageStatus.CURRENT
                stage.number < start -> StageStatus.SKIPPED // until its goals are counted below
                else -> StageStatus.DONE
            }
            StageRow(stage, status, goals = null, units = null)
        }.toMutableList()
        emit(rows.toList())

        val names = quran.getChapterNames(Curriculum.units)
        rows.forEachIndexed { index, row ->
            val stage = row.stage
            var status = row.status
            val goals = if (row.status == StageStatus.LATER) {
                null
            } else {
                val needs = path.needs(PathProgress.goalSurahs(stage))
                if (status == StageStatus.SKIPPED && stage.goals.all { Readiness.isReached(it, needs, known, stage.number) }) {
                    status = StageStatus.DONE
                }
                stage.goals.map { goal -> GoalProgress((goal as? StageGoal.Surahs)?.layer, Readiness.of(goal, needs, known, stage.number)) }
            }
            val units = stage.units.map { surah ->
                val surahNeeds = path.needs(surah)
                UnitDots(
                    surahNo = surah,
                    name = names[surah].orEmpty(),
                    dots = Layer.entries.associateWith { PathProgress.dot(Readiness.of(listOf(surahNeeds), it, known), it) },
                )
            }
            rows[index] = row.copy(status = status, goals = goals, units = units)
            emit(rows.toList())
        }
    }
}
