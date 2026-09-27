package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds

/**
 * What changes when you stop at the end of the ayah (the reading concept "stopping on a
 * word", and madd ʿāriḍ, līn and ʿiwaḍ), and two madds found anywhere: badal, a hamza before
 * its long vowel, and ṣila, the long vowel of the hāʾ of "him".
 *
 * As in the other detectors, we assume the reader stops at the end of the ayah and nowhere
 * else, so the stopping rules are reported on the last word only.
 */
object StopAndMaddDetector {
    private val HAMZA_MARKS = setOf(Arabic.HAMZA_ABOVE, Arabic.HAMZA_BELOW)
    private val FATHATAN = setOf(Arabic.FATHATAN, Arabic.OPEN_FATHATAN)
    private val SMALL_WAW_OR_YA = setOf(Arabic.SMALL_WAW, Arabic.SMALL_YA)

    fun detect(words: List<List<LetterCluster>>): List<Occurrence> {
        val found = mutableListOf<Occurrence>()
        val lastWord = words.indexOfLast { it.isNotEmpty() }
        words.forEachIndexed { index, word ->
            if (hasBadal(word)) found += Occurrence(ConceptIds.MADD_BADAL, index)
            if (hasSila(word, isLastWord = index == lastWord)) found += Occurrence(ConceptIds.MADD_SILA, index)
        }
        if (lastWord >= 0) stoppingRules(words[lastWord]).forEach { found += Occurrence(it, lastWord) }
        return found
    }

    /** An alif, alif maqṣūra or tatweel that only carries a long vowel or tanwīn. */
    private fun isSeat(c: LetterCluster): Boolean =
        (c.letter == Arabic.ALIF || c.letter == Arabic.ALIF_MAQSURA || c.letter == Arabic.TATWEEL) && c.isBare && !c.hasAny(HAMZA_MARKS)

    private fun isHamza(c: LetterCluster): Boolean = c.letter in Arabic.HAMZA_LETTERS || c.hasAny(HAMZA_MARKS)

    /**
     * The last word when you stop on it: its last vowel drops (stopping), tanwīn fatḥ becomes
     * ā (ʿiwaḍ), and a long vowel or a wāw or yāʾ with sukūn after a fatḥa just before the
     * last letter may be stretched (ʿāriḍ, līn). A word that already ends in a sukūn or a long
     * vowel doesn't change.
     */
    private fun stoppingRules(word: List<LetterCluster>): List<String> {
        var j = word.lastIndex
        while (j >= 0 && (isSeat(word[j]) || word[j].hasAny(Arabic.SILENT_MARKS))) j--
        if (j < 0) return emptyList()
        val last = word[j]
        val fathatan = last.hasAny(FATHATAN)
        if (j < word.lastIndex && !fathatan) return emptyList() // it ends in a long vowel
        val voiced = last.hasAny(Arabic.SHORT_VOWELS) || last.hasAny(Arabic.TANWEEN) || last.letter == Arabic.TA_MARBUTA
        if (!voiced) return emptyList()

        val rules = mutableListOf(ConceptIds.STOPPING)
        if (fathatan && last.letter != Arabic.TA_MARBUTA) return rules + ConceptIds.MADD_IWAD
        if (j == 0 || isHamza(last)) return rules // a hamza after a long vowel is madd muttaṣil
        val previous = word[j - 1]
        if (previous.hasAny(Arabic.MADD_SIGNS)) return rules
        val before = word.getOrNull(j - 2)
        val longVowel = previous.has(Arabic.DAGGER_ALIF) || (
            before != null && previous.isBare && (
                (previous.letter == Arabic.ALIF && before.has(Arabic.FATHA)) ||
                    (previous.letter == Arabic.WAW && before.has(Arabic.DAMMA)) ||
                    (previous.letter == Arabic.YA && before.has(Arabic.KASRA))
                )
            )
        val leen = (previous.letter == Arabic.WAW || previous.letter == Arabic.YA) && previous.has(Arabic.SUKUN) &&
            before?.has(Arabic.FATHA) == true
        return when {
            longVowel -> rules + ConceptIds.MADD_ARID
            leen -> rules + ConceptIds.MADD_LEEN
            else -> rules
        }
    }

    /**
     * A hamza followed by its long vowel (ءَامَنُواْ, إِيمَٰن, أُوتُواْ, ٱلۡأٓخِرَةِ), and then a
     * letter that is neither a hamza, a sukūn nor a shadda: those make it another madd.
     */
    private fun hasBadal(word: List<LetterCluster>): Boolean = word.indices.any { i ->
        val hamza = word[i]
        if (!isHamza(hamza)) return@any false
        val next = word.getOrNull(i + 1)
        val longVowel: Boolean
        val after: LetterCluster?
        if (hamza.hasAny(Arabic.MADD_SIGNS) || hamza.has(Arabic.DAGGER_ALIF)) {
            longVowel = true
            after = next
        } else {
            longVowel = next != null && next.isBare && !next.hasAny(Arabic.MADD_SIGNS) && !next.hasAny(Arabic.SILENT_MARKS) && (
                (hamza.has(Arabic.FATHA) && next.letter == Arabic.ALIF) ||
                    (hamza.has(Arabic.DAMMA) && next.letter == Arabic.WAW) ||
                    (hamza.has(Arabic.KASRA) && next.letter == Arabic.YA)
                )
            after = word.getOrNull(i + 2)
        }
        longVowel && after != null && !isHamza(after) && !after.has(Arabic.SUKUN) && !after.has(Arabic.SHADDA)
    }

    /** The hāʾ of "him" written with a small wāw or yāʾ (لَهُۥ, بِهِۦ), unless you stop on it. */
    private fun hasSila(word: List<LetterCluster>, isLastWord: Boolean): Boolean = word.indices.any { i ->
        word[i].letter == Arabic.HA && word[i].hasAny(SMALL_WAW_OR_YA) && !(isLastWord && i == word.lastIndex)
    }
}
