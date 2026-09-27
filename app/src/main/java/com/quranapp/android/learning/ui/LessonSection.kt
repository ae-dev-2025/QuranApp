package com.quranapp.android.learning.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.quranapp.android.R
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.concepts.ConceptCatalog
import com.quranapp.android.learning.lessons.KeyExample
import com.quranapp.android.learning.lessons.Lesson
import com.quranapp.android.repository.QuranRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The key example's Arabic words and surah name, loaded from the database. */
private data class LoadedExample(val arabic: String, val surahName: String)

/** The big example at the top of a lesson: the Arabic, how it sounds, and where it's from. */
@Composable
fun KeyExampleCard(example: KeyExample) {
    val context = LocalContext.current
    val repository = remember { DatabaseProvider.getQuranRepository(context) }

    val loaded by produceState<LoadedExample?>(initialValue = null, example) {
        value = withContext(Dispatchers.IO) {
            LoadedExample(
                arabic = repository.keyExampleArabic(example),
                surahName = repository.getSurahWithLocalizations(example.surahNo)
                    ?.getCurrentName()
                    .orEmpty(),
            )
        }
    }

    val arabicFont = remember { FontFamily(Font(R.font.uthmanic_hafs)) }
    val youSayLabel = stringResource(R.string.learning_you_say)
    val notLabel = stringResource(R.string.learning_not)
    val youSay = stringResource(example.youSay)
    val notSay = example.notSay?.let { stringResource(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // The minimum height reserves space while loading; centring keeps the content balanced.
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        val current = loaded ?: return@Column

        Text(
            text = current.arabic,
            fontFamily = arabicFont,
            style = typography.displaySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = buildAnnotatedString {
                append("$youSayLabel ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colorScheme.primary)) {
                    append(youSay)
                }
            },
            style = typography.bodyLarge,
        )
        Text(
            text = buildAnnotatedString {
                if (notSay != null) {
                    append("$notLabel ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(notSay) }
                    append(" · ")
                }
                append("${current.surahName} ${example.surahNo}:${example.ayahNo}")
            },
            style = typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

/** A key example's Arabic words, from the app's Uthmani text. Call it off the main thread. */
internal suspend fun QuranRepository.keyExampleArabic(example: KeyExample): String {
    val ayahId = example.surahNo * 1000 + example.ayahNo
    val words = getWordsForAyahById(ayahId, QuranScriptUtils.SCRIPT_UTHMANI).map { it.text }
    return example.wordIndexes.mapNotNull { words.getOrNull(it) }.joinToString(" ")
}

/** The written lesson: "How to spot it", "How to say it" and "Don't mix it up with". */
@Composable
fun LessonCard(lesson: Lesson, known: Set<String>, onOpenConcept: (Concept) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LessonHeading(R.string.learning_how_to_spot_it)
        Bullets(stringArrayResource(lesson.spotIt))

        LessonHeading(R.string.learning_how_to_say_it, Modifier.padding(top = 8.dp))
        Bullets(stringArrayResource(lesson.sayIt))

        val confused = lesson.confusedWith
        val other = confused?.let { ConceptCatalog[it.conceptId] }
        if (confused != null && other != null) {
            LessonHeading(R.string.learning_dont_mix_up, Modifier.padding(top = 8.dp))
            ConceptChip(other, isKnown = other.id in known, onClick = { onOpenConcept(other) })
            Text(arabicExamplesInOrder(stringResource(confused.note)), style = paragraphStyle())
        }
    }
}

@Composable
private fun LessonHeading(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(stringResource(text), style = typography.titleSmall, modifier = modifier)
}

@Composable
private fun Bullets(items: Array<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (item in items) {
            Row {
                Text("•", style = typography.bodyMedium, modifier = Modifier.padding(end = 8.dp))
                // weight(1f) gives the text the whole remaining width. Without it the width is
                // measured from the text itself, which comes out too small for lines that mix
                // Arabic and English, and they wrap too early.
                Text(arabicExamplesInOrder(item), style = paragraphStyle(), modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * Lesson text is written in the app's language but often starts with an Arabic example
 * ("قلى: you may continue…"). By default a paragraph takes its direction from its first
 * letter, which would lay such a line out right-to-left. Follow the app's layout instead.
 */
@Composable
private fun paragraphStyle(): TextStyle {
    val direction = when (LocalLayoutDirection.current) {
        LayoutDirection.Rtl -> TextDirection.Rtl
        LayoutDirection.Ltr -> TextDirection.Ltr
    }
    return typography.bodyMedium.copy(textDirection = direction)
}
