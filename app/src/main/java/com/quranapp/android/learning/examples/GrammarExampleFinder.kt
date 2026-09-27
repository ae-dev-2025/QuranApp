package com.quranapp.android.learning.examples

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

    suspend fun find(conceptId: String, limit: Int): List<ConceptExample> {
        val examples = mutableListOf<ConceptExample>()
        for (surahNo in ConceptExampleFinder.SEARCH_ORDER) {
            val byAyah = surahs[surahNo] ?: (grammarOfSurah(surahNo) ?: return examples).also { surahs[surahNo] = it }
            for (ayahId in byAyah.keys.sorted()) {
                val words = byAyah.getValue(ayahId)[conceptId]
                if (words.isNullOrEmpty()) continue
                examples += ConceptExample(AyahWords(surahNo, ayahId % 1000, loadAyah(ayahId)), words.toSet())
                if (examples.size == limit) return examples
            }
        }
        return examples
    }
}
