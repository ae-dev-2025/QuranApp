package com.quranapp.android.learning.analysis

/**
 * One letter together with the marks written on it.
 *
 * Arabic stores vowels as separate characters that follow their letter, so the word بِسۡمِ is
 * six characters: ب ِ س ۡ م ِ. Grouping them gives three clusters: بِ, سۡ and مِ.
 */
data class LetterCluster(val letter: Char, val marks: String) {
    fun has(mark: Char): Boolean = mark in marks

    fun hasAny(candidates: Set<Char>): Boolean = marks.any { it in candidates }

    /** True if the letter has no vowel, tanween, sukun or shadda of its own. */
    val isBare: Boolean get() = !hasAny(Arabic.VOICING_MARKS)

    override fun toString(): String = letter + marks
}

/**
 * Splits a word into [LetterCluster]s. Characters before the first letter (such as the ۞
 * that starts a hizb quarter) and anything that is neither a letter nor a mark are ignored.
 */
fun parseClusters(word: String): List<LetterCluster> {
    val clusters = mutableListOf<LetterCluster>()
    var letter: Char? = null
    val marks = StringBuilder()

    for (ch in word) {
        when {
            Arabic.isLetter(ch) -> {
                // A new letter starts: save the previous one with the marks collected so far.
                if (letter != null) clusters += LetterCluster(letter, marks.toString())
                letter = ch
                marks.clear()
            }

            Arabic.isMark(ch) && letter != null -> marks.append(ch)
        }
    }

    if (letter != null) clusters += LetterCluster(letter, marks.toString())

    return clusters
}
