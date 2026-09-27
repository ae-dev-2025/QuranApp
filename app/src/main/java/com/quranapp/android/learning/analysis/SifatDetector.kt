package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.letters.LetterFinder
import com.quranapp.android.learning.letters.LetterQualities

/**
 * The letter qualities (ṣifāt) heard in a word: a quality is found where a letter that has
 * its marked side is said (a whispered letter for whispered and voiced, and so on).
 */
object SifatDetector {
    fun detect(clusters: List<LetterCluster>): Set<String> {
        val found = mutableSetOf<String>()
        for (cluster in clusters) {
            if (cluster.hasAny(Arabic.SILENT_MARKS)) continue // written but not said
            // A hamza on a seat is said as hamza.
            for (c in LetterFinder.lettersOf(cluster.letter).map { it.char }) {
                if (c in LetterQualities.WHISPERED) found += ConceptIds.SIFA_HAMS
                if (c in LetterQualities.STRONG) found += ConceptIds.SIFA_SHIDDA
                if (c in LetterQualities.RAISED) found += ConceptIds.SIFA_ISTILA
                if (c in LetterQualities.CLOSED) found += ConceptIds.SIFA_ITBAQ
                if (c in LetterQualities.WHISTLING) found += ConceptIds.SIFA_SAFIR
                if (c in LetterQualities.LEANING) found += ConceptIds.SIFA_TAKRIR
                if (c == LetterQualities.SPREADING || c == LetterQualities.LENGTHENING) found += ConceptIds.SIFA_TAFASHSHI
            }
        }
        return found
    }
}
