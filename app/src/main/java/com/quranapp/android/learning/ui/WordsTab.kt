package com.quranapp.android.learning.ui

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackRelease
import com.quranapp.android.learning.pack.LearningPackState
import com.quranapp.android.learning.words.AyahWord
import com.quranapp.android.learning.words.WordCoverage
import com.quranapp.android.learning.words.WordLemma

/** One row of the Words tab: a dictionary word, with where it occurs in this ayah. */
internal data class WordEntry(
    val lemma: WordLemma,
    /** The ayah's words that contain it, as written in the ayah. */
    val texts: List<String>,
    /** Its meaning where it first occurs in this ayah. */
    val gloss: String?,
)

/** The dictionary words of an ayah, in the order they first occur. */
internal fun wordEntries(words: List<AyahWord>, ayahWords: List<String>): List<WordEntry> {
    val entries = LinkedHashMap<Int, WordEntry>()
    for (word in words) {
        val text = ayahWords.getOrNull(word.wordIndex) ?: continue
        for (lemma in word.lemmas) {
            val existing = entries[lemma.lemma.lemmaId]
            entries[lemma.lemma.lemmaId] = existing?.copy(texts = (existing.texts + text).distinct())
                ?: WordEntry(lemma, listOf(text), word.gloss)
        }
    }
    return entries.values.toList()
}

/**
 * The ayah word by word, each with its English meaning under it. Words the learner hasn't
 * learned yet have a dotted line under them. Arabic reads right to left, so the words are
 * laid out right to left whatever the app's language.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InterlinearAyah(
    ayahWords: List<String>,
    words: List<AyahWord>,
    knownItemIds: Set<String>,
    arabicFont: FontFamily,
) {
    val byIndex = words.associateBy { it.wordIndex }
    val dotted = colorScheme.error
    val appDirection = LocalLayoutDirection.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ayahWords.forEachIndexed { index, text ->
                val word = byIndex[index]
                val isNew = word != null && word.lemmas.any { it.itemId !in knownItemIds }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 120.dp),
                ) {
                    Text(
                        text = text,
                        fontFamily = arabicFont,
                        style = typography.titleLarge,
                        modifier = if (isNew) Modifier.dottedUnderline(dotted) else Modifier,
                    )
                    val gloss = word?.gloss
                    if (gloss != null) {
                        // The gloss is in the app's language, so it gets the app's direction back.
                        CompositionLocalProvider(LocalLayoutDirection provides appDirection) {
                            Text(
                                text = gloss,
                                style = typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.dottedUnderline(color: Color) = drawBehind {
    val y = size.height - 2.dp.toPx()
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx())),
    )
}

/** "You know 3 of 4 words (75%)." */
@Composable
internal fun WordCoverageLine(coverage: WordCoverage) {
    Text(
        text = if (coverage.knownWords == coverage.countedWords) {
            stringResource(R.string.learning_words_all_known)
        } else {
            stringResource(R.string.learning_words_you_know, coverage.knownWords, coverage.countedWords, coverage.percent)
        },
        style = typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** A dictionary word: its meaning, headword and root, and the ayah's words that contain it. */
@Composable
internal fun WordRow(
    entry: WordEntry,
    isKnown: Boolean,
    onKnownChange: (Boolean) -> Unit,
    arabicFont: FontFamily,
) {
    val context = LocalContext.current
    val lemma = entry.lemma.lemma
    val root = entry.lemma.root
    // Two tap targets, like the concept rows: the row opens the root's page (words without a
    // root, such as مِن, just toggle), the checkbox marks the word known.
    val rowAction = if (root != null) {
        Modifier.clickable(onClickLabel = stringResource(R.string.learning_open_root)) {
            context.startActivity(ActivityRoot.intent(context, root.rootKey))
        }
    } else {
        Modifier.toggleable(value = isKnown, role = Role.Checkbox, onValueChange = onKnownChange)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(rowAction)
            .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isKnown, onCheckedChange = onKnownChange, modifier = Modifier.padding(4.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (isKnown) 0.6f else 1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (entry.gloss != null) {
                Text(entry.gloss, style = typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(lemma.headword, fontFamily = arabicFont, style = typography.titleMedium)
                root?.let {
                    Text(it.letters, fontFamily = arabicFont, style = typography.bodyMedium, color = colorScheme.primary)
                    OpenChevron()
                }
            }
            Text(
                text = listOfNotNull(
                    lemma.gloss,
                    pluralStringResource(R.plurals.learning_words_times, lemma.occurrences, lemma.occurrences),
                ).joinToString(" · "),
                style = typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = entry.texts.joinToString("  "),
            fontFamily = arabicFont,
            style = typography.titleLarge,
            color = colorScheme.primary,
        )
    }
}

/** Shown in the Words tab until the learning pack is downloaded. */
@Composable
internal fun WordsNeedPack(state: LearningPackState) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                R.string.learning_words_need_pack,
                Formatter.formatShortFileSize(context, LearningPackRelease.DOWNLOAD_BYTES),
            ),
            style = typography.bodyMedium,
        )
        when (state) {
            is LearningPackState.Downloading -> {
                LinearProgressIndicator(
                    progress = { state.bytes.toFloat() / state.total },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(
                        R.string.learning_data_downloading,
                        Formatter.formatShortFileSize(context, state.bytes),
                        Formatter.formatShortFileSize(context, state.total),
                    ),
                    style = typography.labelMedium,
                    color = colorScheme.onSurface.alpha(0.7f),
                )
            }

            LearningPackState.Installing -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            is LearningPackState.Failed -> {
                Text(stringResource(R.string.learning_data_failed, state.reason), color = colorScheme.error)
                Button(onClick = { LearningPackManager.download(context) }) {
                    Text(stringResource(R.string.learning_data_retry))
                }
            }

            else -> Button(onClick = { LearningPackManager.download(context) }) {
                Text(stringResource(R.string.learning_data_download))
            }
        }
    }
}
