package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds

/**
 * Where the sounds of a word are made: the five places (makhārij) of the design. A place is
 * found in a word when one of its letters is sounded there.
 *
 * Wāw and yāʾ count for the lips and the tongue only as consonants (with a vowel, sukūn or
 * shadda); as long vowels they flow from the empty space. The nose is where a ghunnah is.
 */
object MakharijDetector {
    private val THROAT = setOf('ء', 'أ', 'إ', 'ؤ', 'ئ', 'ه', 'ع', 'ح', 'غ', 'خ')
    private val TONGUE = setOf('ق', 'ك', 'ج', 'ش', 'ض', 'ل', 'ن', 'ر', 'ط', 'د', 'ت', 'ص', 'س', 'ز', 'ظ', 'ذ', 'ث')
    private val LIPS = setOf('ف', 'ب', 'م')

    /** Concepts on a word that are read with a long vowel. */
    private val LONG = setOf(ConceptIds.LONG_VOWELS, ConceptIds.DAGGER_ALIF, ConceptIds.SMALL_MADD_LETTERS)

    /** Concepts on a word that carry a ghunnah. */
    private val WITH_GHUNNAH = setOf(
        ConceptIds.GHUNNAH,
        ConceptIds.IDGHAM_GHUNNAH,
        ConceptIds.IQLAB,
        ConceptIds.IKHFA,
        ConceptIds.IDGHAM_SHAFAWI,
        ConceptIds.IKHFA_SHAFAWI,
    )

    /** @param onWord the concepts the other detectors found on this word. */
    fun detect(clusters: List<LetterCluster>, onWord: Set<String>): Set<String> {
        val found = mutableSetOf<String>()
        for (c in clusters) {
            if (c.hasAny(Arabic.SILENT_MARKS)) continue // written but not said
            val consonant = !c.isBare
            when {
                c.letter in THROAT -> found += ConceptIds.MAKHRAJ_THROAT
                c.letter in TONGUE || (c.letter == Arabic.YA && consonant) -> found += ConceptIds.MAKHRAJ_TONGUE
                c.letter in LIPS || (c.letter == Arabic.WAW && consonant) -> found += ConceptIds.MAKHRAJ_LIPS
            }
        }
        if (onWord.any { it in LONG }) found += ConceptIds.MAKHRAJ_JAWF
        if (onWord.any { it in WITH_GHUNNAH }) found += ConceptIds.MAKHRAJ_NOSE
        return found
    }
}
