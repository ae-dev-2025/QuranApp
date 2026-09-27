package com.quranapp.android.learning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.examples.LemmaExampleFinder
import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.words.RootWithLemmas
import com.quranapp.android.learning.words.WordItems
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Every dictionary word of one root, most frequent first: learn one and the root helps with
 * the rest (decision 5). Counts come from the Quranic Arabic Corpus, via the learning pack.
 */
@Composable
fun RootScreen(rootKey: String) {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known by progress.knownConceptIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }

    // Loading until the pack answers; Missing if the pack isn't downloaded or has no such root.
    val page by produceState<RootPage>(initialValue = RootPage.Loading, rootKey) {
        value = withContext(Dispatchers.IO) {
            WordRepository.open(context)?.root(rootKey)?.let(RootPage::Loaded) ?: RootPage.Missing
        }
    }

    val loaded = (page as? RootPage.Loaded)?.content
    var openLemmaId by rememberSaveable(rootKey) { mutableStateOf<Int?>(null) }
    Scaffold(
        topBar = { AppBar(title = stringResource(R.string.learning_root_title, loaded?.root?.letters.orEmpty())) },
    ) { padding ->
        if (loaded == null) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                if (page == RootPage.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.learning_word_needs_pack), textAlign = TextAlign.Center)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surface)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.learning_root_label).uppercase(),
                        style = typography.labelLarge,
                        color = colorScheme.primary,
                    )
                    Text(loaded.root.letters, fontFamily = arabicFont, style = typography.displaySmall)
                    Text(
                        text = pluralStringResource(R.plurals.learning_words_times, loaded.root.occurrences, loaded.root.occurrences) +
                            " · " + pluralStringResource(R.plurals.learning_root_lemma_count, loaded.lemmas.size, loaded.lemmas.size),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            items(loaded.lemmas, key = { it.lemmaId }) { lemma ->
                val itemId = WordItems.idOf(lemma.lemmaKey)
                // Open at most one word's examples at a time; the most frequent one to start with.
                val isOpen = openLemmaId == lemma.lemmaId
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RootLemmaRow(
                        lemma = lemma,
                        isKnown = itemId in known,
                        isOpen = isOpen,
                        onKnownChange = { scope.launch { progress.setKnown(itemId, it) } },
                        onToggleOpen = { openLemmaId = if (isOpen) null else lemma.lemmaId },
                        arabicFont = arabicFont,
                    )
                    if (isOpen) {
                        // Meaning questions need a meaning to ask about.
                        if (lemma.gloss != null) CheckYourselfButton(itemId)
                        LemmaExamples(lemma.lemmaId)
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.learning_words_disclaimer),
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private sealed interface RootPage {
    data object Loading : RootPage
    data object Missing : RootPage
    data class Loaded(val content: RootWithLemmas) : RootPage
}

@Composable
private fun LemmaExamples(lemmaId: Int) {
    val context = LocalContext.current
    val repository = remember { DatabaseProvider.getQuranRepository(context) }
    val finder = remember {
        LemmaExampleFinder(
            occurrences = { id -> WordRepository.open(context)?.occurrences(id).orEmpty() },
            loadAyah = { ayahId ->
                repository.getWordsForAyahById(ayahId, QuranScriptUtils.SCRIPT_UTHMANI).map { it.text }
            },
        )
    }
    ExamplesSection(key = "lemma-$lemmaId") { limit -> finder.find(lemmaId, limit) }
}

@Composable
private fun RootLemmaRow(
    lemma: LemmaEntity,
    isKnown: Boolean,
    isOpen: Boolean,
    onKnownChange: (Boolean) -> Unit,
    onToggleOpen: () -> Unit,
    arabicFont: FontFamily,
) {
    // Two tap targets: the row shows or hides the word's examples, the checkbox marks it known.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isOpen) colorScheme.primary.alpha(0.08f) else colorScheme.surface)
            .clickable(onClickLabel = stringResource(R.string.learning_show_examples), onClick = onToggleOpen)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KnownCheckbox(isKnown, onKnownChange, lemma.gloss ?: lemmaKind(lemma))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isKnown) 0.7f else 1f),
        ) {
            Text(lemma.gloss ?: lemmaKind(lemma), style = typography.titleSmall)
            Text(
                text = listOfNotNull(
                    lemmaKind(lemma).takeIf { lemma.gloss != null },
                    pluralStringResource(R.plurals.learning_words_times, lemma.occurrences, lemma.occurrences),
                ).joinToString(" · "),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = lemma.headword,
            fontFamily = arabicFont,
            style = typography.headlineSmall,
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

/** "Verb, form X", "Noun", "Adjective"…: what kind of word a lemma is, from the corpus's tag. */
@Composable
internal fun lemmaKind(lemma: LemmaEntity): String {
    val form = lemma.verbForm
    if (lemma.pos == "V") {
        return if (form == null || form == "I") {
            stringResource(R.string.learning_pos_verb)
        } else {
            stringResource(R.string.learning_pos_verb_form, form)
        }
    }
    return stringResource(posLabel(lemma.pos))
}

@StringRes
private fun posLabel(pos: String): Int = when (pos) {
    "N" -> R.string.learning_pos_noun
    "ADJ" -> R.string.learning_pos_adjective
    "PN" -> R.string.learning_pos_name
    "T" -> R.string.learning_pos_time
    "LOC" -> R.string.learning_pos_place
    "PRON", "DEM", "REL" -> R.string.learning_pos_pronoun
    else -> R.string.learning_pos_particle
}
