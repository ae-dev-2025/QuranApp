package com.quranapp.android.learning.lessons

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Every grammar lesson's key example shows its concept, as the Grammar tab finds it from the
 * learning pack. Needs the pack on the device.
 */
@RunWith(AndroidJUnit4::class)
class GrammarLessonExamplesTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun keyExamplesShowTheirConcept() = runBlocking {
        val words = WordRepository.open(context)
        assumeTrue("the learning pack isn't installed on this device", words != null)
        val quran = DatabaseProvider.getQuranRepository(context)

        for (lesson in GrammarLessons.all) {
            val example = lesson.keyExample
            val ayahId = example.surahNo * 1000 + example.ayahNo
            // The ayah's words, without the ayah number at the end.
            val wordCount = quran.getWordsForAyahById(ayahId, QuranScriptUtils.SCRIPT_UTHMANI).size - 1
            assertTrue("${lesson.conceptId}: words ${example.wordIndexes} of $wordCount", example.wordIndexes.last < wordCount)

            val foundIn = words!!.grammarOfAyah(ayahId)[lesson.conceptId].orEmpty()
            assertTrue(
                "${lesson.conceptId} is found in words $foundIn of ${example.surahNo}:${example.ayahNo}, not ${example.wordIndexes}",
                foundIn.any { it in example.wordIndexes },
            )
        }
    }
}
