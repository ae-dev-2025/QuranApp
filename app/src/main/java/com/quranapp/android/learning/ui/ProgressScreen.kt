package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.progress.WeekSummary

/** How far the learner has come across the whole Quran, with a map of its surahs. */
@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    Scaffold(topBar = { AppBar(title = stringResource(R.string.learning_progress_title)) }) { padding ->
        val loaded = progress
        if (loaded == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LearnCard(label = stringResource(R.string.learning_progress_words)) {
                    Text(
                        text = loaded.wordsTotal?.let { stringResource(R.string.learning_progress_words_known, loaded.wordsKnown, it) }
                            ?: stringResource(R.string.learning_unit_words_need_pack),
                        style = typography.titleMedium,
                    )
                    loaded.layers[Layer.WORDS]?.let {
                        Text(
                            text = stringResource(R.string.learning_progress_words_cover, it.percent),
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                LearnCard(label = stringResource(R.string.learning_progress_quran)) {
                    ReadinessBars(loaded.layers, title = null)
                }
            }
            item {
                LearnCard(label = stringResource(R.string.learning_progress_map)) {
                    val surahs = loaded.surahs
                    when {
                        loaded.wordsTotal == null -> Text(stringResource(R.string.learning_unit_words_need_pack), style = typography.bodyMedium)
                        surahs == null -> CircularProgressIndicator(Modifier.size(24.dp))
                        else -> QuranMap(surahs)
                    }
                    Text(stringResource(R.string.learning_progress_map_legend), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * The 114 surahs as squares, darker the more of a surah's words are known. Each opens its
 * unit on the Words step. TalkBack reads the name and the share instead of the shade.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuranMap(surahs: List<SurahCell>) {
    val context = LocalContext.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        surahs.forEach { cell ->
            val share = cell.words.fraction
            val description = stringResource(R.string.learning_progress_cell, cell.name, cell.words.percent)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colorScheme.primary.alpha(0.08f + 0.92f * share))
                    .clickable { context.startActivity(ActivityUnit.intent(context, cell.surahNo, focus = Layer.WORDS)) }
                    .clearAndSetSemantics {
                        contentDescription = description
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cell.surahNo.toString(),
                    style = typography.labelSmall,
                    color = if (share > 0.55f) colorScheme.onPrimary else colorScheme.onSurface,
                )
            }
        }
    }
}

/** This week in a few calm numbers, and the way to the progress screen. */
@Composable
internal fun WeekCard(week: WeekSummary, onOpenProgress: () -> Unit) {
    LearnCard(label = stringResource(R.string.learning_week)) {
        if (week.isEmpty) {
            Text(stringResource(R.string.learning_week_quiet), style = typography.bodyMedium)
        } else {
            val learned = listOfNotNull(
                week.newWords.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.learning_week_words, it, it) },
                week.newConcepts.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.learning_week_concepts, it, it) },
            )
            if (learned.isNotEmpty()) Text(learned.joinToString(" · "), style = typography.titleSmall)
            if (week.reviews > 0) {
                Text(
                    text = pluralStringResource(R.plurals.learning_week_reviews, week.reviews, week.reviews, week.rightReviews),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
        OutlinedButton(
            onClick = onOpenProgress,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.learning_progress_see)) }
    }
}
