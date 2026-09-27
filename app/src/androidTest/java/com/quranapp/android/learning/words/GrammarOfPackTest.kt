package com.quranapp.android.learning.words

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.learning.concepts.GrammarIds
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** The grammar detector over the real learning pack, when it's installed on the device. */
@RunWith(AndroidJUnit4::class)
class GrammarOfPackTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun alFatihahAndAlBaqarah() = runBlocking {
        val words = WordRepository.open(context)
        assumeTrue("the learning pack isn't installed on this device", words != null)

        // 1:5 إِيَّاكَ نَعۡبُدُ وَإِيَّاكَ نَسۡتَعِينُ
        val ayah5 = words!!.grammarOfAyah(1005)
        assertEquals(listOf(0, 2), ayah5[GrammarIds.IYYA])
        assertEquals(listOf(1, 3), ayah5[GrammarIds.PRESENT])
        assertEquals(listOf(3), ayah5[GrammarIds.FORMS_7_10])

        // 1:7 أَنۡعَمۡتَ: the doer is inside the verb.
        assertEquals(listOf(2), words.grammarOfAyah(1007)[GrammarIds.DOER_IN_VERB])

        // 2:2 لَا رَيۡبَ
        assertEquals(listOf(2, 3), words.grammarOfAyah(2002)[GrammarIds.LA_GENERIC])

        // Al-Baqarah uses almost everything a word's form can show.
        val baqarah = words.grammarOfSurah(2)
        listOf(GrammarIds.PASSIVE, GrammarIds.SUBJUNCTIVE, GrammarIds.JUSSIVE, GrammarIds.INNA, GrammarIds.KANA, GrammarIds.VOCATIVE)
            .forEach { assertTrue(it, it in baqarah) }
    }
}
