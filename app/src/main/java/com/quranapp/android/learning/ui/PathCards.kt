package com.quranapp.android.learning.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.learning.path.Dot
import com.quranapp.android.learning.path.Layer

/** "Continue": the next unit on the path, or stage 0's next basic. */
@Composable
internal fun ContinueCard(summary: PathSummary) {
    val context = LocalContext.current
    val stage = summary.stage
    LearnCard(label = stringResource(R.string.learning_continue_label, stage.number, stringResource(stage.titleRes))) {
        when (val next = summary.next) {
            is NextStep.Basic -> {
                Text(stringResource(next.concept.titleRes), style = typography.titleMedium)
                Text(
                    text = stringResource(R.string.learning_basics_progress, next.known, next.total),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                ContinueButton { context.startActivity(ActivityConcept.intent(context, next.concept.id)) }
            }

            is NextStep.Unit -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(next.unit.name, style = typography.titleMedium)
                        next.layer?.let {
                            Text(
                                text = stringResource(R.string.learning_next_step, stringResource(it.labelRes)),
                                style = typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    LayerDots(next.unit.dots)
                }
                ContinueButton { context.startActivity(ActivityUnit.intent(context, next.unit.surahNo)) }
            }

            NextStep.Finished -> Text(stringResource(R.string.learning_path_finished), style = typography.bodyMedium)
        }
        Text(stringResource(stage.goalRes), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ContinueButton(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.learning_continue))
    }
}

/** "Your path": a few units around the next one, each with a dot per layer. */
@Composable
internal fun YourPathCard(summary: PathSummary) {
    val context = LocalContext.current
    val nextSurah = (summary.next as? NextStep.Unit)?.unit?.surahNo
    LearnCard(label = stringResource(R.string.learning_your_path)) {
        summary.units.forEach { unit ->
            val isNext = unit.surahNo == nextSurah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .then(if (isNext) Modifier.border(1.5.dp, colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                    .clickable { context.startActivity(ActivityUnit.intent(context, unit.surahNo)) }
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(unit.name, style = if (isNext) typography.titleSmall else typography.bodyLarge, modifier = Modifier.weight(1f))
                LayerDots(unit.dots)
            }
        }
        Text(stringResource(R.string.learning_path_dots_legend), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
        OutlinedButton(
            onClick = { context.startActivity(Intent(context, ActivityPath::class.java)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) { Text(stringResource(R.string.learning_see_whole_path)) }
    }
}

/**
 * One dot per layer, in the order read · recite · words: filled when done, half when
 * started, an outline when not started, and dashed when it needs the learning data.
 * TalkBack reads the states instead of the shapes.
 */
@Composable
internal fun LayerDots(dots: Map<Layer, Dot>) {
    val description = dots.entries.map { (layer, dot) ->
        stringResource(
            when (dot) {
                Dot.FULL -> R.string.learning_dot_full
                Dot.PARTIAL -> R.string.learning_dot_partial
                Dot.EMPTY -> R.string.learning_dot_empty
                Dot.UNKNOWN -> R.string.learning_dot_unknown
            },
            stringResource(layer.labelRes),
        )
    }.joinToString(", ")
    val filled = colorScheme.primary
    val outline = colorScheme.outline
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        dots.values.forEach { dot ->
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .then(
                        when (dot) {
                            Dot.FULL -> Modifier.background(filled)
                            Dot.PARTIAL -> Modifier
                                .border(1.5.dp, filled, CircleShape)
                                .drawBehind { drawRect(filled, size = Size(size.width / 2, size.height)) }
                            Dot.EMPTY -> Modifier.border(1.5.dp, outline, CircleShape)
                            Dot.UNKNOWN -> Modifier.drawBehind {
                                drawLine(outline.alpha(0.7f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 3f)
                            }
                        },
                    ),
            )
        }
    }
}
