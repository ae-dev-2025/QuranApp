package com.quranapp.android.learning.path

import android.content.Context
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.repository.QuranRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads what each surah needs, once per app run: the Quran text never changes, and the
 * words only change when the learning pack is downloaded or deleted.
 */
class PathRepository(
    private val quran: QuranRepository,
    private val openWords: suspend () -> WordRepository?,
) {
    private val mutex = Mutex()
    private val concepts = HashMap<Int, List<Concept>>()
    private val words = HashMap<Int, List<List<String>>>()

    suspend fun needs(surahNo: Int): SurahNeeds = mutex.withLock {
        val surahConcepts = concepts.getOrPut(surahNo) {
            // Always the Unicode text: the other scripts store font glyph codes, not letters.
            val ayahs = quran.getWordsForSurah(surahNo, QuranScriptUtils.SCRIPT_UTHMANI).values.map { ayah -> ayah.map { it.text } }
            SurahNeeds.conceptsOf(ayahs)
        }
        val repository = openWords()
        if (repository == null) words.clear() // the pack was deleted: forget its words
        val surahWords = repository?.let { words.getOrPut(surahNo) { it.wordItemsOfSurah(surahNo) } }
        SurahNeeds(surahNo, surahConcepts, surahWords)
    }

    suspend fun needs(surahs: Collection<Int>): Map<Int, SurahNeeds> = surahs.associateWith { needs(it) }

    companion object {
        @Volatile
        private var instance: PathRepository? = null

        fun get(context: Context): PathRepository {
            val app = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: PathRepository(DatabaseProvider.getQuranRepository(app)) { WordRepository.open(app) }
                    .also { instance = it }
            }
        }
    }
}
