package com.quranapp.android.learning.letters

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.examples.AyahWords
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Every letter is heard somewhere in Al-Fātiḥah or Juz ʿAmma, in the app's own text. */
@RunWith(AndroidJUnit4::class)
class LetterExamplesTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun everyLetterHasAnExampleInTheShortSurahs() = runBlocking {
        val quran = DatabaseProvider.getQuranRepository(context)
        val ayahs = (listOf(1) + (114 downTo 78)).flatMap { surah ->
            quran.getWordsForSurah(surah, QuranScriptUtils.SCRIPT_UTHMANI)
                .map { (ayahId, words) -> AyahWords(surah, ayahId % 1000, words.map { it.text }) }
        }
        Letters.all.forEach { letter ->
            val example = LetterFinder.exampleOf(letter, ayahs)
            assertNotNull(letter.name, example)
            // Every letter but alif starts some word there, so it's heard first.
            if (letter.id != "letter.alif") {
                assertTrue(letter.name + ": " + example!!.word, letter in LetterFinder.lettersOf(example.word.first()))
            }
        }
    }
}
