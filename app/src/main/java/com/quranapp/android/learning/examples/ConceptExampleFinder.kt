package com.quranapp.android.learning.examples

import com.quranapp.android.learning.analysis.AyahAnalyzer

/** The Uthmani words of one ayah. */
data class AyahWords(val surahNo: Int, val ayahNo: Int, val words: List<String>)

/** An ayah that shows a concept, and which of its words to highlight (0-based). */
data class ConceptExample(val ayah: AyahWords, val highlightedWordIndexes: Set<Int>)

/**
 * Finds ayahs in which a concept occurs, to show as examples on its learning page.
 *
 * Surahs are searched in [SEARCH_ORDER]: Al-Fātiḥah, then Juz ʿAmma (the short surahs most
 * people know from prayer), then the rest of the Quran. So a learner meets familiar ayahs
 * first, and most concepts are found after reading only a few short surahs.
 *
 * @param loadSurah returns the ayahs of a surah, in order. It is a parameter rather than a
 * database call so tests can pass in a few ayahs directly.
 */
class ConceptExampleFinder(
    private val loadSurah: suspend (surahNo: Int) -> List<AyahWords>,
) {
    /**
     * The first [limit] examples of [conceptId] in [SEARCH_ORDER]. To show more, call again
     * with a bigger limit: the early surahs are short, so searching them again is cheap.
     */
    suspend fun find(conceptId: String, limit: Int): List<ConceptExample> {
        val examples = mutableListOf<ConceptExample>()

        for (surahNo in SEARCH_ORDER) {
            for (ayah in loadSurah(surahNo)) {
                val wordIndexes = AyahAnalyzer.analyze(ayah.words).wordsByConcept[conceptId]

                if (!wordIndexes.isNullOrEmpty()) {
                    examples += ConceptExample(ayah, wordIndexes.toSet())
                    if (examples.size == limit) return examples
                }
            }
        }

        return examples
    }

    companion object {
        /** Al-Fātiḥah, then Juz ʿAmma (An-Naba to An-Nās), then Al-Baqarah to Al-Mursalāt. */
        val SEARCH_ORDER: List<Int> = listOf(1) + (78..114) + (2..77)
    }
}
