package com.quranapp.android.learning.words

import com.quranapp.android.learning.pack.LemmaEntity
import com.quranapp.android.learning.pack.RootEntity
import com.quranapp.android.learning.pack.SegmentEntity
import com.quranapp.android.learning.pack.SyntaxEntity

/** A dictionary word (lemma) and its root, if it has one. */
data class WordLemma(
    val lemma: LemmaEntity,
    val root: RootEntity?,
) {
    /** The id a learner's progress is stored under: stable across pack versions. */
    val itemId: String get() = WordItems.idOf(lemma.lemmaKey)
}

/**
 * Everything the learning pack knows about one word of an ayah.
 *
 * A written word can hold more than one dictionary word: وَمِمَّا is و + مِن + مَا. So
 * [lemmas] is a list; prefixes such as وَ and ٱلۡ have no lemma of their own.
 */
data class AyahWord(
    val ayahId: Int,
    val wordIndex: Int,
    val segments: List<SegmentEntity>,
    val lemmas: List<WordLemma>,
    /** An English meaning of the word in this ayah ("we worship"), if the pack has one. */
    val gloss: String?,
    /** The word's segments' roles in the sentence, from MASAQ. Empty for a few words. */
    val syntax: List<SyntaxEntity>,
)

/** Ids for vocabulary items in the learner's progress, next to concept ids like `tajweed.ikhfa`. */
object WordItems {
    private const val PREFIX = "word."

    fun idOf(lemmaKey: String) = PREFIX + lemmaKey

    fun isWordItem(itemId: String) = itemId.startsWith(PREFIX)

    fun lemmaKeyOf(itemId: String): String? = itemId.takeIf(::isWordItem)?.removePrefix(PREFIX)
}
