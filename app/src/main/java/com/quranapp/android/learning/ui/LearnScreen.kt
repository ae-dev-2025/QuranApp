package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.pack.LearningPackManager
import com.quranapp.android.learning.path.LearningPreferences
import com.quranapp.android.learning.practice.DailyReview
import com.quranapp.android.learning.practice.ReviewSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The Learn tab (decision 1): today's review, where you left off and your path. */
@Composable
fun LearnScreen(viewModel: LearnViewModel) {
    val context = LocalContext.current
    val reviews = remember { DatabaseProvider.getUserDatabase(context).reviewDao() }
    val path by viewModel.summary.collectAsStateWithLifecycle()
    val start by viewModel.startStage.collectAsStateWithLifecycle()
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    var pickingGoal by rememberSaveable { mutableStateOf(false) }
    if (pickingGoal) {
        GoalPickerSheet(
            onPick = { surah ->
                viewModel.setGoal(surah)
                pickingGoal = false
            },
            onDismiss = { pickingGoal = false },
        )
    }
    LaunchedEffect(Unit) { LearningPackManager.refresh(context) }
    // Recomputed when a card changes and every minute, so items become due while the screen is open.
    val reviewSummary by remember {
        combine(reviews.observeCards(), everyMinute()) { cards, now -> DailyReview.summarize(cards, now) }
    }.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppBar(title = stringResource(R.string.learning_nav_learn)) },
    ) { padding ->
        when (start) {
            null -> return@Scaffold // a moment while the preference loads
            LearningPreferences.NOT_CHOSEN -> {
                PlacementContent(Modifier.fillMaxSize().padding(padding), onChoose = viewModel::choose)
                return@Scaffold
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            reviewSummary?.let { loaded ->
                item {
                    TodayCard(loaded) {
                        scope.launch {
                            val itemIds = withContext(Dispatchers.IO) {
                                DailyReview.sessionItems(reviews.due(System.currentTimeMillis(), DailyReview.SESSION_SIZE))
                            }
                            if (itemIds.isNotEmpty()) {
                                context.startActivity(ActivityPractice.intent(context, itemIds, PracticeMode.REVIEW))
                            }
                        }
                    }
                }
            }
            path?.let { loaded ->
                item { ContinueCard(loaded) }
                // Started past the letters: offer to tick the basics they already know.
                val checkable = loaded.unknownBasics.filter { it != ConceptIds.LETTERS } // letters get questions in M7
                if (loaded.start > 0 && checkable.isNotEmpty()) item { PlacementCheckCard(checkable) }
                item { YourPathCard(loaded) }
            }
            goal?.let { loaded -> item { GoalCard(loaded, onChooseSurah = { pickingGoal = true }) } }
            item {
                Text(
                    text = stringResource(R.string.learning_understand_tip),
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

private fun everyMinute(): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(60_000)
    }
}

@Composable
private fun TodayCard(summary: ReviewSummary, onStart: () -> Unit) {
    LearnCard(label = stringResource(R.string.learning_today)) {
        when {
            summary.dueNow > 0 -> {
                Text(
                    text = pluralStringResource(R.plurals.learning_reviews_due, summary.dueNow, summary.dueNow) +
                        " · " + stringResource(R.string.learning_about_minutes, summary.minutes),
                    style = typography.titleMedium,
                )
                Text(
                    text = if (summary.dueNow > summary.sessionSize) {
                        stringResource(R.string.learning_review_more_later, summary.sessionSize)
                    } else {
                        stringResource(R.string.learning_review_what)
                    },
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.learning_review_start)) }
            }

            summary.nextDueAt != null -> {
                Text(stringResource(R.string.learning_review_caught_up), style = typography.titleMedium)
                val days = DailyReview.daysUntil(summary.nextDueAt, System.currentTimeMillis()).toInt()
                Text(
                    text = when (days) {
                        0 -> stringResource(R.string.learning_review_next_today)
                        1 -> stringResource(R.string.learning_review_next_tomorrow)
                        else -> pluralStringResource(R.plurals.learning_review_next_days, days, days)
                    },
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                Text(stringResource(R.string.learning_review_none), style = typography.titleMedium)
                Text(
                    text = stringResource(R.string.learning_review_none_text),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun LearnCard(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = typography.labelLarge,
            color = colorScheme.primary,
        )
        content()
    }
}
