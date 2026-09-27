package com.quranapp.android.learning.letters

import com.quranapp.android.learning.analysis.Arabic
import com.quranapp.android.learning.examples.AyahWords

/** Where a letter is heard: a word of the Quran and its place. */
data class LetterExample(val surahNo: Int, val ayahNo: Int, val wordIndex: Int, val word: String)

/** Finds letters in the app's Uthmani text. Pure: the ayahs come from the caller. */
object LetterFinder {
    private val ALIF = Letters["letter.alif"]!!
    private val HAMZA = Letters["letter.hamza"]!!
    private val WAW = Letters["letter.waw"]!!
    private val YA = Letters["letter.ya"]!!

    /**
     * The letters written in a character. A hamza on a seat counts as both, since the learner
     * has to know both shapes: أ is alif and hamza. ٱ is an alif; ى is a yāʾ without its dots.
     * Tāʾ marbūṭa (ة) is its own reading concept, not a letter item.
     */
    fun lettersOf(char: Char): List<Letter> = when (char) {
        Arabic.ALIF, Arabic.ALIF_WASLA -> listOf(ALIF)
        Arabic.ALIF_WITH_HAMZA_ABOVE, Arabic.ALIF_WITH_HAMZA_BELOW, 'آ' -> listOf(ALIF, HAMZA)
        Arabic.WAW_WITH_HAMZA -> listOf(WAW, HAMZA)
        Arabic.YA_WITH_HAMZA -> listOf(YA, HAMZA)
        Arabic.ALIF_MAQSURA -> listOf(YA)
        Arabic.HAMZA_ABOVE, Arabic.HAMZA_BELOW -> listOf(HAMZA) // written as a mark on a seat
        else -> listOfNotNull(Letters.ofChar(char))
    }

    /** The ids of the letters a word is spelled with. */
    fun lettersIn(word: String): Set<String> = word.flatMap(::lettersOf).mapTo(LinkedHashSet()) { it.id }

    /**
     * A word where [letter] is heard first: the first word, in the order of [ayahs], that
     * starts with it; if none does (alif never starts a word as itself), the first that has
     * it. Words starting with ٱ are skipped as starts: the article's lām is often not heard.
     */
    fun exampleOf(letter: Letter, ayahs: List<AyahWords>): LetterExample? {
        var inside: LetterExample? = null
        for (ayah in ayahs) {
            // The app stores the ayah number as the last "word": it has no letters, so it never matches.
            ayah.words.forEachIndexed { index, word ->
                val first = word.firstOrNull { Arabic.isLetter(it) } ?: return@forEachIndexed
                if (first != Arabic.ALIF_WASLA && letter in lettersOf(first)) {
                    return LetterExample(ayah.surahNo, ayah.ayahNo, index, word)
                }
                if (inside == null && word.any { letter in lettersOf(it) }) {
                    inside = LetterExample(ayah.surahNo, ayah.ayahNo, index, word)
                }
            }
        }
        return inside
    }
}
