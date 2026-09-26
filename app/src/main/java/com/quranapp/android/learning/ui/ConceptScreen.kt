package com.quranapp.android.learning.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import kotlinx.coroutines.launch

/** The learning page of one concept. More sections are added in the following PRs. */
@Composable
fun ConceptScreen(concept: Concept) {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known by progress.knownConceptIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppBar(title = stringResource(concept.titleRes)) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(concept.track.labelRes),
                    style = typography.labelLarge,
                    color = colorScheme.primary,
                )
            }

            item {
                Text(
                    text = stringResource(concept.summaryRes),
                    style = typography.bodyLarge,
                )
            }

            item {
                KnownToggle(
                    isKnown = concept.id in known,
                    onKnownChange = { isKnown ->
                        scope.launch { progress.setKnown(concept.id, isKnown) }
                    },
                )
            }
        }
    }
}

@Composable
private fun KnownToggle(isKnown: Boolean, onKnownChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isKnown, role = Role.Checkbox, onValueChange = onKnownChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isKnown, onCheckedChange = null)
        Text(
            text = stringResource(R.string.learning_i_know_this),
            style = typography.bodyMedium,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
