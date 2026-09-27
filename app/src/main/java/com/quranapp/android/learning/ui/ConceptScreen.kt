package com.quranapp.android.learning.ui

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.ConceptGraph
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.concepts.Track
import com.quranapp.android.learning.lessons.LessonCatalog
import kotlinx.coroutines.launch

/**
 * The learning page of one concept, in the approved "guided" order: what to learn first,
 * a key example, the explanation and lesson, examples from the Quran, then "I know this",
 * and what the concept unlocks. Concepts without a written lesson skip those two cards.
 */
@Composable
fun ConceptScreen(concept: Concept) {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known by progress.knownConceptIds.collectAsStateWithLifecycle(initialValue = emptySet())
    val scope = rememberCoroutineScope()

    val graph = remember { ConceptGraph() }
    val prerequisites = remember(concept) { graph.prerequisitesOf(concept.id) }
    val unlocks = remember(concept) { graph.dependentsOf(concept.id) }
    val lesson = remember(concept) { LessonCatalog[concept.id] }
    val isUmbrella = concept.id in ConceptCatalog.umbrellaIds
    val isKnown = concept.id in known

    // Each linked concept opens as its own page; the back button returns here.
    val openConcept = { linked: Concept ->
        context.startActivity(ActivityConcept.intent(context, linked.id))
    }

    Scaffold(
        topBar = { AppBar(title = conceptTitle(concept)) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                // An umbrella concept counts its rules; any other shows known / not known yet.
                val status = if (isUmbrella) {
                    val knownRules = unlocks.count { it.id in known }
                    pluralStringResource(R.plurals.learning_rules_known, unlocks.size, knownRules, unlocks.size)
                } else {
                    stringResource(if (isKnown) R.string.learning_known else R.string.learning_not_known_yet)
                }
                val done = if (isUmbrella) unlocks.all { it.id in known } else isKnown
                TrackAndStatus(concept.track, status, done)
            }

            if (prerequisites.isNotEmpty()) {
                item {
                    ConceptChipGroup(R.string.learning_learn_these_first, prerequisites, known, openConcept)
                }
            }

            if (lesson != null) {
                item { KeyExampleCard(lesson.keyExample) }
            }

            concept.arabicTerm?.let { term ->
                item { Text(term, style = typography.headlineSmall, color = colorScheme.primary) }
            }

            item {
                Text(
                    text = arabicExamplesInOrder(stringResource(concept.summaryRes)),
                    style = typography.bodyLarge,
                )
            }

            if (lesson != null) {
                item { LessonCard(lesson, known, openConcept) }
            }

            if (isUmbrella) {
                item { RuleOverview(unlocks, openConcept) }
            }

            // The letters are items of their own now: the concept points to them.
            if (concept.id == ConceptIds.LETTERS) {
                item {
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(context, ActivityLetters::class.java)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.learning_letters_see_all)) }
                }
            }

            item { ConceptExamplesSection(concept.id) }

            item {
                KnownButton(
                    isKnown = isKnown,
                    onKnownChange = { checked ->
                        scope.launch { progress.setKnown(concept.id, checked) }
                    },
                )
            }

            // Grammar questions come with the sentence roles (milestone 9).
            if (concept.track != Track.GRAMMAR) item { CheckYourselfButton(concept.id) }

            // An umbrella concept already lists what it unlocks: its rules, above.
            if (unlocks.isNotEmpty() && !isUmbrella) {
                item { ConceptChipGroup(R.string.learning_unlocks, unlocks, known, openConcept) }
            }

            item {
                Text(
                    text = stringResource(R.string.learning_analysis_disclaimer),
                    style = typography.labelSmall,
                    color = colorScheme.onSurface.alpha(0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TrackAndStatus(track: Track, status: String, done: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            // Locale-aware upper case ("TAJWEED"); Arabic and other scripts are unaffected.
            text = stringResource(track.labelRes).toUpperCase(Locale.current),
            style = typography.labelLarge,
            color = colorScheme.primary,
        )

        val (background, foreground) = if (done) {
            colorScheme.primary.alpha(0.12f) to colorScheme.primary
        } else {
            colorScheme.surfaceVariant to colorScheme.onSurfaceVariant
        }
        Text(
            text = status,
            style = typography.labelMedium,
            color = foreground,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(background)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** A titled group of concept chips, e.g. "Learn these first". Chips wrap onto new lines. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConceptChipGroup(
    @StringRes title: Int,
    concepts: List<Concept>,
    known: Set<String>,
    onOpen: (Concept) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(title),
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (concept in concepts) {
                ConceptChip(concept, isKnown = concept.id in known, onClick = { onOpen(concept) })
            }
        }
    }
}

/** Known concepts are filled green with a tick; the others are outlined with a ">". */
@Composable
internal fun ConceptChip(concept: Concept, isKnown: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val look = if (isKnown) {
        Modifier.background(colorScheme.primary.alpha(0.12f))
    } else {
        Modifier.border(1.dp, colorScheme.outlineVariant, shape)
    }

    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .then(look)
            .clickable(onClickLabel = stringResource(R.string.learning_open_lesson), onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isKnown) {
            Icon(
                painter = painterResource(R.drawable.dr_icon_check),
                contentDescription = stringResource(R.string.learning_known),
                tint = colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = conceptTitle(concept),
            style = typography.labelLarge,
            color = if (isKnown) colorScheme.primary else colorScheme.onSurface,
        )
        if (!isKnown) OpenChevron()
    }
}

/** The page ends with this: tick it once you've understood the concept. */
@Composable
private fun KnownButton(isKnown: Boolean, onKnownChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.primary.alpha(0.12f))
            .toggleable(value = isKnown, role = Role.Checkbox, onValueChange = onKnownChange),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isKnown, onCheckedChange = null)
        Text(
            text = stringResource(R.string.learning_i_know_this),
            style = typography.titleSmall,
            color = colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
