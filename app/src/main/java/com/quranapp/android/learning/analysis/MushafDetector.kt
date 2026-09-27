package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds

/**
 * What the muṣḥaf shows with its own spellings and marks:
 * - the disjointed letters that open 29 surahs, read by their names, and their madd
 * - the saktas of Ḥafṣ, a small س over a letter (مَنۡۜ رَاقٖ)
 * - Ḥafṣ's special words: imāla (مَجۡر۪ىٰهَا), ishmām and tas-hīl (تَأۡمَ۬نَّا, ءَا۬عۡجَمِيّٞ),
 *   ṣād read as sīn (يَبۡصُۜطُ) and naql (ٱلِٱسۡمُ)
 * - words you may start on with hamzat al-waṣl (ٱهۡدِنَا), other than ال
 * - special spellings: a wāw read as ā (ٱلصَّلَوٰةَ), a silent letter inside a word (أُوْلَٰٓئِكَ, مِاْئَةَ)
 */
object MushafDetector {
    fun detect(words: List<List<LetterCluster>>): List<Occurrence> {
        val found = mutableListOf<Occurrence>()
        words.forEachIndexed { index, word ->
            if (word.isEmpty()) return@forEachIndexed
            fun add(conceptId: String) {
                found += Occurrence(conceptId, index)
            }
            if (isDisjointedLetters(word)) {
                add(ConceptIds.DISJOINTED_LETTERS)
                if (word.any { it.hasAny(Arabic.MADD_SIGNS) }) add(ConceptIds.MADD_MUQATTAAT)
                return@forEachIndexed
            }
            val sinOverSad = word.any { isSinOverSad(it) }
            if (word.any { it.has(Arabic.SMALL_HIGH_SEEN) && !isSinOverSad(it) }) add(ConceptIds.SAKTA)
            if (sinOverSad || word.any { it.has(Arabic.IMALA_MARK) || it.has(Arabic.ISHMAM_MARK) } || isNaql(word)) {
                add(ConceptIds.HAFS_WORDS)
            }
            if (word[0].letter == Arabic.ALIF_WASLA && word.getOrNull(1)?.letter != Arabic.LAM) add(ConceptIds.WASL_START)
            if (hasSpecialSpelling(word)) add(ConceptIds.SPECIAL_SPELLINGS)
        }
        return found
    }

    /**
     * Letters read by their names (الٓمٓ, يسٓ, نٓ): a word with no vowel, sukūn or shadda at all.
     * The Quran's text is voweled everywhere else.
     */
    fun isDisjointedLetters(word: List<LetterCluster>): Boolean = word.isNotEmpty() && word.none { it.hasAny(Arabic.VOICING_MARKS) }

    /** A small س over a ص: the ص is read as س (يَبۡصُۜطُ, بَصۜۡطَةٗ). Elsewhere the same mark is a sakta. */
    fun isSinOverSad(c: LetterCluster): Boolean = c.letter == Arabic.SAD && c.has(Arabic.SMALL_HIGH_SEEN)

    /** ٱلِٱسۡمُ (49:11): the hamza of ٱسۡم is dropped and its kasra moves to the lām. */
    private fun isNaql(word: List<LetterCluster>): Boolean =
        word.size > 2 && word[0].letter == Arabic.ALIF_WASLA && word[1].letter == Arabic.LAM && word[1].has(Arabic.KASRA) &&
            word[2].letter == Arabic.ALIF_WASLA

    private fun hasSpecialSpelling(word: List<LetterCluster>): Boolean = word.indices.any { i ->
        val c = word[i]
        (c.letter == Arabic.WAW && c.has(Arabic.DAGGER_ALIF) && !c.hasAny(Arabic.SHORT_VOWELS)) ||
            (c.has(Arabic.SILENT_LETTER_MARK) && i < word.lastIndex) ||
            c.has(Arabic.SMALL_HIGH_NOON)
    }
}
