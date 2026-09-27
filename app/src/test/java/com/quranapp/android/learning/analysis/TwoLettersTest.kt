package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_MITHLAYN
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_MUTAJANISAYN
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_MUTAQARIBAYN
import com.quranapp.android.learning.concepts.ConceptIds.LAM_SAKINAH
import com.quranapp.android.learning.concepts.ConceptIds.TANWEEN_BEFORE_WASL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Two letters that meet, lām sākinah and tanwīn before ٱ, on ayahs copied from the app's text. */
class TwoLettersTest {
    private val AYAH_26_63 = listOf("فَأَوۡحَيۡنَآ", "إِلَىٰ", "مُوسَىٰٓ", "أَنِ", "ٱضۡرِب", "بِّعَصَاكَ", "ٱلۡبَحۡرَۖ", "فَٱنفَلَقَ", "فَكَانَ", "كُلُّ", "فِرۡقٖ", "كَٱلطَّوۡدِ", "ٱلۡعَظِيمِ", "٦٣")
    private val AYAH_2_256 = listOf("لَآ", "إِكۡرَاهَ", "فِي", "ٱلدِّينِۖ", "قَد", "تَّبَيَّنَ", "ٱلرُّشۡدُ", "مِنَ", "ٱلۡغَيِّۚ", "فَمَن", "يَكۡفُرۡ", "بِٱلطَّٰغُوتِ", "وَيُؤۡمِنۢ", "بِٱللَّهِ", "فَقَدِ", "ٱسۡتَمۡسَكَ", "بِٱلۡعُرۡوَةِ", "ٱلۡوُثۡقَىٰ", "لَا", "ٱنفِصَامَ", "لَهَاۗ", "وَٱللَّهُ", "سَمِيعٌ", "عَلِيمٌ", "٢٥٦")
    private val AYAH_4_158 = listOf("بَل", "رَّفَعَهُ", "ٱللَّهُ", "إِلَيۡهِۚ", "وَكَانَ", "ٱللَّهُ", "عَزِيزًا", "حَكِيمٗا", "١٥٨")
    private val AYAH_112_1 = listOf("قُلۡ", "هُوَ", "ٱللَّهُ", "أَحَدٌ", "١")
    private val AYAH_2_180 = listOf("كُتِبَ", "عَلَيۡكُمۡ", "إِذَا", "حَضَرَ", "أَحَدَكُمُ", "ٱلۡمَوۡتُ", "إِن", "تَرَكَ", "خَيۡرًا", "ٱلۡوَصِيَّةُ", "لِلۡوَٰلِدَيۡنِ", "وَٱلۡأَقۡرَبِينَ", "بِٱلۡمَعۡرُوفِۖ", "حَقًّا", "عَلَى", "ٱلۡمُتَّقِينَ", "١٨٠")
    private val AYAH_77_20 = listOf("أَلَمۡ", "نَخۡلُقكُّم", "مِّن", "مَّآءٖ", "مَّهِينٖ", "٢٠")
    private val AYAH_5_28 = listOf("لَئِنۢ", "بَسَطتَ", "إِلَيَّ", "يَدَكَ", "لِتَقۡتُلَنِي", "مَآ", "أَنَا۠", "بِبَاسِطٖ", "يَدِيَ", "إِلَيۡكَ", "لِأَقۡتُلَكَۖ", "إِنِّيٓ", "أَخَافُ", "ٱللَّهَ", "رَبَّ", "ٱلۡعَٰلَمِينَ", "٢٨")
    private val AYAH_2_61 = listOf("وَإِذۡ", "قُلۡتُمۡ", "يَٰمُوسَىٰ", "لَن", "نَّصۡبِرَ", "عَلَىٰ", "طَعَامٖ", "وَٰحِدٖ", "فَٱدۡعُ", "لَنَا", "رَبَّكَ", "يُخۡرِجۡ", "لَنَا", "مِمَّا", "تُنۢبِتُ", "ٱلۡأَرۡضُ", "مِنۢ", "بَقۡلِهَا", "وَقِثَّآئِهَا", "وَفُومِهَا", "وَعَدَسِهَا", "وَبَصَلِهَاۖ", "قَالَ", "أَتَسۡتَبۡدِلُونَ", "ٱلَّذِي", "هُوَ", "أَدۡنَىٰ", "بِٱلَّذِي", "هُوَ", "خَيۡرٌۚ", "ٱهۡبِطُواْ", "مِصۡرٗا", "فَإِنَّ", "لَكُم", "مَّا", "سَأَلۡتُمۡۗ", "وَضُرِبَتۡ", "عَلَيۡهِمُ", "ٱلذِّلَّةُ", "وَٱلۡمَسۡكَنَةُ", "وَبَآءُو", "بِغَضَبٖ", "مِّنَ", "ٱللَّهِۚ", "ذَٰلِكَ", "بِأَنَّهُمۡ", "كَانُواْ", "يَكۡفُرُونَ", "بِـَٔايَٰتِ", "ٱللَّهِ", "وَيَقۡتُلُونَ", "ٱلنَّبِيِّـۧنَ", "بِغَيۡرِ", "ٱلۡحَقِّۚ", "ذَٰلِكَ", "بِمَا", "عَصَواْ", "وَّكَانُواْ", "يَعۡتَدُونَ", "٦١")
    private val AYAH_2_33 = listOf("قَالَ", "يَٰٓـَٔادَمُ", "أَنۢبِئۡهُم", "بِأَسۡمَآئِهِمۡۖ", "فَلَمَّآ", "أَنۢبَأَهُم", "بِأَسۡمَآئِهِمۡ", "قَالَ", "أَلَمۡ", "أَقُل", "لَّكُمۡ", "إِنِّيٓ", "أَعۡلَمُ", "غَيۡبَ", "ٱلسَّمَٰوَٰتِ", "وَٱلۡأَرۡضِ", "وَأَعۡلَمُ", "مَا", "تُبۡدُونَ", "وَمَا", "كُنتُمۡ", "تَكۡتُمُونَ", "٣٣")
    private val AYAH_6_143 = listOf("ثَمَٰنِيَةَ", "أَزۡوَٰجٖۖ", "مِّنَ", "ٱلضَّأۡنِ", "ٱثۡنَيۡنِ", "وَمِنَ", "ٱلۡمَعۡزِ", "ٱثۡنَيۡنِۗ", "قُلۡ", "ءَآلذَّكَرَيۡنِ", "حَرَّمَ", "أَمِ", "ٱلۡأُنثَيَيۡنِ", "أَمَّا", "ٱشۡتَمَلَتۡ", "عَلَيۡهِ", "أَرۡحَامُ", "ٱلۡأُنثَيَيۡنِۖ", "نَبِّـُٔونِي", "بِعِلۡمٍ", "إِن", "كُنتُمۡ", "صَٰدِقِينَ", "١٤٣")
    private val AYAH_2_2 = listOf("ذَٰلِكَ", "ٱلۡكِتَٰبُ", "لَا", "رَيۡبَۛ", "فِيهِۛ", "هُدٗى", "لِّلۡمُتَّقِينَ", "٢")
    private val AYAH_1_2 = listOf("ٱلۡحَمۡدُ", "لِلَّهِ", "رَبِّ", "ٱلۡعَٰلَمِينَ", "٢")

    private fun found(words: List<String>): Map<String, List<Int>> =
        TajweedDetector.detect(words.map(::parseClusters))
            .groupBy({ it.conceptId }, { it.wordIndex })
            .mapValues { (_, indexes) -> indexes.distinct().sorted() }

    @Test
    fun theSameLetter() {
        assertEquals("ٱضۡرِب بِّعَصَاكَ", listOf(4, 5), found(AYAH_26_63)[IDGHAM_MITHLAYN])
        assertEquals("أَقُل لَّكُمۡ", true, found(AYAH_2_33).getValue(IDGHAM_MITHLAYN).isNotEmpty())
        // 2:61 عَصَواْ وَّكَانُواْ: a wāw after a fatḥa merges into the next word's wāw.
        assertTrue(IDGHAM_MITHLAYN in found(AYAH_2_61))
    }

    @Test
    fun lettersFromOnePlaceAndFromClosePlaces() {
        assertEquals("قَد تَّبَيَّنَ", listOf(4, 5), found(AYAH_2_256)[IDGHAM_MUTAJANISAYN])
        assertEquals("بَسَطتَ: ط before ت, merged only partly", listOf(1), found(AYAH_5_28)[IDGHAM_MUTAJANISAYN])
        assertEquals("بَل رَّفَعَهُ", listOf(0, 1), found(AYAH_4_158)[IDGHAM_MUTAQARIBAYN])
        assertEquals("نَخۡلُقكُّم", listOf(1), found(AYAH_77_20)[IDGHAM_MUTAQARIBAYN])
    }

    @Test
    fun lamSakinahOutsideTheArticle() {
        assertEquals("قُلۡ", listOf(0), found(AYAH_112_1)[LAM_SAKINAH])
        assertEquals("بَل merges into رَّفَعَهُ", listOf(0, 1), found(AYAH_4_158)[LAM_SAKINAH])
        // The lām of ال is its own rule: ٱلۡحَمۡدُ, ٱلۡعَٰلَمِينَ, and ءَآلذَّكَرَيۡنِ after a question.
        assertFalse(LAM_SAKINAH in found(AYAH_1_2))
        assertFalse(IDGHAM_MITHLAYN in found(AYAH_6_143))
    }

    @Test
    fun tanwinBeforeHamzatAlWasl() {
        assertEquals("خَيۡرًا ٱلۡوَصِيَّةُ", listOf(8, 9), found(AYAH_2_180)[TANWEEN_BEFORE_WASL])
        assertFalse(TANWEEN_BEFORE_WASL in found(AYAH_2_2))
    }
}
