package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.learning.path.Placement

/** Decision 11: three plain choices on the first visit, no test before anything else. */
@Composable
internal fun PlacementContent(modifier: Modifier = Modifier, onChoose: (Placement) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(Placement.NEW) }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.learning_place_title), style = typography.headlineSmall, modifier = Modifier.heading())
        Text(
            text = stringResource(R.string.learning_place_subtitle),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Placement.entries.forEach { placement ->
                val isSelected = placement == selected
                val shape = RoundedCornerShape(16.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colorScheme.surface)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) colorScheme.primary else colorScheme.outlineVariant, shape)
                        .selectable(selected = isSelected, role = Role.RadioButton) { selected = placement }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(placement.titleRes), style = typography.titleMedium)
                    Text(
                        text = stringResource(placement.descriptionRes),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Button(
            onClick = { onChoose(selected) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text(stringResource(R.string.learning_continue)) }
    }
}

/**
 * For a learner who started past the letters: a short check of the basics. What they pass
 * is ticked, so the path doesn't send them back to it.
 */
@Composable
internal fun PlacementCheckCard(unknownBasics: List<String>) {
    val context = LocalContext.current
    LearnCard(label = stringResource(R.string.learning_place_check_label)) {
        Text(stringResource(R.string.learning_place_check_text), style = typography.bodyMedium)
        OutlinedButton(
            onClick = { context.startActivity(ActivityPractice.intent(context, unknownBasics, PracticeMode.PLACEMENT)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text(stringResource(R.string.learning_place_check_start)) }
    }
}
