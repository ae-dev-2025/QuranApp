package com.quranapp.android.learning.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.reader.LocalReaderViewModel
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.db.relations.VerseWithDetails
import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptGraph
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.Track
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackState
import com.quranapp.android.learning.words.AyahWord
import com.quranapp.android.learning.words.WordCoverage
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.repository.QuranRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * One concept to show, with the words of the ayah where it appears. [words] is empty for a
 * concept that is only needed as a prerequisite.
 */
private data class ConceptItem(
    val concept: Concept,
    val words: List<String>,
    val inEveryWord: Boolean,
)

/** Everything the sheet shows for one ayah. */
private data class UnderstandAyahState(
    val ayahText: String,
    /** The ayah's words in the Uthmani script, without the ayah number. */
    val words: List<String>,
    val items: List<ConceptItem>,
)

/**
 * Bottom sheet that answers "what do I need to know to read this ayah?".
 *
 * It lists every concept the ayah uses, plus their prerequisites, in the order a learner
 * should study them. Nothing is shown when [verse] is null.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnderstandAyahSheet(
    verse: VerseWithDetails?,
    onDismiss: () -> Unit,
) {
    if (verse == null) return

    val repository = LocalReaderViewModel.current.repository
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Starts as null (loading) and is filled in by the coroutine below. The work is
    // restarted only when the ayah changes.
    val state by produceState<UnderstandAyahState?>(initialValue = null, verse.id) {
        value = withContext(Dispatchers.IO) { loadState(repository, verse.id) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = colorScheme.scrim.alpha(0.5f),
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface,
        contentWindowInsets = { WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom) },
    ) {
        val loaded = state

        if (loaded == null) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            SheetContent(verse, loaded)
        }
    }
}

private suspend fun loadState(repository: QuranRepository, ayahId: Int): UnderstandAyahState {
    // Always analyze the Unicode text: the other scripts store font glyph codes, not letters.
    val words = repository
        .getWordsForAyahById(ayahId, QuranScriptUtils.SCRIPT_UTHMANI)
        .map { it.text }

    val analysis = AyahAnalyzer.analyze(words)
    val graph = ConceptGraph()
    val needed = graph.inLearningOrder(graph.withPrerequisites(analysis.conceptIds))

    // Every real word has letters; the ayah-number marker at the end has none.
    val wordCount = analysis.wordsByConcept[ConceptIds.LETTERS].orEmpty().size

    val items = needed.map { concept ->
        val wordIndexes = analysis.wordsByConcept[concept.id].orEmpty()
        ConceptItem(
            concept = concept,
            words = wordIndexes.map { words[it] },
            inEveryWord = wordCount > 1 && wordIndexes.size == wordCount,
        )
    }

    return UnderstandAyahState(ayahText = words.joinToString(" "), words = words.dropLast(1), items = items)
}

/**
 * The layers of understanding an ayah (decision 3): one tab each. Read and Recite list the
 * concepts found in the text; Words lists its dictionary words from the learning pack.
 * Grammar is added in milestone 8.
 */
private enum class Layer(val labelRes: Int, val track: Track?) {
    READ(R.string.learning_layer_read, Track.READING),
    RECITE(R.string.learning_layer_recite, Track.TAJWEED),
    WORDS(R.string.learning_layer_words, null),
}

/** How many of a layer's items the learner knows. */
private data class LayerCount(val known: Int, val total: Int)

@Composable
private fun SheetContent(verse: VerseWithDetails, state: UnderstandAyahState) {
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }

    // The set of known concept IDs. Whenever the database changes, the Flow emits a new set
    // and Compose redraws everything that reads `known`.
    val known by progress.knownConceptIds.collectAsState(initial = emptySet())

    // Tied to this composable: if the sheet closes, unfinished work is cancelled.
    val scope = rememberCoroutineScope()

    val itemsByLayer = remember(state) {
        Layer.entries.associateWith { layer -> state.items.filter { it.concept.track == layer.track } }
    }

    // The ayah's words from the learning pack, or null if it isn't downloaded (yet).
    val packState by LearningPackManager.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { LearningPackManager.refresh(context) }
    val installed = packState == LearningPackState.Installed
    val packWords by produceState<List<AyahWord>?>(initialValue = null, verse.id, installed) {
        value = if (installed) {
            withContext(Dispatchers.IO) { WordRepository.open(context)?.wordsOfAyah(verse.id) }
        } else {
            null
        }
    }
    val coverage = packWords?.let { WordCoverage.of(it, known) }
    val entries = remember(packWords, state) { packWords?.let { wordEntries(it, state.words) }.orEmpty() }

    // Null for Words while the pack isn't there: the tab then shows a dash.
    val counts = Layer.entries.associateWith { layer ->
        if (layer == Layer.WORDS) {
            coverage?.let { LayerCount(it.knownWords, it.countedWords) }
        } else {
            val items = itemsByLayer.getValue(layer)
            LayerCount(items.count { it.concept.id in known }, items.size)
        }
    }

    // Opens on the first layer with something left to learn; rememberSaveable keeps the
    // learner's choice when the screen rotates.
    var selected by rememberSaveable(verse.id) {
        mutableStateOf(
            Layer.entries.firstOrNull { layer -> counts[layer]?.let { it.known < it.total } == true } ?: Layer.READ,
        )
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Header(verse, state, packWords, known, arabicFont)
        }

        item {
            LayerTabs(selected, counts, onSelect = { selected = it })
        }

        if (selected == Layer.WORDS) {
            if (coverage == null) {
                item { WordsNeedPack(packState) }
            } else {
                item { WordCoverageLine(coverage) }
                for (entry in entries) {
                    item(key = "word-${entry.lemma.lemma.lemmaId}") {
                        WordRow(
                            entry = entry,
                            isKnown = entry.lemma.itemId in known,
                            onKnownChange = { isKnown -> scope.launch { progress.setKnown(entry.lemma.itemId, isKnown) } },
                            arabicFont = arabicFont,
                        )
                    }
                }
            }
        } else {
            for (conceptItem in itemsByLayer.getValue(selected)) {
                item(key = conceptItem.concept.id) {
                    ConceptRow(
                        item = conceptItem,
                        isKnown = conceptItem.concept.id in known,
                        onKnownChange = { isKnown ->
                            scope.launch { progress.setKnown(conceptItem.concept.id, isKnown) }
                        },
                        onOpen = {
                            context.startActivity(ActivityConcept.intent(context, conceptItem.concept.id))
                        },
                        arabicFont = arabicFont,
                    )
                }
            }
        }

        item {
            Text(
                // Reading and tajweed come from the marks in the text; words from the pack's sources.
                text = stringResource(
                    if (selected == Layer.WORDS) R.string.learning_words_disclaimer else R.string.learning_analysis_disclaimer,
                ),
                style = typography.labelSmall,
                color = colorScheme.onSurface.alpha(0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

/** One tab per layer, each with how many of its items the learner knows: "Recite 1/2". */
@Composable
private fun LayerTabs(
    selected: Layer,
    counts: Map<Layer, LayerCount?>,
    onSelect: (Layer) -> Unit,
) {
    PrimaryTabRow(selectedTabIndex = selected.ordinal, containerColor = colorScheme.surface) {
        Layer.entries.forEach { layer ->
            val count = counts[layer]
            Tab(
                selected = layer == selected,
                onClick = { onSelect(layer) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(layer.labelRes), style = typography.labelLarge)
                        Text(
                            text = when {
                                count == null -> "–"
                                count.known == count.total ->
                                    stringResource(R.string.learning_layer_done, count.known, count.total)
                                else -> stringResource(R.string.learning_layer_count, count.known, count.total)
                            },
                            style = typography.labelSmall,
                        )
                    }
                },
                unselectedContentColor = colorScheme.onSurface.alpha(0.7f),
            )
        }
    }
}

@Composable
private fun Header(
    verse: VerseWithDetails,
    state: UnderstandAyahState,
    packWords: List<AyahWord>?,
    known: Set<String>,
    arabicFont: FontFamily,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.learning_understand_title),
            style = typography.titleMedium,
        )
        Text(
            text = stringResource(
                R.string.strLabelVerseWithChapNameAndNo,
                verse.chapter.getCurrentName(),
                verse.chapterNo,
                verse.verseNo,
            ),
            style = typography.labelMedium,
            color = colorScheme.onSurface.alpha(0.8f),
        )
        if (packWords != null) {
            // Word by word, with meanings, once the learning pack is there.
            Box(Modifier.padding(top = 8.dp)) {
                InterlinearAyah(state.words, packWords, known, arabicFont)
            }
        } else {
            Text(
                text = state.ayahText,
                fontFamily = arabicFont,
                style = typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ConceptRow(
    item: ConceptItem,
    isKnown: Boolean,
    onKnownChange: (Boolean) -> Unit,
    onOpen: () -> Unit,
    arabicFont: FontFamily,
) {
    // Two tap targets: the row opens the concept's learning page, the checkbox marks it known.
    // onClickLabel is what screen readers announce: "double-tap to open lesson".
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.learning_open_lesson), onClick = onOpen)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
    ) {
        ConceptDetails(
            item = item,
            arabicFont = arabicFont,
            modifier = Modifier
                .weight(1f)
                .alpha(if (isKnown) 0.55f else 1f),
        )
        Checkbox(checked = isKnown, onCheckedChange = onKnownChange, modifier = Modifier.padding(4.dp))
    }
}

@Composable
private fun ConceptDetails(item: ConceptItem, arabicFont: FontFamily, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(item.concept.titleRes),
                style = typography.titleSmall,
            )
            OpenChevron()
        }
        Text(
            text = stringResource(item.concept.summaryRes),
            style = typography.bodySmall,
            color = colorScheme.onSurface.alpha(0.75f),
        )

        if (item.inEveryWord) {
            Text(
                text = stringResource(R.string.learning_in_every_word),
                style = typography.labelSmall,
                color = colorScheme.primary,
            )
        } else if (item.words.isNotEmpty()) {
            Text(
                text = item.words.joinToString("   "),
                fontFamily = arabicFont,
                style = typography.titleMedium,
                color = colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.learning_needed_as_foundation),
                style = typography.labelSmall,
                color = colorScheme.onSurface.alpha(0.6f),
            )
        }
    }
}
