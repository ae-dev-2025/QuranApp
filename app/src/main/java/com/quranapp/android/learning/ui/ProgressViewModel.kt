package com.quranapp.android.learning.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.LayerProgress
import com.quranapp.android.learning.path.PathRepository
import com.quranapp.android.learning.words.WordItems
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

/** A surah on the map: how much of its words the learner knows. */
data class SurahCell(val surahNo: Int, val name: String, val words: LayerProgress)

/** Everything the progress screen shows. Words parts are null without the learning pack. */
data class QuranProgress(
    val wordsKnown: Int,
    /** How many dictionary words the pack has. */
    val wordsTotal: Int?,
    /** Reading and tajweed concepts known out of all of them; the map's words over the whole Quran. */
    val layers: Map<Layer, LayerProgress?>,
    /** Null until all 114 surahs are counted. */
    val surahs: List<SurahCell>?,
)

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val path = PathRepository.get(application)
    private val quran = DatabaseProvider.getQuranRepository(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val progress: StateFlow<QuranProgress?> =
        combine(DatabaseProvider.getLearningProgressRepository(application).knownConceptIds, LearningPackManager.state) { known, _ -> known }
            .transformLatest { known -> emitAll(progressOf(known)) }
            .flowOn(Dispatchers.IO)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun progressOf(known: Set<String>): Flow<QuranProgress> = flow {
        val lemmaCount = LearningPackManager.database(getApplication())?.dao()?.lemmaCount()
        fun conceptsOf(layer: Layer): LayerProgress {
            val concepts = ConceptCatalog.all.filter { it.track == layer.track }
            return LayerProgress(concepts.count { it.id in known }, concepts.size)
        }
        val first = QuranProgress(
            wordsKnown = known.count(WordItems::isWordItem),
            wordsTotal = lemmaCount,
            layers = mapOf(Layer.READ to conceptsOf(Layer.READ), Layer.RECITE to conceptsOf(Layer.RECITE), Layer.WORDS to null),
            surahs = null,
        )
        emit(first) // the totals show at once; the map follows
        if (lemmaCount == null) return@flow

        val names = quran.getChapterNames((1..114).toList())
        val cells = (1..114).mapNotNull { surah ->
            val words = path.words(surah) ?: return@mapNotNull null
            SurahCell(surah, names[surah].orEmpty(), LayerProgress(words.count { word -> word.all { it in known } }, words.size))
        }
        val quranWords = LayerProgress(cells.sumOf { it.words.known }, cells.sumOf { it.words.total })
        emit(first.copy(layers = first.layers + (Layer.WORDS to quranWords), surahs = cells))
    }
}
