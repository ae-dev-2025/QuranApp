package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds

/**
 * Finds the Reading-track concepts in a single word of the Uthmani text, e.g. which vowel
 * marks and special signs a learner must recognise to read it.
 *
 * Everything here looks at one word at a time. Rules that depend on the next word
 * (tajweed) live in a separate detector.
 */
object ReadingDetector {
    fun detect(word: String): Set<String> = detect(parseClusters(word))

    fun detect(clusters: List<LetterCluster>): Set<String> {
        if (clusters.isEmpty()) return emptySet()

        val found = mutableSetOf(ConceptIds.LETTERS)

        clusters.forEachIndexed { index, cluster ->
            if (cluster.hasAny(Arabic.SHORT_VOWELS)) found += ConceptIds.SHORT_VOWELS
            if (cluster.has(Arabic.SUKUN)) found += ConceptIds.SUKUN
            if (cluster.has(Arabic.SHADDA)) found += ConceptIds.SHADDA
            if (isTanween(cluster)) found += ConceptIds.TANWEEN
            if (isHamza(cluster)) found += ConceptIds.HAMZA
            if (cluster.letter == Arabic.TA_MARBUTA) found += ConceptIds.TA_MARBUTA
            if (cluster.has(Arabic.DAGGER_ALIF)) found += ConceptIds.DAGGER_ALIF
            if (cluster.hasAny(Arabic.SMALL_MADD_LETTERS)) found += ConceptIds.SMALL_MADD_LETTERS
            if (cluster.letter == Arabic.ALIF_WASLA) found += ConceptIds.HAMZAT_WASL
            if (cluster.hasAny(Arabic.SILENT_MARKS)) found += ConceptIds.SILENT_LETTERS
            // A small س over a ص isn't a pause: it says to read the ص as س (Ḥafṣ's special words).
            if (cluster.hasAny(Arabic.STOP_SIGNS) && !MushafDetector.isSinOverSad(cluster)) found += ConceptIds.STOP_SIGNS
            if (cluster.has(Arabic.SAJDAH_SIGN)) found += ConceptIds.SAJDAH

            if (index > 0 && isLongVowel(previous = clusters[index - 1], current = cluster)) {
                found += ConceptIds.LONG_VOWELS
            }
        }

        return found
    }

    /**
     * Tanween is usually a doubled vowel mark. When it is followed by ب (iqlab), the text
     * instead writes a single vowel plus a small meem, as in مُحِيطُۢ or عَوَانُۢ. A small meem
     * on a noon *without* a vowel is a noon sakinah (أَنۢبِيَآءَ), not tanween.
     */
    private fun isTanween(cluster: LetterCluster): Boolean {
        if (cluster.hasAny(Arabic.TANWEEN)) return true
        return cluster.hasAny(Arabic.SHORT_VOWELS) && cluster.hasAny(Arabic.IQLAB_MARKS)
    }

    private fun isHamza(cluster: LetterCluster): Boolean {
        return cluster.letter in Arabic.HAMZA_LETTERS ||
            cluster.has(Arabic.HAMZA_ABOVE) ||
            cluster.has(Arabic.HAMZA_BELOW)
    }

    /**
     * A long vowel is a bare alif, wāw or yāʾ after the matching short vowel:
     * fatha + ا or ى, damma + و, kasra + ي. A letter marked as silent doesn't count.
     */
    private fun isLongVowel(previous: LetterCluster, current: LetterCluster): Boolean {
        if (!current.isBare || current.hasAny(Arabic.SILENT_MARKS)) return false

        return when (current.letter) {
            Arabic.ALIF, Arabic.ALIF_MAQSURA -> previous.has(Arabic.FATHA)
            Arabic.WAW -> previous.has(Arabic.DAMMA)
            Arabic.YA -> previous.has(Arabic.KASRA)
            else -> false
        }
    }
}
