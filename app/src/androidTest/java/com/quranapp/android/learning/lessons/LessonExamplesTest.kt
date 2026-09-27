package com.quranapp.android.learning.lessons

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.analysis.AyahAnalyzer
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Every reading and tajweed lesson's key example shows its concept, as the Understand sheet
 * finds it in the app's own text. (Grammar lessons are checked against the pack in
 * GrammarLessonExamplesTest.)
 */
@RunWith(AndroidJUnit4::class)
class LessonExamplesTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun keyExamplesShowTheirConcept() = runBlocking {
        val quran = DatabaseProvider.getQuranRepository(context)
        val missing = LessonCatalog.all.filter { !it.isGrammar }.mapNotNull { lesson ->
            val example = lesson.keyExample
            val words = quran.getWordsForAyahById(example.surahNo * 1000 + example.ayahNo, QuranScriptUtils.SCRIPT_UTHMANI).map { it.text }
            val foundIn = AyahAnalyzer.analyze(words).wordsByConcept[lesson.conceptId].orEmpty()
            if (foundIn.any { it in example.wordIndexes }) {
                null
            } else {
                "${lesson.conceptId}: found in words $foundIn of ${example.surahNo}:${example.ayahNo}, not ${example.wordIndexes}"
            }
        }
        assertTrue(missing.joinToString("\n"), missing.isEmpty())
    }
}
