package com.quranapp.android.learning.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quranapp.android.R
import com.quranapp.android.activities.base.BaseActivity
import com.quranapp.android.compose.components.common.AppBar
import com.quranapp.android.compose.theme.alpha
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.learning.letters.Letter
import com.quranapp.android.learning.letters.LetterExample
import com.quranapp.android.learning.letters.LetterFinder
import com.quranapp.android.learning.letters.LetterQualities
import com.quranapp.android.learning.letters.Letters
import com.quranapp.android.learning.progress.LearningProgressRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The 29 letters in their seven groups (stage 0). */
class ActivityLetters : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        setContent { LearningTheme { LettersScreen() } }
    }
}

/** One letter's page. Open it with [intent]. */
class ActivityLetter : BaseActivity() {
    override fun getLayoutResource() = 0

    override fun onActivityInflated(activityView: View, savedInstanceState: Bundle?) {
        val letter = intent.getStringExtra(EXTRA_LETTER_ID)?.let(Letters::get)
        if (letter == null) {
            finish()
            return
        }
        setContent { LearningTheme { LetterScreen(letter) } }
    }

    companion object {
        private const val EXTRA_LETTER_ID = "letter_id"

        fun intent(context: Context, letterId: String): Intent =
            Intent(context, ActivityLetter::class.java).putExtra(EXTRA_LETTER_ID, letterId)
    }
}

@Composable
private fun knownIds(): Set<String>? {
    val context = LocalContext.current
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known by progress.knownConceptIds.collectAsStateWithLifecycle(initialValue = null)
    return known
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LettersScreen() {
    val context = LocalContext.current
    val arabicFont = rememberLetterFont()
    val known = knownIds().orEmpty()
    val knownCount = Letters.all.count { it.id in known }
    val next = Letters.all.firstOrNull { it.id !in known }

    Scaffold(topBar = { AppBar(title = stringResource(R.string.learning_letters_title)) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LearnCard(label = stringResource(R.string.learning_stage_title, 0, stringResource(R.string.learning_stage_0))) {
                    Text(stringResource(R.string.learning_letters_known, knownCount, Letters.all.size), style = typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { knownCount / Letters.all.size.toFloat() },
                        trackColor = colorScheme.primary.alpha(0.15f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            next?.let { letter ->
                item {
                    LearnCard(label = stringResource(R.string.learning_letters_next)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(letter.name, style = typography.titleMedium)
                                Text(stringResource(letter.soundRes), style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            }
                            Text(letter.char.toString(), fontFamily = arabicFont, style = typography.displaySmall)
                        }
                        Button(
                            onClick = { context.startActivity(ActivityLetter.intent(context, letter.id)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text(stringResource(R.string.learning_letters_learn)) }
                    }
                }
            }
            Letters.groups.forEach { (group, letters) ->
                item(key = group) {
                    LearnCard(label = stringResource(R.string.learning_letters_group, group)) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                letters.forEach { LetterCell(it, isKnown = it.id in known, arabicFont) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LetterCell(letter: Letter, isKnown: Boolean, arabicFont: FontFamily) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(12.dp)
    val description = letter.name + ", " + stringResource(if (isKnown) R.string.learning_known else R.string.learning_not_known)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .then(if (isKnown) Modifier.background(colorScheme.primary.alpha(0.2f)) else Modifier.border(1.dp, colorScheme.outlineVariant, shape))
            .clickable { context.startActivity(ActivityLetter.intent(context, letter.id)) }
            .semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(letter.char.toString(), fontFamily = arabicFont, style = typography.headlineMedium, color = if (isKnown) colorScheme.primary else colorScheme.onSurface)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterScreen(letter: Letter) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val arabicFont = rememberLetterFont()
    val quranFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    val progress = remember { DatabaseProvider.getLearningProgressRepository(context) }
    val known = knownIds()
    val example by produceState<LetterExample?>(initialValue = null, letter) {
        value = withContext(Dispatchers.IO) { LetterFinder.exampleOf(letter, shortSurahs(context)) }
    }

    Scaffold(topBar = { AppBar(title = letter.name) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surface)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(letter.char.toString(), fontFamily = arabicFont, style = typography.displayLarge)
                    Text(letter.arabicName + " · " + letter.name, fontFamily = arabicFont, style = typography.titleMedium)
                    Text(stringResource(letter.soundRes), style = typography.bodyLarge, textAlign = TextAlign.Center)
                    val syllables = Letters.syllablesOf(letter)
                    if (syllables.isEmpty()) {
                        Text(stringResource(R.string.learning_letter_alif_note), style = typography.bodyMedium, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            syllables.forEach { (arabic, latin) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(arabic, fontFamily = arabicFont, style = typography.headlineMedium)
                                    Text(latin, style = typography.labelLarge, color = colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            item {
                LearnCard(label = stringResource(R.string.learning_letter_shapes)) {
                    val shapes = Letters.shapesOf(letter)
                    // Right to left, like the word: alone, start, middle, end.
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(
                                shapes.alone to R.string.learning_letter_alone,
                                shapes.start to R.string.learning_letter_start,
                                shapes.middle to R.string.learning_letter_middle,
                                shapes.end to R.string.learning_letter_end,
                            ).forEach { (shape, label) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(shape, fontFamily = arabicFont, style = typography.headlineLarge)
                                    Text(stringResource(label), style = typography.labelMedium, color = colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (!letter.joinsNext && letter.id != "letter.hamza") {
                        Text(stringResource(R.string.learning_letter_no_join), style = typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                LearnCard(label = stringResource(R.string.learning_letter_place)) {
                    Text(stringResource(letter.placeRes), style = typography.bodyLarge)
                    ConceptCatalog[letter.place.conceptId]?.let { concept ->
                        ConceptChip(concept, isKnown = known?.contains(concept.id) == true) {
                            context.startActivity(ActivityConcept.intent(context, concept.id))
                        }
                    }
                }
            }
            item {
                LearnCard(label = stringResource(R.string.learning_letter_qualities)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LetterQualities.of(letter).forEach { quality ->
                                QualityChip(stringResource(quality.labelRes), isKnown = known?.contains(quality.conceptId) == true) {
                                    context.startActivity(ActivityConcept.intent(context, quality.conceptId))
                                }
                            }
                        }
                    }
                }
            }
            example?.let { found ->
                item {
                    LearnCard(label = stringResource(R.string.learning_letter_in_quran)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HearWordButton(found.surahNo, found.ayahNo, found.wordIndex)
                            Text("${found.surahNo}:${found.ayahNo}", style = typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(found.word, fontFamily = quranFont, style = typography.headlineMedium)
                        }
                    }
                }
            }
            if (known != null) {
                item {
                    val isKnown = letter.id in known
                    Button(
                        onClick = { scope.launch { setLetterKnown(progress, letter.id, !isKnown, known) } },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(stringResource(if (isKnown) R.string.learning_letter_known else R.string.learning_letter_i_know)) }
                }
                item { CheckYourselfButton(letter.id) }
            }
        }
    }
}

/** A quality as a chip: its label, opening the lesson of its pair (voiced opens whispered and voiced). */
@Composable
private fun QualityChip(label: String, isKnown: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text = label,
        style = typography.labelLarge,
        color = if (isKnown) colorScheme.primary else colorScheme.onSurface,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(shape)
            .then(if (isKnown) Modifier.background(colorScheme.primary.alpha(0.12f)) else Modifier.border(1.dp, colorScheme.outlineVariant, shape))
            .clickable(onClickLabel = stringResource(R.string.learning_open_lesson), onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}

/** Marks a letter known or not. Knowing all 29 completes the "Arabic letters" concept others build on. */
private suspend fun setLetterKnown(progress: LearningProgressRepository, letterId: String, known: Boolean, knownBefore: Set<String>) {
    progress.setKnown(letterId, known)
    val allKnown = Letters.all.all { it.id == letterId && known || it.id != letterId && it.id in knownBefore }
    if (allKnown) progress.setKnown(ConceptIds.LETTERS, true)
}

/**
 * Letters on their own are drawn in Scheherazade New, which the app already ships (SIL Open
 * Font License). The Quran font draws a lone yāʾ without its dots, as the muṣḥaf does, and a
 * beginner needs to see them. Words from the Quran keep the Quran font.
 */
@Composable
private fun rememberLetterFont() = remember { FontFamily(Font(R.font.scheherazadenew_regular)) }

/** Al-Fātiḥah, then Juz ʿAmma from its end: where beginners meet the letters first. */
private suspend fun shortSurahs(context: Context): List<AyahWords> {
    val quran = DatabaseProvider.getQuranRepository(context)
    return (listOf(1) + (114 downTo 78)).flatMap { surah ->
        quran.getWordsForSurah(surah, QuranScriptUtils.SCRIPT_UTHMANI)
            .map { (ayahId, words) -> AyahWords(surah, ayahId % 1000, words.map { it.text }) }
    }
}
