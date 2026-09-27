package com.quranapp.android.learning.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.components.common.RadioItem
import com.quranapp.android.compose.components.dialogs.AlertDialog
import com.quranapp.android.compose.components.dialogs.AlertDialogAction
import com.quranapp.android.compose.components.dialogs.AlertDialogActionStyle
import com.quranapp.android.compose.components.settings.SettingsItem
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.path.Curriculum
import com.quranapp.android.learning.path.LearningPreferences
import com.quranapp.android.learning.progress.LearningBackupRepository
import kotlinx.coroutines.launch

/** Settings → Learning: new words a day, where you start, learning data, and reset. */
@Composable
fun LearningSettingsScreen(onOpenLearningData: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val perDay by remember { LearningPreferences.newWordsPerDay() }.collectAsStateWithLifecycle(initialValue = null)
    val start by remember { LearningPreferences.startStage() }.collectAsStateWithLifecycle(initialValue = null)
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(topBar = { AppBar(title = stringResource(R.string.learning_settings_title)) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item { SectionTitle(R.string.learning_settings_new_words) }
            item {
                Column(Modifier.selectableGroup()) {
                    LearningPreferences.NEW_WORDS_CHOICES.forEach { count ->
                        RadioItem(
                            titleStr = if (count == 0) stringResource(R.string.learning_settings_new_words_off) else count.toString(),
                            selected = perDay == count,
                        ) { scope.launch { LearningPreferences.setNewWordsPerDay(count) } }
                    }
                }
            }
            item { Hint(R.string.learning_settings_new_words_text) }

            item { SectionTitle(R.string.learning_settings_start) }
            item {
                val stage = start?.let { number -> Curriculum.stages.firstOrNull { it.number == number } }
                Text(
                    text = stage?.let { stringResource(R.string.learning_stage_title, it.number, stringResource(it.titleRes)) }
                        ?: stringResource(R.string.learning_settings_start_not_chosen),
                    style = typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            if (start != null && start != LearningPreferences.NOT_CHOSEN) {
                item {
                    OutlinedButton(
                        onClick = { scope.launch { LearningPreferences.setStartStage(LearningPreferences.NOT_CHOSEN) } },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.learning_change_start)) }
                }
            }

            item { SectionTitle(R.string.learning_data_title) }
            item { LearningDataSettingsItem(onOpenLearningData) }

            item { SectionTitle(R.string.learning_settings_reset) }
            item { Hint(R.string.learning_settings_reset_text) }
            item {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.learning_settings_reset), color = colorScheme.error) }
            }
        }
    }

    AlertDialog(
        isOpen = confirmReset,
        onClose = { confirmReset = false },
        title = stringResource(R.string.learning_settings_reset_confirm_title),
        actions = listOf(
            AlertDialogAction(stringResource(R.string.learning_settings_keep)),
            AlertDialogAction(stringResource(R.string.learning_settings_reset_action), AlertDialogActionStyle.Danger) {
                scope.launch {
                    LearningBackupRepository(DatabaseProvider.getUserDatabase(context)).resetAll()
                    Toast.makeText(context, R.string.learning_settings_reset_done, Toast.LENGTH_SHORT).show()
                }
            },
        ),
    ) {
        Text(stringResource(R.string.learning_settings_reset_confirm_text), style = typography.bodyMedium)
    }
}

@Composable
private fun SectionTitle(title: Int) {
    Text(
        text = stringResource(title),
        style = typography.titleSmall,
        color = colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun Hint(text: Int) {
    Text(
        text = stringResource(text),
        style = typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

/** The entry in the main settings list. */
@Composable
fun LearningSettingsItem(onClick: () -> Unit) {
    SettingsItem(
        title = R.string.learning_settings_title,
        subtitle = R.string.learning_settings_summary,
        icon = R.drawable.ic_graduation_cap,
        onClick = onClick,
    )
}
