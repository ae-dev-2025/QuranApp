package com.quranapp.android.learning.path

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.words.WordItems
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Loads a real surah from the app's Quran database and, when it's installed, the learning pack. */
@RunWith(AndroidJUnit4::class)
class PathRepositoryTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun alIkhlas() = runBlocking {
        val needs = PathRepository.get(context).needs(112)
        assertTrue(needs.concepts.any { it.id == ConceptIds.SUKUN }) // قُلۡ

        val words = needs.words
        assumeTrue("the learning pack isn't installed on this device", words != null)
        assertEquals(15, words!!.size)
        assertTrue(words.all { it.isNotEmpty() })
        // لَمۡ is the surah's most frequent dictionary word: three times.
        assertEquals(WordItems.idOf("lam"), Readiness.unknownWords(needs, known = emptySet()).first())
    }
}
