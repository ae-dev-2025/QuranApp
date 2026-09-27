package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.components.reader.dialogs.QuickReference
import com.quranapp.android.compose.components.reader.dialogs.QuickReferenceData
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.analysis.ConceptIndex
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.Track
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.examples.ConceptExample
import com.quranapp.android.learning.examples.ConceptExampleFinder
import com.quranapp.android.learning.examples.GrammarExampleFinder
import com.quranapp.android.learning.words.GrammarIndex
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import com.quranapp.android.utils.reader.factory.ReaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 3

/** An example ready to show; the surah's name is looked up once while loading. */
private data class ExampleItem(val surahName: String, val example: ConceptExample)

/**
 * "In the Quran": the first examples of a concept (short surahs first) and a button to show
 * more. Tapping an example opens the app's quick-view sheet over the lesson (ayah, translation,
 * play and "open in reader"), so closing it returns here. Shows nothing when there are none,
 * e.g. for an umbrella concept such as noon sākinah.
 */
@Composable
fun ConceptExamplesSection(conceptId: String) {
    val context = LocalContext.current
    val repository = remember { DatabaseProvider.getQuranRepository(context) }
    val finder = remember {
        ConceptExampleFinder(ayahsWith = { ConceptIndex.get(context)?.ayahsOf(it) }) { surahNo ->
            repository.getWordsForSurah(surahNo, QuranScriptUtils.SCRIPT_UTHMANI)
                .map { (ayahId, words) -> AyahWords(surahNo, ayahId % 1000, words.map { it.text }) }
        }
    }
    // Grammar isn't in the text's marks: it comes from the learning pack's analysis of each word.
    val grammarFinder = remember {
        GrammarExampleFinder(
            grammarOfSurah = { surahNo -> WordRepository.open(context)?.grammarByAyahOfSurah(surahNo) },
            loadAyah = { ayahId -> repository.getWordsForAyahById(ayahId, QuranScriptUtils.SCRIPT_UTHMANI).map { it.text } },
            ayahsWith = { GrammarIndex.get(context)?.ayahsOf(it) },
            grammarOfAyah = { ayahId -> WordRepository.open(context)?.grammarOfAyah(ayahId) },
        )
    }
    val isGrammar = ConceptCatalog[conceptId]?.track == Track.GRAMMAR
    ExamplesSection(key = conceptId) { limit ->
        if (isGrammar) grammarFinder.find(conceptId, limit) else finder.find(conceptId, limit)
    }
}

/**
 * The examples list itself, shared by concepts and dictionary words: [find] returns the first
 * `limit` examples, and is called again with a bigger limit for "Show more". [key] identifies
 * what the examples are of, so a new key starts from the first page again.
 */
@Composable
internal fun ExamplesSection(key: String, find: suspend (limit: Int) -> List<ConceptExample>) {
    val context = LocalContext.current
    val repository = remember { DatabaseProvider.getQuranRepository(context) }

    // The ayah shown in the quick-view sheet; null = sheet closed.
    var quickView by remember { mutableStateOf<QuickReferenceData?>(null) }

    QuickReference(
        data = quickView,
        onOpenInReader = { chapterNo, range ->
            quickView = null
            ReaderFactory.startVerseRange(context, chapterNo, range.first, range.last)
        },
        onClose = { quickView = null },
    )

    // How many to show. "Show more" raises it; rememberSaveable keeps it across rotation.
    var limit by rememberSaveable(key) { mutableIntStateOf(PAGE_SIZE) }

    // Re-runs when `limit` changes. The previous list stays visible until the new one is ready.
    val items by produceState<List<ExampleItem>?>(initialValue = null, key, limit) {
        value = withContext(Dispatchers.Default) {
            find(limit).map { example ->
                val surah = repository.getSurahWithLocalizations(example.ayah.surahNo)
                ExampleItem(surah?.getCurrentName().orEmpty(), example)
            }
        }
    }

    val loaded = items
    if (loaded != null && loaded.isEmpty()) return

    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.learning_in_the_quran), style = typography.titleMedium, modifier = Modifier.heading())
            Text(
                text = stringResource(R.string.learning_short_surahs_first),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }

        if (loaded == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterHorizontally),
            )
            return@Column
        }

        for (item in loaded) {
            ExampleCard(item, arabicFont) {
                val ayah = item.example.ayah
                // An empty set of translation slugs means: use the reader's chosen translations.
                quickView = QuickReferenceData(emptySet(), ayah.surahNo, ayah.ayahNo.toString())
            }
        }

        // A full page means there may be more.
        if (loaded.size == limit) {
            TextButton(
                onClick = { limit += PAGE_SIZE },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.learning_show_more_examples))
            }
        }
    }
}

@Composable
private fun ExampleCard(item: ExampleItem, arabicFont: FontFamily, onOpen: () -> Unit) {
    val ayah = item.example.ayah
    val highlight = SpanStyle(color = colorScheme.primary, background = colorScheme.primary.alpha(0.12f))

    // One string with the highlighted words styled differently: an AnnotatedString.
    val text = remember(item, highlight) {
        buildAnnotatedString {
            ayah.words.forEachIndexed { index, word ->
                if (index > 0) append(" ")
                if (index in item.example.highlightedWordIndexes) {
                    withStyle(highlight) { append(word) }
                } else {
                    append(word)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .clickable(onClickLabel = stringResource(R.string.learning_view_ayah), onClick = onOpen)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                item.example.highlightedWordIndexes.minOrNull()?.let { word ->
                    HearWordButton(ayah.surahNo, ayah.ayahNo, word, Modifier.size(40.dp))
                }
                Text(
                    text = ayahReference(item.surahName, ayah.surahNo, ayah.ayahNo),
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.learning_view_ayah) + " ›",
                style = typography.labelMedium,
                color = colorScheme.primary,
            )
        }
        val script = rememberScriptAyah(ayah.surahNo, ayah.ayahNo)
        if (script == null) {
            // An ayah is right to left, so it lines up on the right.
            Text(
                text = text,
                fontFamily = arabicFont,
                style = typography.titleLarge.copy(textDirection = TextDirection.Rtl),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // In the reader's script, word by word, with the same words highlighted.
            val highlighted = item.example.highlightedWordIndexes
            val primary = colorScheme.primary
            val plain = colorScheme.onSurface
            val background = primary.alpha(0.12f)
            LearningWords(
                script = script,
                words = ayah.words,
                indexes = ayah.words.indices,
                arabicFont = arabicFont,
                style = typography.titleLarge,
                color = { if (it in highlighted) primary else plain },
                modifier = Modifier.fillMaxWidth(),
                wordModifier = { if (it in highlighted) Modifier.background(background) else Modifier },
            )
        }
    }
}
