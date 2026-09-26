package com.quranapp.android.learning.analysis

/**
 * The Unicode characters that appear in the app's Uthmani Quran text
 * (script code `uthmani` in `quranapp.db`).
 *
 * Careful: the text was prepared for the KFGQPC Uthmanic font, so a few codepoints are used
 * with a different meaning from their official Unicode name. Those are marked "(!)" below.
 * The meanings were checked against the whole Quran; see docs/learning/DESIGN.md, section 4.
 */
object Arabic {
    // ---- Letters ----
    const val HAMZA = 'ء' // ء
    const val ALIF_WITH_HAMZA_ABOVE = 'أ' // أ
    const val WAW_WITH_HAMZA = 'ؤ' // ؤ
    const val ALIF_WITH_HAMZA_BELOW = 'إ' // إ
    const val YA_WITH_HAMZA = 'ئ' // ئ
    const val ALIF = 'ا' // ا
    const val BA = 'ب' // ب
    const val TA_MARBUTA = 'ة' // ة
    const val RA = 'ر' // ر
    const val LAM = 'ل' // ل
    const val MEEM = 'م' // م
    const val NOON = 'ن' // ن
    const val HA = 'ه' // ه
    const val WAW = 'و' // و
    const val ALIF_MAQSURA = 'ى' // ى
    const val YA = 'ي' // ي
    const val ALIF_WASLA = 'ٱ' // ٱ

    /** The stretching line ـ. Marks such as a dagger alif can be written on it. */
    const val TATWEEL = 'ـ'

    // ---- Marks written above or below a letter ----
    const val FATHATAN = 'ً' // ً
    const val DAMMATAN = 'ٌ' // ٌ
    const val KASRATAN = 'ٍ' // ٍ
    const val FATHA = 'َ' // َ
    const val DAMMA = 'ُ' // ُ
    const val KASRA = 'ِ' // ِ
    const val SHADDA = 'ّ' // ّ

    /** (!) Unicode "sukun". Here: the small circle over a letter that is never pronounced. */
    const val SILENT_LETTER_MARK = 'ْ'

    /** The small oval over a letter pronounced only when stopping, as in أَنَا۠. */
    const val SILENT_WHEN_CONTINUING_MARK = '۠'

    /** (!) Unicode "small high dotless head of khah". Here: the normal sukun. */
    const val SUKUN = 'ۡ'

    const val MADDAH = 'ٓ' // ٓ
    const val SMALL_HIGH_MADDA = 'ۤ'
    const val HAMZA_ABOVE = 'ٔ'
    const val HAMZA_BELOW = 'ٕ'
    const val DAGGER_ALIF = 'ٰ' // ٰ

    /** (!) Unicode "subscript alef". Here: kasratan in its staggered form (merged/hidden noon). */
    const val OPEN_KASRATAN = 'ٖ'

    /** (!) Unicode "inverted damma". Here: fathatan in its staggered form. */
    const val OPEN_FATHATAN = 'ٗ'

    /** (!) Unicode "fatha with two dots". Here: dammatan in its staggered form. */
    const val OPEN_DAMMATAN = 'ٞ'

    /** Small meem above a noon or a vowel: the noon sound becomes a meem (iqlab). */
    const val IQLAB_MEEM = 'ۢ'

    /** The same as [IQLAB_MEEM], written below a kasra. */
    const val IQLAB_MEEM_BELOW = 'ۭ'

    const val SMALL_WAW = 'ۥ' // ۥ
    const val SMALL_YA = 'ۦ' // ۦ
    const val SMALL_HIGH_YA = 'ۧ'
    const val SAJDAH_SIGN = '۩' // ۩

    // ---- Groups ----
    val SHORT_VOWELS = setOf(FATHA, DAMMA, KASRA)
    val STACKED_TANWEEN = setOf(FATHATAN, DAMMATAN, KASRATAN)
    val OPEN_TANWEEN = setOf(OPEN_FATHATAN, OPEN_DAMMATAN, OPEN_KASRATAN)
    val TANWEEN = STACKED_TANWEEN + OPEN_TANWEEN
    val IQLAB_MARKS = setOf(IQLAB_MEEM, IQLAB_MEEM_BELOW)
    val MADD_SIGNS = setOf(MADDAH, SMALL_HIGH_MADDA)
    val SMALL_MADD_LETTERS = setOf(SMALL_WAW, SMALL_YA, SMALL_HIGH_YA)
    val SILENT_MARKS = setOf(SILENT_LETTER_MARK, SILENT_WHEN_CONTINUING_MARK)

    /** Pause marks: ۖ ۗ ۘ ۙ ۚ ۛ ۜ */
    val STOP_SIGNS = setOf('ۖ', 'ۗ', 'ۘ', 'ۙ', 'ۚ', 'ۛ', 'ۜ')

    val HAMZA_LETTERS =
        setOf(HAMZA, ALIF_WITH_HAMZA_ABOVE, WAW_WITH_HAMZA, ALIF_WITH_HAMZA_BELOW, YA_WITH_HAMZA)

    /** Marks that give a letter its own sound: vowels, tanween, sukun and shadda. */
    val VOICING_MARKS = SHORT_VOWELS + TANWEEN + SUKUN + SHADDA

    fun isLetter(ch: Char): Boolean = ch in 'ء'..'ي' || ch == ALIF_WASLA

    fun isMark(ch: Char): Boolean =
        ch in 'ً'..'ٟ' || ch == DAGGER_ALIF || ch in 'ۖ'..'ۭ'
}
