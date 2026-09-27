package com.quranapp.android.learning.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.practice.ChoiceKind
import com.quranapp.android.learning.practice.ChoiceQuestion
import com.quranapp.android.learning.practice.RuleQuestion
import com.quranapp.android.learning.practice.TapWordQuestion
import com.quranapp.android.learning.practice.WordIntroduction
import com.quranapp.android.learning.progress.ReviewRating

/** The practice screen: one question at a time, graded straight away (decision 8). */
@Composable
fun PracticeScreen(viewModel: PracticeViewModel, onClose: () -> Unit) {
    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    val state = viewModel.state

    Scaffold(
        topBar = {
            val title = when {
                state is PracticeUiState.Asking && state.question is WordIntroduction -> stringResource(R.string.learning_new_word)
                state is PracticeUiState.Asking -> stringResource(R.string.learning_practice_progress, state.number, state.total)
                else -> stringResource(R.string.learning_practice_title)
            }
            AppBar(title = title)
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                PracticeUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                PracticeUiState.Empty -> Text(
                    text = stringResource(R.string.learning_practice_empty),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                )
                is PracticeUiState.Asking -> Asking(state, arabicFont, viewModel)
                is PracticeUiState.Finished -> Finished(state, onClose)
            }
        }
    }
}

@Composable
private fun Asking(state: PracticeUiState.Asking, arabicFont: FontFamily, viewModel: PracticeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            // Counts the current question as done once it's answered.
            progress = { (state.number - if (state.answered) 0 else 1) / state.total.toFloat() },
            trackColor = colorScheme.primary.alpha(0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (val question = state.question) {
                is ChoiceQuestion -> ChoiceView(question, state.answer, arabicFont) {
                    viewModel.answer(it, question.isRight(it))
                }
                is TapWordQuestion -> TapWordView(question, state.answer, arabicFont) {
                    viewModel.answer(it, question.isRight(it))
                }
                is RuleQuestion -> RuleView(question, state.answer, arabicFont) {
                    viewModel.answer(it, question.isRight(it))
                }
                is WordIntroduction -> IntroductionView(question, arabicFont)
            }
        }
        if (state.question is WordIntroduction) {
            Button(
                onClick = viewModel::acknowledge,
                modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.learning_got_it)) }
        } else if (state.answered) {
            Feedback(state, onContinue = viewModel::next)
        }
    }
}

@Composable
private fun ChoiceView(question: ChoiceQuestion, answer: Int?, arabicFont: FontFamily, onAnswer: (Int) -> Unit) {
    when (question.kind) {
        ChoiceKind.MEANING_OF_WORD -> {
            Prompt(stringResource(R.string.learning_q_meaning))
            Text(
                text = question.prompt,
                fontFamily = arabicFont,
                style = typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ChoiceKind.WORD_FOR_MEANING -> Prompt(stringResource(R.string.learning_q_word_for, question.prompt))
    }
    question.options.forEachIndexed { index, option ->
        OptionRow(
            text = option,
            arabicFont = if (question.optionsAreArabic) arabicFont else null,
            look = optionLook(index, answer, question.answerIndex),
            onClick = { onAnswer(index) },
        )
    }
}

@Composable
private fun RuleView(question: RuleQuestion, answer: Int?, arabicFont: FontFamily, onAnswer: (Int) -> Unit) {
    Prompt(stringResource(R.string.learning_q_rule))
    AyahWordsCard(question.words, question.surahNo, question.ayahNo, arabicFont) { index ->
        if (index == question.highlighted) WordLook.Highlighted else WordLook.Plain
    }
    question.options.forEachIndexed { index, conceptId ->
        OptionRow(
            text = ConceptCatalog[conceptId]?.let { stringResource(it.titleRes) } ?: conceptId,
            arabicFont = null,
            look = optionLook(index, answer, question.answerIndex),
            onClick = { onAnswer(index) },
        )
    }
}

@Composable
private fun TapWordView(question: TapWordQuestion, answer: Int?, arabicFont: FontFamily, onAnswer: (Int) -> Unit) {
    val title = ConceptCatalog[question.itemId]?.let { stringResource(it.titleRes) } ?: question.itemId
    Prompt(stringResource(R.string.learning_q_tap, title))
    AyahWordsCard(
        words = question.words,
        surahNo = question.surahNo,
        ayahNo = question.ayahNo,
        arabicFont = arabicFont,
        onTap = if (answer == null) onAnswer else null,
    ) { index ->
        when {
            answer == null -> WordLook.Plain
            index in question.answers -> WordLook.Right
            index == answer -> WordLook.Wrong
            else -> WordLook.Plain
        }
    }
}

/** A new word before its questions: dictionary form, meaning and how often it occurs. */
@Composable
private fun IntroductionView(introduction: WordIntroduction, arabicFont: FontFamily) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(introduction.headword, fontFamily = arabicFont, style = typography.displayMedium)
        introduction.firstPlace?.let { place ->
            HearWordButton(place.ayahId / 1000, place.ayahId % 1000, place.wordIndex)
        }
        Text(introduction.meaning, style = typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            text = pluralStringResource(R.plurals.learning_words_times, introduction.occurrences, introduction.occurrences),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
    Text(
        text = stringResource(R.string.learning_new_word_hint),
        style = typography.bodyMedium,
        color = colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Prompt(text: String) {
    Text(text, style = typography.titleMedium)
}

private enum class WordLook { Plain, Highlighted, Right, Wrong }

/** An ayah's words, right to left, each optionally tappable. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AyahWordsCard(
    words: List<String>,
    surahNo: Int,
    ayahNo: Int,
    arabicFont: FontFamily,
    onTap: ((Int) -> Unit)? = null,
    look: (Int) -> WordLook,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                words.forEachIndexed { index, word ->
                    val wordLook = look(index)
                    // Right and highlighted words also get an outline, so colour isn't the only sign.
                    val (background, foreground, outline) = when (wordLook) {
                        WordLook.Plain -> Triple(Color.Transparent, colorScheme.onSurface, Color.Transparent)
                        WordLook.Highlighted -> Triple(colorScheme.onSurface.alpha(0.08f), colorScheme.onSurface, colorScheme.onSurface)
                        WordLook.Right -> Triple(colorScheme.primary.alpha(0.2f), colorScheme.primary, colorScheme.primary)
                        WordLook.Wrong -> Triple(colorScheme.error.alpha(0.15f), colorScheme.error, Color.Transparent)
                    }
                    val state = when (wordLook) {
                        WordLook.Right -> stringResource(R.string.learning_a11y_right_answer)
                        WordLook.Wrong -> stringResource(R.string.learning_a11y_your_answer)
                        else -> null
                    }
                    val shape = RoundedCornerShape(8.dp)
                    Text(
                        text = word,
                        fontFamily = arabicFont,
                        style = typography.headlineMedium,
                        color = foreground,
                        modifier = Modifier
                            .clip(shape)
                            .background(background)
                            .border(2.dp, outline, shape)
                            .then(if (onTap != null) Modifier.clickable { onTap(index) } else Modifier)
                            .semantics { if (state != null) stateDescription = state }
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Text(
            text = "$surahNo:$ayahNo",
            style = typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private enum class OptionLook { Plain, Right, Wrong, Faded }

private fun optionLook(index: Int, answer: Int?, rightIndex: Int): OptionLook = when {
    answer == null -> OptionLook.Plain
    index == rightIndex -> OptionLook.Right
    index == answer -> OptionLook.Wrong
    else -> OptionLook.Faded
}

@Composable
private fun OptionRow(text: String, arabicFont: FontFamily?, look: OptionLook, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val (border, background) = when (look) {
        OptionLook.Plain, OptionLook.Faded -> colorScheme.outlineVariant to colorScheme.surface
        OptionLook.Right -> colorScheme.primary to colorScheme.primary.alpha(0.12f)
        OptionLook.Wrong -> colorScheme.error to colorScheme.error.alpha(0.1f)
    }
    val (mark, state) = when (look) {
        OptionLook.Right -> "✓" to stringResource(R.string.learning_a11y_right_answer)
        OptionLook.Wrong -> "✗" to stringResource(R.string.learning_a11y_your_answer)
        else -> null to null
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(background)
            .border(if (look == OptionLook.Right) 2.dp else 1.dp, border, shape)
            .then(if (look == OptionLook.Plain) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) { if (state != null) stateDescription = state }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = if (arabicFont != null) Alignment.Center else Alignment.CenterStart,
    ) {
        Text(
            text = text,
            fontFamily = arabicFont,
            style = if (arabicFont != null) typography.headlineSmall else typography.bodyLarge,
            color = if (look == OptionLook.Faded) colorScheme.onSurface.alpha(0.5f) else colorScheme.onSurface,
            modifier = Modifier.padding(end = 24.dp),
        )
        if (mark != null) {
            Text(
                text = mark,
                style = typography.titleLarge,
                color = border,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clearAndSetSemantics {},
            )
        }
    }
}

@Composable
private fun Feedback(state: PracticeUiState.Asking, onContinue: () -> Unit) {
    val right = when (val question = state.question) {
        is ChoiceQuestion -> question.isRight(state.answer!!)
        is TapWordQuestion -> question.isRight(state.answer!!)
        is RuleQuestion -> question.isRight(state.answer!!)
        is WordIntroduction -> true // never answered: it has "Got it" instead
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (right) colorScheme.primary.alpha(0.12f) else colorScheme.error.alpha(0.1f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(if (right) R.string.learning_right else R.string.learning_not_quite),
            style = typography.titleMedium,
            color = if (right) colorScheme.primary else colorScheme.error,
        )
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.learning_continue))
        }
    }
}

@Composable
private fun Finished(state: PracticeUiState.Finished, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.learning_practice_done), style = typography.headlineSmall)
        Text(
            text = stringResource(R.string.learning_practice_score, state.right, state.total),
            style = typography.titleMedium,
            color = colorScheme.primary,
        )
        val passed = state.results.count { it.rating != ReviewRating.AGAIN }
        val failed = state.results.size - passed
        val placement = state.mode == PracticeMode.PLACEMENT
        if (passed > 0) {
            val text = if (placement) R.plurals.learning_placement_passed else R.plurals.learning_practice_passed
            Text(pluralStringResource(text, passed, passed), textAlign = TextAlign.Center)
        }
        if (failed > 0) {
            val text = if (placement) R.plurals.learning_placement_failed else R.plurals.learning_practice_failed
            Text(pluralStringResource(text, failed, failed), textAlign = TextAlign.Center)
        }
        Button(onClick = onClose) { Text(stringResource(R.string.learning_continue)) }
    }
}

/** Opens a check of [itemId]: three questions, graded by the app (decision 7). */
@Composable
internal fun CheckYourselfButton(itemId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = { context.startActivity(ActivityPractice.intent(context, listOf(itemId), PracticeMode.CHECK)) },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        Text(stringResource(R.string.learning_check_yourself))
    }
}
