package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackState
import com.quranapp.android.learning.words.AyahWord
import com.quranapp.android.learning.words.WordLemma
import com.quranapp.android.learning.words.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * "Learn this word", added to the reader's word-by-word popup (decision 4): the dictionary
 * words in the tapped word, their roots, and a way to mark them known. The popup already
 * shows the word's translation and plays its audio, so this doesn't repeat them.
 */
@Composable
fun LearnThisWord(ayahId: Int, wordIndex: Int) {
    val context = LocalContext.current
    val packState by LearningPackManager.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { LearningPackManager.refresh(context) }
    val installed = packState == LearningPackState.Installed

    val word by produceState<AyahWord?>(initialValue = null, ayahId, wordIndex, installed) {
        value = if (installed) {
            withContext(Dispatchers.IO) {
                WordRepository.open(context)?.wordsOfAyah(ayahId)?.firstOrNull { it.wordIndex == wordIndex }
            }
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceVariant.alpha(0.4f)) // like the word card above it
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.learning_learn_this_word),
            style = typography.labelLarge,
            color = colorScheme.primary,
        )

        val loaded = word
        when {
            !installed -> PackHint(packState)
            loaded == null -> Unit // loading, or a word the pack doesn't cover
            loaded.lemmas.isEmpty() -> Text(
                text = stringResource(R.string.learning_word_no_lemma),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
            else -> {
                // The popup's own translation needs a word-by-word download; this doesn't.
                loaded.gloss?.let { gloss ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.learning_word_in_this_ayah),
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Text(gloss, style = typography.titleSmall, modifier = Modifier.weight(1f))
                    }
                }
                loaded.lemmas.forEach { LemmaRow(it) }
            }
        }
    }
}

@Composable
private fun LemmaRow(lemma: WordLemma) {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known by progress.knownConceptIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    val isKnown = lemma.itemId in known

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isKnown,
                role = Role.Checkbox,
                onValueChange = { scope.launch { progress.setKnown(lemma.itemId, it) } },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.learning_word_dictionary_form),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                Text(lemma.lemma.headword, fontFamily = arabicFont, style = typography.titleLarge)
                lemma.lemma.gloss?.let { Text(it, style = typography.bodyMedium, modifier = Modifier.weight(1f, fill = false)) }
            }
            lemma.root?.let { root ->
                Row(
                    modifier = Modifier.clickable(onClickLabel = stringResource(R.string.learning_open_root)) {
                        context.startActivity(ActivityRoot.intent(context, root.rootKey))
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.learning_word_root),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                    Text(root.letters, fontFamily = arabicFont, style = typography.titleMedium, color = colorScheme.primary)
                    OpenChevron()
                }
            }
            Text(
                text = pluralStringResource(R.plurals.learning_words_times, lemma.lemma.occurrences, lemma.lemma.occurrences),
                style = typography.labelSmall,
                color = colorScheme.onSurface.alpha(0.7f),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Checkbox(checked = isKnown, onCheckedChange = null)
            Text(stringResource(R.string.learning_i_know_it), style = typography.labelSmall)
        }
    }
}

/** The popup's hint while the learning pack isn't downloaded. */
@Composable
private fun PackHint(state: LearningPackState) {
    val context = LocalContext.current
    Text(
        text = stringResource(R.string.learning_word_needs_pack),
        style = typography.bodyMedium,
        color = colorScheme.onSurfaceVariant,
    )
    when (state) {
        is LearningPackState.Downloading, LearningPackState.Installing -> Text(
            text = stringResource(R.string.learning_data_downloading_short),
            style = typography.labelMedium,
        )
        else -> TextButton(onClick = { LearningPackManager.download(context) }) {
            Text(stringResource(R.string.learning_data_download))
        }
    }
}
