package com.quranapp.android.learning.words

/**
 * How many of an ayah's words the learner knows (decision 9: "words %").
 *
 * Words are counted as they appear, so a word that occurs twice counts twice: that is what
 * a reader meets. A word counts as known when every dictionary word in it is known
 * (وَمِمَّا needs مِن and مَا). Words without any dictionary word, such as a detached
 * pronoun, which the corpus gives no lemma, aren't counted either way.
 *
 * Research on reading suggests about 98% of words known lets a reader follow a text
 * unaided, and 95% with the odd look-up (Hu and Nation, 2000).
 */
data class WordCoverage(
    val knownWords: Int,
    val countedWords: Int,
    /** The dictionary words still to learn, in the order they first appear. */
    val unknownLemmas: List<WordLemma>,
) {
    val percent: Int get() = if (countedWords == 0) 100 else knownWords * 100 / countedWords

    companion object {
        fun of(words: List<AyahWord>, knownItemIds: Set<String>): WordCoverage {
            val counted = words.filter { it.lemmas.isNotEmpty() }
            val known = counted.count { word -> word.lemmas.all { it.itemId in knownItemIds } }
            val unknown = counted
                .flatMap { it.lemmas }
                .filter { it.itemId !in knownItemIds }
                .distinctBy { it.lemma.lemmaId }
            return WordCoverage(known, counted.size, unknown)
        }
    }
}
