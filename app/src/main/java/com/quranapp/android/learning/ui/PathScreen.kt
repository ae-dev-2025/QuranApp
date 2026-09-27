package com.quranapp.android.learning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.path.Curriculum
import com.quranapp.android.learning.path.Layer

/** Every stage of the path, its goal, how far along it is, and its units. */
@Composable
fun PathScreen(viewModel: PathViewModel) {
    val stages by viewModel.stages.collectAsStateWithLifecycle()
    Scaffold(topBar = { AppBar(title = stringResource(R.string.learning_your_path)) }) { padding ->
        val loaded = stages
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
                Text(
                    text = stringResource(R.string.learning_path_intro),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            items(loaded, key = { it.stage.number }) { StageCard(it) }
        }
    }
}

@Composable
private fun StageCard(row: StageRow) {
    val stage = row.stage
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .then(if (row.status == StageStatus.CURRENT) Modifier.border(1.5.dp, colorScheme.primary, shape) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.learning_stage_title, stage.number, stringResource(stage.titleRes)),
                style = typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            when (row.status) {
                StageStatus.DONE -> StatusLabel(R.string.learning_stage_done)
                StageStatus.CURRENT -> StatusLabel(R.string.learning_stage_current)
                StageStatus.LATER -> Unit
            }
        }
        Text(stringResource(stage.goalRes), style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        row.goals?.let { GoalsLine(it) }

        when {
            stage.number == 0 -> BasicsChips()
            stage.units.isEmpty() -> stageNote(stage.number)?.let {
                Text(stringResource(it), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }
            row.units == null -> CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.CenterHorizontally))
            else -> row.units.forEach { UnitRow(it) }
        }
    }
}

@Composable
private fun StatusLabel(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = typography.labelMedium,
        color = colorScheme.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorScheme.primary.alpha(0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** "Read 6/30 · Recite 3/28 · Words 6%": each goal of the stage, counted like a unit's bars. */
@Composable
private fun GoalsLine(goals: List<GoalProgress>) {
    val parts = goals.map { goal ->
        val label = goal.layer?.let { stringResource(it.labelRes) } ?: stringResource(R.string.learning_basics)
        val value = when {
            goal.progress == null -> "—"
            goal.layer == Layer.WORDS -> stringResource(R.string.learning_readiness_percent, goal.progress.percent)
            else -> stringResource(R.string.learning_layer_count, goal.progress.known, goal.progress.total)
        }
        "$label $value"
    }
    Text(parts.joinToString(" · "), style = typography.labelLarge)
}

@Composable
private fun UnitRow(unit: UnitDots) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { context.startActivity(ActivityUnit.intent(context, unit.surahNo)) }
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(unit.name, style = typography.bodyLarge, modifier = Modifier.weight(1f))
        LayerDots(unit.dots)
    }
}

/** Stage 0's basics, until the letters arrive (milestone 7). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicsChips() {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known by progress.knownConceptIds.collectAsStateWithLifecycle(initialValue = emptySet())
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Curriculum.BASICS.mapNotNull(ConceptCatalog::get).forEach { concept ->
            ConceptChip(concept, isKnown = concept.id in known) {
                context.startActivity(ActivityConcept.intent(context, concept.id))
            }
        }
    }
}

@StringRes
private fun stageNote(number: Int): Int? = when (number) {
    3 -> R.string.learning_stage_3_note
    4 -> R.string.learning_stage_4_note
    6 -> R.string.learning_stage_6_note
    else -> null
}
