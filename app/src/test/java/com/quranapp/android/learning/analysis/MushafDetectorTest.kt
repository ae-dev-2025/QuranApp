package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds.DISJOINTED_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.HAFS_WORDS
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUQATTAAT
import com.quranapp.android.learning.concepts.ConceptIds.QALQALAH
import com.quranapp.android.learning.concepts.ConceptIds.SAKTA
import com.quranapp.android.learning.concepts.ConceptIds.SPECIAL_SPELLINGS
import com.quranapp.android.learning.concepts.ConceptIds.STOP_SIGNS
import com.quranapp.android.learning.concepts.ConceptIds.WASL_START
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** The muṣḥaf's own spellings and marks, on ayahs copied from the app's text. */
class MushafDetectorTest {
    private val AYAH_2_1 = listOf("الٓمٓ", "١")
    private val AYAH_20_1 = listOf("طه", "١")
    private val AYAH_42_2 = listOf("عٓسٓقٓ", "٢")
    private val AYAH_75_27 = listOf("وَقِيلَ", "مَنۡۜ", "رَاقٖ", "٢٧")
    private val AYAH_2_245 = listOf("مَّن", "ذَا", "ٱلَّذِي", "يُقۡرِضُ", "ٱللَّهَ", "قَرۡضًا", "حَسَنٗا", "فَيُضَٰعِفَهُۥ", "لَهُۥٓ", "أَضۡعَافٗا", "كَثِيرَةٗۚ", "وَٱللَّهُ", "يَقۡبِضُ", "وَيَبۡصُۜطُ", "وَإِلَيۡهِ", "تُرۡجَعُونَ", "٢٤٥")
    private val AYAH_11_41 = listOf("۞ وَقَالَ", "ٱرۡكَبُواْ", "فِيهَا", "بِسۡمِ", "ٱللَّهِ", "مَجۡر۪ىٰهَا", "وَمُرۡسَىٰهَآۚ", "إِنَّ", "رَبِّي", "لَغَفُورٞ", "رَّحِيمٞ", "٤١")
    private val AYAH_12_11 = listOf("قَالُواْ", "يَٰٓأَبَانَا", "مَا", "لَكَ", "لَا", "تَأۡمَ۬نَّا", "عَلَىٰ", "يُوسُفَ", "وَإِنَّا", "لَهُۥ", "لَنَٰصِحُونَ", "١١")
    private val AYAH_49_11 = listOf("يَٰٓأَيُّهَا", "ٱلَّذِينَ", "ءَامَنُواْ", "لَا", "يَسۡخَرۡ", "قَوۡمٞ", "مِّن", "قَوۡمٍ", "عَسَىٰٓ", "أَن", "يَكُونُواْ", "خَيۡرٗا", "مِّنۡهُمۡ", "وَلَا", "نِسَآءٞ", "مِّن", "نِّسَآءٍ", "عَسَىٰٓ", "أَن", "يَكُنَّ", "خَيۡرٗا", "مِّنۡهُنَّۖ", "وَلَا", "تَلۡمِزُوٓاْ", "أَنفُسَكُمۡ", "وَلَا", "تَنَابَزُواْ", "بِٱلۡأَلۡقَٰبِۖ", "بِئۡسَ", "ٱلِٱسۡمُ", "ٱلۡفُسُوقُ", "بَعۡدَ", "ٱلۡإِيمَٰنِۚ", "وَمَن", "لَّمۡ", "يَتُبۡ", "فَأُوْلَٰٓئِكَ", "هُمُ", "ٱلظَّٰلِمُونَ", "١١")
    private val AYAH_1_6 = listOf("ٱهۡدِنَا", "ٱلصِّرَٰطَ", "ٱلۡمُسۡتَقِيمَ", "٦")
    private val AYAH_98_5 = listOf("وَمَآ", "أُمِرُوٓاْ", "إِلَّا", "لِيَعۡبُدُواْ", "ٱللَّهَ", "مُخۡلِصِينَ", "لَهُ", "ٱلدِّينَ", "حُنَفَآءَ", "وَيُقِيمُواْ", "ٱلصَّلَوٰةَ", "وَيُؤۡتُواْ", "ٱلزَّكَوٰةَۚ", "وَذَٰلِكَ", "دِينُ", "ٱلۡقَيِّمَةِ", "٥")
    private val AYAH_2_5 = listOf("أُوْلَٰٓئِكَ", "عَلَىٰ", "هُدٗى", "مِّن", "رَّبِّهِمۡۖ", "وَأُوْلَٰٓئِكَ", "هُمُ", "ٱلۡمُفۡلِحُونَ", "٥")
    private val AYAH_21_88 = listOf("فَٱسۡتَجَبۡنَا", "لَهُۥ", "وَنَجَّيۡنَٰهُ", "مِنَ", "ٱلۡغَمِّۚ", "وَكَذَٰلِكَ", "نُـۨجِي", "ٱلۡمُؤۡمِنِينَ", "٨٨")

    private fun found(words: List<String>): Map<String, List<Int>> = AyahAnalyzer.analyze(words).wordsByConcept

    @Test
    fun disjointedLettersAndTheirMadd() {
        assertEquals(listOf(0), found(AYAH_2_1)[DISJOINTED_LETTERS])
        assertEquals("الٓمٓ: lām and mīm, six counts", listOf(0), found(AYAH_2_1)[MADD_MUQATTAAT])
        assertEquals("طه: two counts only", null, found(AYAH_20_1)[MADD_MUQATTAAT])
        assertEquals(listOf(0), found(AYAH_20_1)[DISJOINTED_LETTERS])
        assertFalse("the ق of عٓسٓقٓ is qāf, not a bounce", QALQALAH in found(AYAH_42_2))
    }

    @Test
    fun saktaAndSadReadAsSin() {
        assertEquals("مَنۡۜ", listOf(1), found(AYAH_75_27)[SAKTA])
        // وَيَبۡصُۜطُ: the same small س over a ص means "read it as س", not a pause.
        assertEquals(listOf(13), found(AYAH_2_245)[HAFS_WORDS])
        assertEquals(null, found(AYAH_2_245)[SAKTA])
        assertFalse(13 in found(AYAH_2_245)[STOP_SIGNS].orEmpty())
    }

    @Test
    fun hafsSpecialWords() {
        assertEquals("مَجۡر۪ىٰهَا", listOf(5), found(AYAH_11_41)[HAFS_WORDS])
        assertEquals("تَأۡمَ۬نَّا", listOf(5), found(AYAH_12_11)[HAFS_WORDS])
        assertEquals("ٱلِٱسۡمُ", listOf(29), found(AYAH_49_11)[HAFS_WORDS])
    }

    @Test
    fun startingOnHamzatAlWaslAndSpecialSpellings() {
        assertEquals("ٱهۡدِنَا, but not ٱلصِّرَٰطَ", listOf(0), found(AYAH_1_6)[WASL_START])
        assertEquals("ٱلصَّلَوٰةَ, ٱلزَّكَوٰةَ", listOf(10, 12), found(AYAH_98_5)[SPECIAL_SPELLINGS])
        assertEquals("أُوْلَٰٓئِكَ", listOf(0, 5), found(AYAH_2_5)[SPECIAL_SPELLINGS])
        assertEquals("نُـۨجِي", listOf(6), found(AYAH_21_88)[SPECIAL_SPELLINGS])
    }
}
