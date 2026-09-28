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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
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
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.SurahNeeds
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackState
import com.quranapp.android.learning.words.AyahWord
import com.quranapp.android.learning.words.WordCoverage
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.repository.QuranRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import com.quranapp.android.utils.reader.factory.ReaderFactory
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
    /** Where [words] are in the ayah (0-based), to draw them in the reader's script. */
    val wordIndexes: List<Int> = emptyList(),
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
            LearningStyle { SheetContent(verse, loaded) }
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
            wordIndexes = wordIndexes,
        )
    }

    return UnderstandAyahState(ayahText = words.joinToString(" "), words = words.dropLast(1), items = items)
}

/**
 * The ayah's grammar concepts in learning order, with the grammar they build on, and the
 * words each is found in. [found] maps concepts to 0-based word indexes, as the pack gives them.
 */
private fun grammarItems(found: Map<String, List<Int>>, words: List<String>): List<ConceptItem> =
    SurahNeeds.grammarOf(found.keys).map { concept ->
        val indexes = found[concept.id].orEmpty().filter { it in words.indices }
        ConceptItem(
            concept = concept,
            words = indexes.map { words[it] },
            inEveryWord = words.size > 1 && indexes.size == words.size,
            wordIndexes = indexes,
        )
    }

/** How many of a layer's items the learner knows. */
private data class LayerCount(val known: Int, val total: Int)

@Composable
private fun SheetContent(verse: VerseWithDetails, state: UnderstandAyahState) {
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    // The reader's script (Indo-Pak), or null for the app's Uthmani text.
    val script = rememberScriptAyah(verse.chapterNo, verse.verseNo)
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }

    // The set of known concept IDs. Whenever the database changes, the Flow emits a new set
    // and Compose redraws everything that reads `known`.
    val known by progress.knownConceptIds.collectAsState(initial = emptySet())

    // Tied to this composable: if the sheet closes, unfinished work is cancelled.
    val scope = rememberCoroutineScope()

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
    // Its grammar, from the pack's analysis of each word; null without the pack.
    val packGrammar by produceState<Map<String, List<Int>>?>(initialValue = null, verse.id, installed) {
        value = if (installed) {
            withContext(Dispatchers.IO) { WordRepository.open(context)?.grammarOfAyah(verse.id) }
        } else {
            null
        }
    }
    val grammarItems = remember(packGrammar, state) { packGrammar?.let { grammarItems(it, state.words) } }
    val itemsByLayer = remember(state, grammarItems) {
        Layer.entries.associateWith { layer ->
            if (layer == Layer.GRAMMAR) grammarItems.orEmpty() else state.items.filter { it.concept.track == layer.track }
        }
    }
    val coverage = packWords?.let { WordCoverage.of(it, known) }
    val entries = remember(packWords, state) { packWords?.let { wordEntries(it, state.words) }.orEmpty() }

    // Null for Words and Grammar while the pack isn't there: the tab then shows a dash.
    val counts = Layer.entries.associateWith { layer ->
        if (layer == Layer.WORDS) {
            coverage?.let { LayerCount(it.knownWords, it.countedWords) }
        } else if (layer == Layer.GRAMMAR && grammarItems == null) {
            null
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

    // The Grammar tab's view: its concepts, or each word's role.
    var grammarByWord by rememberSaveable(verse.id) { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Header(verse, state, packWords, known, arabicFont, script)
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
                            script = script,
                            ayahWords = state.words,
                        )
                    }
                }
            }
        } else if (selected == Layer.GRAMMAR && grammarItems == null) {
            item { WordsNeedPack(packState, R.string.learning_grammar_need_pack) }
        } else if (selected == Layer.GRAMMAR && grammarByWord) {
            item(key = "grammar-view") { GrammarViewSwitch(byWord = true) { grammarByWord = it } }
            wordRoleItems(packWords.orEmpty(), state.words, arabicFont, script) {
                ReaderFactory.startTafsir(context, verse.chapterNo, verse.verseNo)
            }
        } else {
            if (selected == Layer.GRAMMAR) {
                item(key = "grammar-view") { GrammarViewSwitch(byWord = false) { grammarByWord = it } }
            }
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
                        script = script,
                        ayahWords = state.words,
                    )
                }
            }
        }

        item {
            Text(
                // Reading and tajweed come from the marks in the text; words and grammar from the pack's sources.
                text = stringResource(
                    when (selected) {
                        Layer.WORDS -> R.string.learning_words_disclaimer
                        Layer.GRAMMAR -> R.string.learning_grammar_disclaimer
                        else -> R.string.learning_analysis_disclaimer
                    },
                ),
                style = typography.labelSmall,
                color = colorScheme.onSurface.alpha(0.7f),
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
    script: ScriptAyah?,
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
            modifier = Modifier.heading(),
        )
        Text(
            text = ayahReference(verse.chapter.getCurrentName(), verse.chapterNo, verse.verseNo),
            style = typography.labelMedium,
            color = colorScheme.onSurface.alpha(0.8f),
        )
        if (packWords != null) {
            // Word by word, with meanings, once the learning pack is there.
            Box(Modifier.padding(top = 8.dp)) {
                InterlinearAyah(state.words, packWords, known, arabicFont, script)
            }
        } else if (script != null) {
            val color = colorScheme.onSurface
            LearningWords(script, state.words, state.words.indices, arabicFont, typography.titleLarge, color = { color }, Modifier.padding(top = 8.dp))
        } else {
            Text(
                text = state.ayahText,
                fontFamily = arabicFont,
                style = typography.titleLarge.copy(textDirection = TextDirection.Rtl),
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
    script: ScriptAyah?,
    ayahWords: List<String>,
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
            script = script,
            ayahWords = ayahWords,
            modifier = Modifier
                .weight(1f)
                .alpha(if (isKnown) 0.7f else 1f),
        )
        KnownCheckbox(isKnown, onKnownChange, conceptTitle(item.concept))
    }
}

@Composable
private fun ConceptDetails(item: ConceptItem, arabicFont: FontFamily, script: ScriptAyah?, ayahWords: List<String>, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = conceptTitle(item.concept)
            val termColor = colorScheme.primary
            Text(
                // Grammar shows the Arabic term too (decision 6: English and Arabic). One text,
                // so a long title wraps and the chevron stays at its end.
                text = buildAnnotatedString {
                    append(title)
                    item.concept.arabicTerm?.let { term ->
                        append("  ")
                        withStyle(SpanStyle(color = termColor)) { append(term) }
                    }
                },
                style = typography.titleSmall,
                modifier = Modifier.weight(1f, fill = false),
            )
            OpenChevron()
        }
        Text(
            text = arabicExamplesInOrder(stringResource(item.concept.summaryRes)),
            style = typography.bodySmall,
            color = colorScheme.onSurface.alpha(0.75f),
        )

        if (item.inEveryWord) {
            Text(
                text = stringResource(R.string.learning_in_every_word),
                style = typography.labelSmall,
                color = colorScheme.primary,
            )
        } else if (item.words.isNotEmpty() && script != null) {
            val primary = colorScheme.primary
            LearningWords(
                script = script,
                words = ayahWords,
                indexes = item.wordIndexes,
                arabicFont = arabicFont,
                style = typography.titleMedium,
                color = { primary },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                alignment = Alignment.End, // where the Uthmani words sit: at the start of the English
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
                color = colorScheme.onSurface.alpha(0.7f),
            )
        }
    }
}
