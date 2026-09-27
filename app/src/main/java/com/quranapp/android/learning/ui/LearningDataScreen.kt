package com.quranapp.android.learning.ui

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.components.dialogs.AlertDialog
import com.quranapp.android.compose.components.dialogs.AlertDialogAction
import com.quranapp.android.compose.components.dialogs.AlertDialogActionStyle
import com.quranapp.android.compose.components.settings.SettingsItem
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.pack.LearningPackRelease
import com.quranapp.android.learning.pack.LearningPackState
import com.quranapp.android.learning.pack.PackCreditEntity
import kotlinx.coroutines.launch

/**
 * Settings → Learning data: download or delete the learning pack, and see where its data
 * comes from. Every source's licence and notice is shown here, as the licences require.
 */
@Composable
fun LearningDataScreen() {
    val context = LocalContext.current
    val state by LearningPackManager.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { LearningPackManager.refresh(context) }

    // The installed pack's own credits, with full notices. Loaded again after it's installed.
    val credits by produceState<List<PackCreditEntity>?>(initialValue = null, state) {
        value = if (state == LearningPackState.Installed) {
            LearningPackManager.database(context)?.dao()?.credits()
        } else {
            null
        }
    }

    Scaffold(topBar = { AppBar(title = stringResource(R.string.learning_data_title)) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PackCard(
                    state = state,
                    onDownload = { LearningPackManager.download(context) },
                    onCancel = { LearningPackManager.cancelDownload() },
                    onDelete = { confirmDelete = true },
                )
            }

            item {
                Text(
                    text = stringResource(R.string.learning_data_sources),
                    style = typography.titleSmall,
                    modifier = Modifier.heading(),
                )
            }

            val installedCredits = credits
            if (installedCredits != null) {
                items(installedCredits, key = { it.creditId }) { CreditRow(it) }
            } else {
                item { Text(stringResource(R.string.learning_data_source_corpus), style = typography.bodyMedium) }
                item { Text(stringResource(R.string.learning_data_source_masaq), style = typography.bodyMedium) }
            }

            item {
                Text(
                    text = stringResource(R.string.learning_data_security),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    AlertDialog(
        isOpen = confirmDelete,
        onClose = { confirmDelete = false },
        title = stringResource(R.string.learning_data_delete_title),
        actions = listOf(
            AlertDialogAction(stringResource(R.string.learning_data_keep)),
            AlertDialogAction(stringResource(R.string.learning_data_delete), AlertDialogActionStyle.Danger) {
                scope.launch { LearningPackManager.delete(context) }
            },
        ),
    ) {
        Text(stringResource(R.string.learning_data_delete_text), style = typography.bodyMedium)
    }
}

@Composable
private fun PackCard(
    state: LearningPackState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    fun size(bytes: Long) = Formatter.formatShortFileSize(context, bytes)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.learning_data_what), style = typography.bodyLarge)
        Text(
            text = stringResource(
                R.string.learning_data_size,
                size(LearningPackRelease.DOWNLOAD_BYTES),
                size(LearningPackRelease.PACK_BYTES),
            ),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )

        when (state) {
            LearningPackState.NotInstalled -> Button(onClick = onDownload) {
                Text(stringResource(R.string.learning_data_download))
            }

            is LearningPackState.Downloading -> {
                LinearProgressIndicator(
                    progress = { state.bytes.toFloat() / state.total },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.learning_data_downloading, size(state.bytes), size(state.total)),
                        style = typography.bodyMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 10.dp),
                    )
                    OutlinedButton(onClick = onCancel) { Text(stringResource(R.string.learning_data_cancel)) }
                }
            }

            LearningPackState.Installing -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.learning_data_installing), style = typography.bodyMedium)
            }

            LearningPackState.Installed -> {
                Text(
                    text = stringResource(R.string.learning_data_installed),
                    style = typography.labelLarge,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colorScheme.primary.alpha(0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                OutlinedButton(onClick = onDelete) { Text(stringResource(R.string.learning_data_delete)) }
            }

            is LearningPackState.Failed -> {
                Text(
                    text = stringResource(R.string.learning_data_failed, state.reason),
                    style = typography.bodyMedium,
                    color = colorScheme.error,
                )
                Button(onClick = onDownload) { Text(stringResource(R.string.learning_data_retry)) }
            }
        }
    }
}

@Composable
private fun CreditRow(credit: PackCreditEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(credit.name, style = typography.titleSmall)
        Text("${credit.licence} · ${credit.url}", style = typography.bodySmall, color = colorScheme.primary)
        Text(credit.notice, style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
    }
}

/** The entry in the main settings list, saying whether the pack is downloaded. */
@Composable
fun LearningDataSettingsItem(onClick: () -> Unit) {
    val context = LocalContext.current
    val state by LearningPackManager.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { LearningPackManager.refresh(context) }

    SettingsItem(
        title = R.string.learning_data_title,
        subtitle = if (state == LearningPackState.Installed) {
            R.string.learning_data_summary_installed
        } else {
            R.string.learning_data_summary_not_installed
        },
        icon = R.drawable.ic_graduation_cap,
        onClick = onClick,
    )
}
