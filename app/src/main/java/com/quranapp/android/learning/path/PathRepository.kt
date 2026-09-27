package com.quranapp.android.learning.path

import android.content.Context
import com.quranapp.android.db.DatabaseProvider
import com.quranapp.android.learning.analysis.ConceptIndex
import com.quranapp.android.learning.concepts.Concept
import com.quranapp.android.learning.words.GrammarIndex
import com.quranapp.android.learning.words.WordRepository
import com.quranapp.android.repository.QuranRepository
import com.quranapp.android.utils.reader.QuranScriptUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads what each surah needs, once per app run: the Quran text never changes, and the
 * words only change when the learning pack is downloaded or deleted. A surah's reading and
 * recitation concepts come from the shipped [ConceptIndex], and its grammar from the
 * [GrammarIndex] once that's made, so no ayah has to be analysed.
 */
class PathRepository(
    private val quran: QuranRepository,
    private val openWords: suspend () -> WordRepository?,
    private val index: suspend () -> ConceptIndex? = { null },
    private val grammarIndex: suspend () -> ConceptIndex? = { null },
) {
    private val mutex = Mutex()
    private val concepts = HashMap<Int, List<Concept>>()
    private val words = HashMap<Int, List<List<String>>>()
    private val grammar = HashMap<Int, List<Concept>>()

    suspend fun needs(surahNo: Int): SurahNeeds = mutex.withLock {
        val surahConcepts = concepts.getOrPut(surahNo) {
            index()?.let { return@getOrPut SurahNeeds.conceptsFound(it.conceptsOf(surahNo)) }
            // Without the index, analyse the text. Always the Unicode text: the other scripts
            // store font glyph codes, not letters.
            val ayahs = quran.getWordsForSurah(surahNo, QuranScriptUtils.SCRIPT_UTHMANI).values.map { ayah -> ayah.map { it.text } }
            SurahNeeds.conceptsOf(ayahs)
        }
        SurahNeeds(surahNo, surahConcepts, wordsLocked(surahNo), grammarLocked(surahNo))
    }

    private suspend fun grammarLocked(surahNo: Int): List<Concept>? {
        val repository = openWords()
        if (repository == null) {
            grammar.clear()
            return null
        }
        return grammar.getOrPut(surahNo) {
            SurahNeeds.grammarOf(grammarIndex()?.conceptsOf(surahNo) ?: repository.grammarOfSurah(surahNo))
        }
    }

    /** Just a surah's words, without analysing its text; null without the pack. */
    suspend fun words(surahNo: Int): List<List<String>>? = mutex.withLock { wordsLocked(surahNo) }

    private suspend fun wordsLocked(surahNo: Int): List<List<String>>? {
        val repository = openWords()
        if (repository == null) {
            words.clear() // the pack was deleted: forget its words
            return null
        }
        return words.getOrPut(surahNo) { repository.wordItemsOfSurah(surahNo) }
    }

    suspend fun needs(surahs: Collection<Int>): Map<Int, SurahNeeds> = surahs.associateWith { needs(it) }

    companion object {
        @Volatile
        private var instance: PathRepository? = null

        fun get(context: Context): PathRepository {
            val app = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: PathRepository(
                    quran = DatabaseProvider.getQuranRepository(app),
                    openWords = { WordRepository.open(app) },
                    index = { ConceptIndex.get(app) },
                    grammarIndex = { GrammarIndex.get(app) },
                ).also { instance = it }
            }
        }
    }
}
