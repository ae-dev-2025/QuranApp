package com.quranapp.android.learning.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.path.Curriculum
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.LayerProgress
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

enum class StageStatus { DONE, CURRENT, LATER }

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
        combine(DatabaseProvider.getLearningProgressRepository(application).knownConceptIds, LearningPackManager.state) { known, _ -> known }
            .transformLatest { known -> emitAll(rows(known)) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun rows(known: Set<String>): Flow<List<StageRow>> = flow {
        val current = PathProgress.currentStage(known, path::needs).number
        val rows = Curriculum.stages.map { stage ->
            val status = when {
                stage.number < current -> StageStatus.DONE
                stage.number == current -> StageStatus.CURRENT
                else -> StageStatus.LATER
            }
            StageRow(stage, status, goals = null, units = null)
        }.toMutableList()
        emit(rows.toList())

        val names = quran.getChapterNames(Curriculum.units)
        rows.forEachIndexed { index, row ->
            val stage = row.stage
            val goals = if (row.status == StageStatus.LATER) {
                null
            } else {
                val needs = path.needs(PathProgress.goalSurahs(stage))
                stage.goals.map { goal -> GoalProgress((goal as? StageGoal.Surahs)?.layer, Readiness.of(goal, needs, known)) }
            }
            val units = stage.units.map { surah ->
                val surahNeeds = path.needs(surah)
                UnitDots(
                    surahNo = surah,
                    name = names[surah].orEmpty(),
                    dots = Layer.entries.associateWith { PathProgress.dot(Readiness.of(listOf(surahNeeds), it, known), it) },
                )
            }
            rows[index] = row.copy(goals = goals, units = units)
            emit(rows.toList())
        }
    }
}
