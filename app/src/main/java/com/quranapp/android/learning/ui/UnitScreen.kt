package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackState
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.LayerProgress
import com.quranapp.android.learning.path.PathRepository
import com.quranapp.android.learning.path.Readiness
import com.quranapp.android.learning.path.SurahNeeds
import com.quranapp.android.learning.words.WordItems
import com.quranapp.android.learning.words.WordLemma
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import com.quranapp.android.utils.reader.factory.ReaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How many of a unit's words to list at once, most frequent in the surah first. */
private const val WORDS_SHOWN = 10

/** How many words one "Check yourself" asks about: three questions each. */
private const val WORDS_PER_CHECK = 4

/** A surah unit (decision 2): what the surah needs, layer by layer, and how ready you are. */
@Composable
fun UnitScreen(surahNo: Int, focus: Layer? = null) {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    // Null until the database answers, so the word list below isn't built from an empty set.
    val known by remember { progress.knownConceptIds.map<Set<String>, Set<String>?> { it } }
        .collectAsStateWithLifecycle(initialValue = null)
    val packState by LearningPackManager.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { LearningPackManager.refresh(context) }
    val installed = packState == LearningPackState.Installed

    val unit by produceState<UnitHeader?>(initialValue = null, surahNo, installed) {
        value = withContext(Dispatchers.IO) { loadUnit(context, surahNo) }
    }

    Scaffold(
        topBar = { AppBar(title = unit?.name.orEmpty()) },
    ) { padding ->
        val loaded = unit
        val knownIds = known
        if (loaded == null || knownIds == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        UnitContent(loaded, knownIds, packState, focus, Modifier.padding(padding))
    }
}

/** Everything the screen shows that doesn't change while it's open. */
private data class UnitHeader(val name: String, val ayahCount: Int, val firstAyah: String, val needs: SurahNeeds)

private suspend fun loadUnit(context: android.content.Context, surahNo: Int): UnitHeader {
    val quran = DatabaseProvider.getQuranRepository(context)
    val firstAyah = quran.getWordsForAyahById(surahNo * 1000 + 1, QuranScriptUtils.SCRIPT_UTHMANI)
        .dropLast(1) // the ayah number
        .joinToString(" ") { it.text }
    return UnitHeader(
        name = quran.getChapterName(surahNo),
        ayahCount = quran.getChapterVerseCount(surahNo),
        firstAyah = firstAyah,
        needs = PathRepository.get(context).needs(surahNo),
    )
}

private enum class UnitStep(val layer: Layer) { READ(Layer.READ), RECITE(Layer.RECITE), WORDS(Layer.WORDS) }

@Composable
private fun UnitContent(unit: UnitHeader, known: Set<String>, packState: LearningPackState, focus: Layer?, modifier: Modifier) {
    val context = LocalContext.current
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    val progress = remember(unit, known) { Layer.entries.associateWith { Readiness.of(listOf(unit.needs), it, known) } }
    fun isDone(step: UnitStep) = progress[step.layer]?.let { it.known == it.total } ?: false

    // The asked-for step, or the first with something left, is open to start with; any other can be opened.
    val current = UnitStep.entries.firstOrNull { it.layer == focus } ?: UnitStep.entries.firstOrNull { !isDone(it) }
    var open by rememberSaveable(unit.needs.surahNo) { mutableStateOf(current) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card {
                Text(
                    text = unit.firstAyah,
                    fontFamily = arabicFont,
                    style = typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = unit.name + " · " + pluralStringResource(R.plurals.learning_unit_ayahs, unit.ayahCount, unit.ayahCount),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = { ReaderFactory.startChapter(context, unit.needs.surahNo) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.learning_unit_listen)) }
            }
        }

        item { Card { ReadinessBars(progress) } }

        UnitStep.entries.forEachIndexed { index, step ->
            item(key = step.name) {
                StepCard(
                    number = index + 1,
                    step = step,
                    done = isDone(step),
                    isOpen = open == step,
                    onToggle = { open = if (open == step) null else step },
                    unit = unit,
                    known = known,
                    packState = packState,
                    arabicFont = arabicFont,
                )
            }
        }
    }
}

/** Decision 9: one bar per layer, so the learner sees where the gap is. */
@Composable
internal fun ReadinessBars(progress: Map<Layer, LayerProgress?>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.learning_unit_readiness), style = typography.titleSmall)
        progress.forEach { (layer, layerProgress) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(layer.labelRes), style = typography.bodyMedium, modifier = Modifier.width(64.dp))
                LinearProgressIndicator(
                    progress = { layerProgress?.fraction ?: 0f },
                    trackColor = colorScheme.primary.alpha(0.15f),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = when {
                        layerProgress == null -> "—"
                        layer == Layer.WORDS -> stringResource(R.string.learning_readiness_percent, layerProgress.percent)
                        else -> stringResource(R.string.learning_layer_count, layerProgress.known, layerProgress.total)
                    },
                    style = typography.labelLarge,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    number: Int,
    step: UnitStep,
    done: Boolean,
    isOpen: Boolean,
    onToggle: () -> Unit,
    unit: UnitHeader,
    known: Set<String>,
    packState: LearningPackState,
    arabicFont: FontFamily,
) {
    val concepts = unit.needs.concepts.filter { it.track == step.layer.track }
    val unknownWords = remember(unit.needs, known) { Readiness.unknownWords(unit.needs, known) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .then(if (isOpen && !done) Modifier.border(1.5.dp, colorScheme.primary, RoundedCornerShape(16.dp)) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel = stringResource(if (isOpen) R.string.learning_unit_close_step else R.string.learning_unit_open_step),
                    onClick = onToggle,
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepNumber(number, done)
            Column(Modifier.weight(1f)) {
                Text(stringResource(step.layer.labelRes), style = typography.titleSmall)
                Text(
                    text = stepSummary(step, done, concepts.count { it.id !in known }, unknownWords.size, unit.needs.words == null),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            // Points down when the step can be opened, up when it can be closed.
            Icon(
                painter = painterResource(R.drawable.dr_icon_chevron_down),
                contentDescription = null,
                tint = colorScheme.onSurface.alpha(0.5f),
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (isOpen) 180f else 0f),
            )
        }
        if (isOpen) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (step) {
                    UnitStep.READ, UnitStep.RECITE -> ConceptChips(concepts, known)
                    UnitStep.WORDS -> if (unit.needs.words == null) {
                        WordsNeedPack(packState)
                    } else {
                        UnitWords(unknownWords, known, arabicFont)
                    }
                }
            }
        }
    }
}

@Composable
private fun stepSummary(step: UnitStep, done: Boolean, unknownConcepts: Int, unknownWords: Int, needsPack: Boolean): String = when {
    step == UnitStep.WORDS && needsPack -> stringResource(R.string.learning_unit_words_need_pack)
    done -> stringResource(
        when (step) {
            UnitStep.READ -> R.string.learning_unit_read_done
            UnitStep.RECITE -> R.string.learning_unit_recite_done
            UnitStep.WORDS -> R.string.learning_unit_words_done
        },
    )
    step == UnitStep.READ -> pluralStringResource(R.plurals.learning_unit_read_left, unknownConcepts, unknownConcepts)
    step == UnitStep.RECITE -> pluralStringResource(R.plurals.learning_unit_recite_left, unknownConcepts, unknownConcepts)
    else -> pluralStringResource(R.plurals.learning_unit_words_left, unknownWords, unknownWords)
}

@Composable
private fun StepNumber(number: Int, done: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (done) colorScheme.primary else colorScheme.primary.alpha(0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (done) "✓" else number.toString(),
            style = typography.labelLarge,
            color = if (done) colorScheme.onPrimary else colorScheme.primary,
        )
    }
}

/** The step's concepts in learning order: known ones ticked, the rest open their page. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConceptChips(concepts: List<Concept>, known: Set<String>) {
    val context = LocalContext.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        concepts.forEach { concept ->
            ConceptChip(concept, isKnown = concept.id in known) {
                context.startActivity(ActivityConcept.intent(context, concept.id))
            }
        }
    }
}

/**
 * The surah's words still to learn, most frequent in it first. The list is fixed while the
 * screen is open, so a word ticked as known stays in place with its tick.
 */
@Composable
private fun UnitWords(unknownNow: List<String>, known: Set<String>, arabicFont: FontFamily) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val shownKeys = rememberSaveable { unknownNow.take(WORDS_SHOWN).mapNotNull(WordItems::lemmaKeyOf) }
    val words by produceState<List<WordLemma>?>(initialValue = null, shownKeys) {
        value = withContext(Dispatchers.IO) { WordRepository.open(context)?.wordLemmas(shownKeys) }
    }
    val loaded = words ?: return

    loaded.forEach { word ->
        UnitWordRow(
            word = word,
            isKnown = word.itemId in known,
            onKnownChange = { scope.launch { progress.setKnown(word.itemId, it) } },
            arabicFont = arabicFont,
        )
    }
    val more = unknownNow.size - shownKeys.size
    if (more > 0) {
        Text(
            text = pluralStringResource(R.plurals.learning_unit_more_words, more, more),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
    }
    val toCheck = loaded.filter { it.itemId !in known && it.lemma.gloss != null }.take(WORDS_PER_CHECK)
    if (toCheck.isNotEmpty()) {
        OutlinedButton(
            onClick = {
                context.startActivity(ActivityPractice.intent(context, toCheck.map { it.itemId }, PracticeMode.CHECK))
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(pluralStringResource(R.plurals.learning_unit_check_words, toCheck.size, toCheck.size))
        }
    }
}

@Composable
private fun UnitWordRow(word: WordLemma, isKnown: Boolean, onKnownChange: (Boolean) -> Unit, arabicFont: FontFamily) {
    val context = LocalContext.current
    val root = word.root
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (root != null) {
                    Modifier.clickable(onClickLabel = stringResource(R.string.learning_open_root)) {
                        context.startActivity(ActivityRoot.intent(context, root.rootKey))
                    }
                } else {
                    Modifier
                },
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isKnown, onCheckedChange = onKnownChange)
        Column(Modifier.weight(1f)) {
            Text(word.lemma.gloss ?: lemmaKind(word.lemma), style = typography.titleSmall)
            Text(
                text = pluralStringResource(R.plurals.learning_words_times, word.lemma.occurrences, word.lemma.occurrences),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = word.lemma.headword,
            fontFamily = arabicFont,
            style = typography.headlineSmall,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}
