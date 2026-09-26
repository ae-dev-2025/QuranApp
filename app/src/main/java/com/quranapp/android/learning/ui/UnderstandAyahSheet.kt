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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.components.reader.LocalReaderViewModel
import com.quranapp.android.compose.extensions.bottomBorder
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.db.relations.VerseWithDetails
import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptGraph
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.Track
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

    return UnderstandAyahState(ayahText = words.joinToString(" "), items = items)
}

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

    val knownCount = state.items.count { it.concept.id in known }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Header(verse, state, knownCount, arabicFont)
        }

        // The list is already in learning order, and all reading concepts come before
        // tajweed ones, so a heading is added whenever the track changes.
        var previousTrack: Track? = null
        for (conceptItem in state.items) {
            val track = conceptItem.concept.track
            if (track != previousTrack) {
                item(key = "track-$track") { TrackHeading(track) }
                previousTrack = track
            }
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

        item {
            Text(
                text = stringResource(R.string.learning_analysis_disclaimer),
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

@Composable
private fun Header(
    verse: VerseWithDetails,
    state: UnderstandAyahState,
    knownCount: Int,
    arabicFont: FontFamily,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bottomBorder()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
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
            ) + " · " + pluralStringResource(
                R.plurals.learning_concept_count,
                state.items.size,
                state.items.size,
            ),
            style = typography.labelMedium,
            color = colorScheme.onSurface.alpha(0.8f),
        )
        Text(
            text = state.ayahText,
            fontFamily = arabicFont,
            style = typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        val total = state.items.size
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else knownCount / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.learning_you_know, knownCount, total),
            style = typography.labelMedium,
            color = colorScheme.primary,
        )
    }
}

@Composable
private fun TrackHeading(track: Track) {
    Text(
        text = stringResource(track.labelRes),
        style = typography.labelLarge,
        color = colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
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

/** A small ">" that hints the row opens a page. It points left in right-to-left languages. */
@Composable
private fun OpenChevron() {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Icon(
        painter = painterResource(R.drawable.dr_icon_chevron_down),
        contentDescription = null, // decorative: the row's click label already says what happens
        tint = colorScheme.onSurface.alpha(0.5f),
        modifier = Modifier
            .size(18.dp)
            .rotate(if (isRtl) 90f else -90f),
    )
}
