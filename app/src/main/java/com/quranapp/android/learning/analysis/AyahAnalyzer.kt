package com.quranapp.android.learning.analysis

/**
 * The concepts needed to read one ayah.
 *
 * @property wordsByConcept for each concept ID, the 0-based indexes of the words it occurs in
 * (the same numbering as `word_index` in the `ayah_words` table). A rule that joins two words
 * lists both.
 */
data class AyahAnalysis(val wordsByConcept: Map<String, List<Int>>) {
    val conceptIds: Set<String> get() = wordsByConcept.keys
}

/** Runs all detectors over the words of one ayah. */
object AyahAnalyzer {
    /**
     * @param words the ayah's words in the Uthmani script (script code `uthmani`), in order.
     * The ayah-number marker at the end may be included; it contains no letters and is ignored.
     */
    fun analyze(words: List<String>): AyahAnalysis {
        val clustersPerWord = words.map { parseClusters(it) }
        val wordsByConcept = LinkedHashMap<String, MutableList<Int>>()

        fun add(conceptId: String, wordIndex: Int) {
            val indexes = wordsByConcept.getOrPut(conceptId) { mutableListOf() }
            if (wordIndex !in indexes) indexes += wordIndex
        }

        clustersPerWord.forEachIndexed { wordIndex, clusters ->
            ReadingDetector.detect(clusters).forEach { add(it, wordIndex) }
        }

        TajweedDetector.detect(clustersPerWord).forEach { add(it.conceptId, it.wordIndex) }

        return AyahAnalysis(wordsByConcept.mapValues { (_, indexes) -> indexes.sorted() })
    }
}
