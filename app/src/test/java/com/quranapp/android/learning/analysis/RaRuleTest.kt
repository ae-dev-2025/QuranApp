package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds.RA_HEAVY
import com.quranapp.android.learning.concepts.ConceptIds.RA_LIGHT
import org.junit.Assert.assertEquals
import org.junit.Test

/** Heavy and light rāʾ, on ayahs copied from the app's text (the ayah number is the last "word"). */
class RaRuleTest {
    private val AYAH_1_2 = listOf("ٱلۡحَمۡدُ", "لِلَّهِ", "رَبِّ", "ٱلۡعَٰلَمِينَ", "٢")
    private val AYAH_1_7 = listOf("صِرَٰطَ", "ٱلَّذِينَ", "أَنۡعَمۡتَ", "عَلَيۡهِمۡ", "غَيۡرِ", "ٱلۡمَغۡضُوبِ", "عَلَيۡهِمۡ", "وَلَا", "ٱلضَّآلِّينَ", "٧")
    private val AYAH_97_1 = listOf("إِنَّآ", "أَنزَلۡنَٰهُ", "فِي", "لَيۡلَةِ", "ٱلۡقَدۡرِ", "١")
    private val AYAH_34_12 = listOf("وَلِسُلَيۡمَٰنَ", "ٱلرِّيحَ", "غُدُوُّهَا", "شَهۡرٞ", "وَرَوَاحُهَا", "شَهۡرٞۖ", "وَأَسَلۡنَا", "لَهُۥ", "عَيۡنَ", "ٱلۡقِطۡرِۖ", "وَمِنَ", "ٱلۡجِنِّ", "مَن", "يَعۡمَلُ", "بَيۡنَ", "يَدَيۡهِ", "بِإِذۡنِ", "رَبِّهِۦۖ", "وَمَن", "يَزِغۡ", "مِنۡهُمۡ", "عَنۡ", "أَمۡرِنَا", "نُذِقۡهُ", "مِنۡ", "عَذَابِ", "ٱلسَّعِيرِ", "١٢")
    private val AYAH_110_3 = listOf("فَسَبِّحۡ", "بِحَمۡدِ", "رَبِّكَ", "وَٱسۡتَغۡفِرۡهُۚ", "إِنَّهُۥ", "كَانَ", "تَوَّابَۢا", "٣")
    private val AYAH_11_41 = listOf("۞ وَقَالَ", "ٱرۡكَبُواْ", "فِيهَا", "بِسۡمِ", "ٱللَّهِ", "مَجۡر۪ىٰهَا", "وَمُرۡسَىٰهَآۚ", "إِنَّ", "رَبِّي", "لَغَفُورٞ", "رَّحِيمٞ", "٤١")
    private val AYAH_6_7 = listOf("وَلَوۡ", "نَزَّلۡنَا", "عَلَيۡكَ", "كِتَٰبٗا", "فِي", "قِرۡطَاسٖ", "فَلَمَسُوهُ", "بِأَيۡدِيهِمۡ", "لَقَالَ", "ٱلَّذِينَ", "كَفَرُوٓاْ", "إِنۡ", "هَٰذَآ", "إِلَّا", "سِحۡرٞ", "مُّبِينٞ", "٧")
    private val AYAH_26_63 = listOf("فَأَوۡحَيۡنَآ", "إِلَىٰ", "مُوسَىٰٓ", "أَنِ", "ٱضۡرِب", "بِّعَصَاكَ", "ٱلۡبَحۡرَۖ", "فَٱنفَلَقَ", "فَكَانَ", "كُلُّ", "فِرۡقٖ", "كَٱلطَّوۡدِ", "ٱلۡعَظِيمِ", "٦٣")
    private val AYAH_54_16 = listOf("فَكَيۡفَ", "كَانَ", "عَذَابِي", "وَنُذُرِ", "١٦")
    private val AYAH_89_5 = listOf("هَلۡ", "فِي", "ذَٰلِكَ", "قَسَمٞ", "لِّذِي", "حِجۡرٍ", "٥")
    private val AYAH_54_2 = listOf("وَإِن", "يَرَوۡاْ", "ءَايَةٗ", "يُعۡرِضُواْ", "وَيَقُولُواْ", "سِحۡرٞ", "مُّسۡتَمِرّٞ", "٢")
    private val AYAH_3_41 = listOf("قَالَ", "رَبِّ", "ٱجۡعَل", "لِّيٓ", "ءَايَةٗۖ", "قَالَ", "ءَايَتُكَ", "أَلَّا", "تُكَلِّمَ", "ٱلنَّاسَ", "ثَلَٰثَةَ", "أَيَّامٍ", "إِلَّا", "رَمۡزٗاۗ", "وَٱذۡكُر", "رَّبَّكَ", "كَثِيرٗا", "وَسَبِّحۡ", "بِٱلۡعَشِيِّ", "وَٱلۡإِبۡكَٰرِ", "٤١")
    private val AYAH_10_1 = listOf("الٓرۚ", "تِلۡكَ", "ءَايَٰتُ", "ٱلۡكِتَٰبِ", "ٱلۡحَكِيمِ", "١")
    private val AYAH_76_15 = listOf("وَيُطَافُ", "عَلَيۡهِم", "بِـَٔانِيَةٖ", "مِّن", "فِضَّةٖ", "وَأَكۡوَابٖ", "كَانَتۡ", "قَوَارِيرَا۠", "١٥")

    private fun ra(words: List<String>): Map<String, List<Int>> =
        TajweedDetector.detect(words.map(::parseClusters))
            .filter { it.conceptId == RA_HEAVY || it.conceptId == RA_LIGHT }
            .groupBy({ it.conceptId }, { it.wordIndex })
            .mapValues { (_, indexes) -> indexes.distinct().sorted() }

    @Test
    fun theRasOwnVowelDecides() {
        assertEquals("رَبِّ", mapOf(RA_HEAVY to listOf(2)), ra(AYAH_1_2))
        assertEquals("غَيۡرِ is light", listOf(4), ra(AYAH_1_7)[RA_LIGHT])
    }

    @Test
    fun withASukunTheVowelBeforeDecides() {
        assertEquals("وَٱسۡتَغۡفِرۡهُ, after a kasra", listOf(3), ra(AYAH_110_3)[RA_LIGHT])
        assertEquals("ٱرۡكَبُواْ, after hamzat al-waṣl", 1, ra(AYAH_11_41).getValue(RA_HEAVY).first())
        assertEquals("قِرۡطَاسٖ: a heavy letter after it", true, 5 in ra(AYAH_6_7).getValue(RA_HEAVY))
    }

    @Test
    fun stoppingAtTheEndOfTheAyah() {
        assertEquals("ٱلۡقَدۡرِ: al-qadr, heavy", mapOf(RA_HEAVY to listOf(4)), ra(AYAH_97_1))
        assertEquals("حِجۡرٍ: after a kasra", mapOf(RA_LIGHT to listOf(5)), ra(AYAH_89_5))
        assertEquals("مُّسۡتَمِرّٞ", true, 6 in ra(AYAH_54_2).getValue(RA_LIGHT))
        // ٱلرِّيحَ and ٱلۡقِطۡرِۖ have a kasra (mid-ayah); ٱلسَّعِيرِ ends it, light after a yāʾ.
        assertEquals(listOf(1, 9, 22, 26), ra(AYAH_34_12)[RA_LIGHT])
        // قَوَارِيرَا۠: the first rāʾ has a kasra, the second a fatḥa.
        assertEquals(listOf(7), ra(AYAH_76_15)[RA_LIGHT])
        assertEquals(true, 7 in ra(AYAH_76_15).getValue(RA_HEAVY))
    }

    @Test
    fun nothingWhereEitherIsAllowedOrThereIsNoRule() {
        assertEquals("فِرۡقٖ may be either", false, 10 in (ra(AYAH_26_63).values.flatten()))
        assertEquals("وَنُذُرِ when stopping", emptyMap<String, List<Int>>(), ra(AYAH_54_16).filterValues { 3 in it })
        assertEquals("مَجۡر۪ىٰهَا", false, 5 in ra(AYAH_11_41).values.flatten())
        assertEquals("وَٱذۡكُر merged into رَّبَّكَ", false, 14 in ra(AYAH_3_41).values.flatten())
        assertEquals("الٓرۚ is a letter name", false, 0 in ra(AYAH_10_1).values.flatten())
    }
}
