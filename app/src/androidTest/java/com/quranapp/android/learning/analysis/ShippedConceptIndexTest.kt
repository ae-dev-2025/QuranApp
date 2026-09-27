package com.quranapp.android.learning.analysis

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * The shipped [ConceptIndex] is what the detectors find in the app's Quran text today.
 *
 * When a detector changes, this fails and writes the index made now to the app's external
 * files folder. Copy it into the assets and commit it:
 *
 *     adb pull /sdcard/Android/data/com.quranapp.android.debug/files/concept_index.txt app/src/main/assets/learning/
 */
@RunWith(AndroidJUnit4::class)
class ShippedConceptIndexTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun theShippedIndexIsWhatTheDetectorsFind() = runBlocking {
        val quran = DatabaseProvider.getQuranRepository(context)
        val ayahs = (1..114).flatMap { surahNo ->
            quran.getWordsForSurah(surahNo, QuranScriptUtils.SCRIPT_UTHMANI).map { (ayahId, words) -> ayahId to words.map { it.text } }
        }.toMap()
        val now = StringBuilder().also { ConceptIndex.of(ayahs).write(it) }.toString()

        val shipped = runCatching { context.assets.open(ConceptIndex.ASSET).bufferedReader().readText() }.getOrNull()
        if (shipped != now) {
            val made = File(context.getExternalFilesDir(null), "concept_index.txt").apply { writeText(now) }
            fail("${ConceptIndex.ASSET} is ${if (shipped == null) "missing" else "out of date"}. The index made now is in ${made.path}.")
        }
    }
}
