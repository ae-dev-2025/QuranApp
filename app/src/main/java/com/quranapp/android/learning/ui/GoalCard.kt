package com.quranapp.android.learning.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.path.Layer
import com.quranapp.android.learning.path.PathProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Journey 4, "aim at a surah": how much of its words you know, and which to learn next. */
@Composable
internal fun GoalCard(goal: GoalState, onChooseSurah: () -> Unit) {
    val context = LocalContext.current
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    LearnCard(label = stringResource(R.string.learning_goal)) {
        when (goal) {
            GoalState.None -> {
                Text(stringResource(R.string.learning_goal_none), style = typography.titleMedium)
                Text(stringResource(R.string.learning_goal_none_text), style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onChooseSurah, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.learning_goal_choose))
                }
            }

            is GoalState.Chosen -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(goal.name, style = typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        text = goal.words?.let { stringResource(R.string.learning_goal_words, it.percent) }
                            ?: stringResource(R.string.learning_unit_words_need_pack),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                goal.words?.let { words ->
                    LinearProgressIndicator(
                        progress = { words.fraction },
                        trackColor = colorScheme.primary.alpha(0.15f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(
                            if (words.reaches(PathProgress.WORDS_DONE_PERCENT)) R.string.learning_goal_reached else R.string.learning_goal_aim,
                        ),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
                if (goal.nextWords.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.learning_goal_next_words), style = typography.bodyMedium)
                        Text(
                            text = goal.nextWords.joinToString("  ·  "),
                            fontFamily = arabicFont,
                            style = typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                        )
                    }
                }
                Button(
                    onClick = { context.startActivity(ActivityUnit.intent(context, goal.surahNo, focus = Layer.WORDS)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.learning_goal_learn)) }
                TextButton(onClick = onChooseSurah, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.learning_goal_change))
                }
            }
        }
    }
}

/** Every surah by number and name. Choosing one makes it the goal. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalPickerSheet(onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val names by produceState<Map<Int, String>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { DatabaseProvider.getQuranRepository(context).getChapterNames((1..114).toList()) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.learning_goal_pick_title),
            style = typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        val loaded = names
        if (loaded == null) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@ModalBottomSheet
        }
        LazyColumn {
            items((1..114).toList()) { surah ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(surah) }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = surah.toString(),
                        style = typography.labelLarge,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(grownWithText(40.dp)),
                    )
                    Text(loaded[surah].orEmpty(), style = typography.bodyLarge)
                }
            }
        }
    }
}
