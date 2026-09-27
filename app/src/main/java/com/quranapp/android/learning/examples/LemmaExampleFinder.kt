package com.quranapp.android.learning.examples

import com.quranapp.android.learning.pack.WordLocation

/**
 * Ayahs where a dictionary word occurs, in the same order as concept examples: Al-Fātiḥah,
 * then the short surahs of Juz ʿAmma, then the rest ([ConceptExampleFinder.SEARCH_ORDER]).
 *
 * Unlike concepts, where a word occurs is already in the learning pack, so nothing has to be
 * analysed: the pack gives the locations and only the chosen ayahs' text is loaded.
 */
class LemmaExampleFinder(
    /** Every place the lemma occurs, in any order. */
    private val occurrences: suspend (lemmaId: Int) -> List<WordLocation>,
    /** An ayah's words in the Uthmani script. */
    private val loadAyah: suspend (ayahId: Int) -> List<String>,
) {
    suspend fun find(lemmaId: Int, limit: Int): List<ConceptExample> {
        val byAyah = occurrences(lemmaId).groupBy { it.ayahId }
        return byAyah.keys
            .sortedWith(compareBy({ SURAH_RANK.getValue(it / 1000) }, { it }))
            .take(limit)
            .map { ayahId ->
                ConceptExample(
                    ayah = AyahWords(ayahId / 1000, ayahId % 1000, loadAyah(ayahId)),
                    highlightedWordIndexes = byAyah.getValue(ayahId).map { it.wordIndex }.toSet(),
                )
            }
    }

    private companion object {
        val SURAH_RANK: Map<Int, Int> =
            ConceptExampleFinder.SEARCH_ORDER.withIndex().associate { (rank, surahNo) -> surahNo to rank }
    }
}
