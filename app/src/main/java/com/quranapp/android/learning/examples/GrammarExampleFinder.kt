package com.quranapp.android.learning.examples

import com.quranapp.android.learning.practice.GrammarAyah
import java.util.concurrent.ConcurrentHashMap

/**
 * Ayahs that show a grammar concept, in the same order as other concept examples: Al-Fātiḥah,
 * then Juz ʿAmma, then the rest ([ConceptExampleFinder.SEARCH_ORDER]).
 *
 * Grammar is found from the learning pack's analysis of each word, one surah at a time, so a
 * common concept is found in the first short surahs. Each surah's grammar is kept, so "Show
 * more" doesn't ask the pack again.
 */
class GrammarExampleFinder(
    /** For each ayah id of a surah, its concepts and the 0-based words they're in; null without the pack. */
    private val grammarOfSurah: suspend (surahNo: Int) -> Map<Int, Map<String, List<Int>>>?,
    /** An ayah's words in the Uthmani script. */
    private val loadAyah: suspend (ayahId: Int) -> List<String>,
) {
    // Concurrent: "Show more" can start a search before a cancelled one has stopped.
    private val surahs = ConcurrentHashMap<Int, Map<Int, Map<String, List<Int>>>>()

    suspend fun find(conceptId: String, limit: Int): List<ConceptExample> =
        ayahs(conceptId, limit).map { ConceptExample(it.ayah, it.wordsByConcept.getValue(conceptId).toSet()) }

    /** The first [limit] ayahs with [conceptId], each with all the grammar found in it (for questions). */
    suspend fun ayahs(conceptId: String, limit: Int): List<GrammarAyah> {
        val found = mutableListOf<GrammarAyah>()
        for (surahNo in ConceptExampleFinder.SEARCH_ORDER) {
            val byAyah = surahs[surahNo] ?: (grammarOfSurah(surahNo) ?: return found).also { surahs[surahNo] = it }
            for (ayahId in byAyah.keys.sorted()) {
                val grammar = byAyah.getValue(ayahId)
                if (grammar[conceptId].isNullOrEmpty()) continue
                found += GrammarAyah(AyahWords(surahNo, ayahId % 1000, loadAyah(ayahId)), grammar)
                if (found.size == limit) return found
            }
        }
        return found
    }
}
