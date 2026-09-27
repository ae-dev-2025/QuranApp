package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds.MADD_ARID
import com.quranapp.android.learning.concepts.ConceptIds.MADD_BADAL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_IWAD
import com.quranapp.android.learning.concepts.ConceptIds.MADD_LEEN
import com.quranapp.android.learning.concepts.ConceptIds.MADD_SILA
import com.quranapp.android.learning.concepts.ConceptIds.STOPPING
import org.junit.Assert.assertEquals
import org.junit.Test

/** Stopping and the madds of stage 3, on ayahs copied from the app's text (the ayah number is the last "word"). */
class StopAndMaddDetectorTest {
    private val AYAH_1_5 = listOf("إِيَّاكَ", "نَعۡبُدُ", "وَإِيَّاكَ", "نَسۡتَعِينُ", "٥")
    private val AYAH_106_3 = listOf("فَلۡيَعۡبُدُواْ", "رَبَّ", "هَٰذَا", "ٱلۡبَيۡتِ", "٣")
    private val AYAH_110_2 = listOf("وَرَأَيۡتَ", "ٱلنَّاسَ", "يَدۡخُلُونَ", "فِي", "دِينِ", "ٱللَّهِ", "أَفۡوَاجٗا", "٢")
    private val AYAH_104_3 = listOf("يَحۡسَبُ", "أَنَّ", "مَالَهُۥٓ", "أَخۡلَدَهُۥ", "٣")
    private val AYAH_103_3 = listOf("إِلَّا", "ٱلَّذِينَ", "ءَامَنُواْ", "وَعَمِلُواْ", "ٱلصَّٰلِحَٰتِ", "وَتَوَاصَوۡاْ", "بِٱلۡحَقِّ", "وَتَوَاصَوۡاْ", "بِٱلصَّبۡرِ", "٣")
    private val AYAH_2_5 = listOf("أُوْلَٰٓئِكَ", "عَلَىٰ", "هُدٗى", "مِّن", "رَّبِّهِمۡۖ", "وَأُوْلَٰٓئِكَ", "هُمُ", "ٱلۡمُفۡلِحُونَ", "٥")
    private val AYAH_97_5 = listOf("سَلَٰمٌ", "هِيَ", "حَتَّىٰ", "مَطۡلَعِ", "ٱلۡفَجۡرِ", "٥")
    private val AYAH_87_1 = listOf("سَبِّحِ", "ٱسۡمَ", "رَبِّكَ", "ٱلۡأَعۡلَى", "١")
    private val AYAH_112_4 = listOf("وَلَمۡ", "يَكُن", "لَّهُۥ", "كُفُوًا", "أَحَدُۢ", "٤")
    private val AYAH_74_3 = listOf("وَرَبَّكَ", "فَكَبِّرۡ", "٣")

    private fun found(words: List<String>): Map<String, List<Int>> =
        StopAndMaddDetector.detect(words.map(::parseClusters))
            .groupBy({ it.conceptId }, { it.wordIndex })
            .mapValues { (_, indexes) -> indexes.distinct().sorted() }

    @Test
    fun stoppingAfterALongVowelOrAGlide() {
        assertEquals("نَسۡتَعِينُ", mapOf(STOPPING to listOf(3), MADD_ARID to listOf(3)), found(AYAH_1_5))
        assertEquals("ٱلۡبَيۡتِ", mapOf(STOPPING to listOf(3), MADD_LEEN to listOf(3)), found(AYAH_106_3))
    }

    @Test
    fun stoppingOnTanwinFathAndOnAPlainVowel() {
        assertEquals("أَفۡوَاجٗا", listOf(6), found(AYAH_110_2)[MADD_IWAD])
        assertEquals("ٱلۡفَجۡرِ: the vowel drops, nothing to stretch", mapOf(STOPPING to listOf(4)), found(AYAH_97_5))
        // ٱلۡأَعۡلَى ends in a long vowel and فَكَبِّرۡ in a sukūn: stopping changes nothing.
        assertEquals(emptyMap<String, List<Int>>(), found(AYAH_87_1))
        assertEquals(emptyMap<String, List<Int>>(), found(AYAH_74_3))
    }

    @Test
    fun badalButNotASilentWaw() {
        assertEquals("ءَامَنُواْ", listOf(2), found(AYAH_103_3)[MADD_BADAL])
        // أُوْلَٰٓئِكَ: the wāw after the hamza is silent.
        assertEquals(null, found(AYAH_2_5)[MADD_BADAL])
    }

    @Test
    fun silaExceptWhenStoppingOnIt() {
        assertEquals("لَّهُۥ", listOf(2), found(AYAH_112_4)[MADD_SILA])
        // مَالَهُۥٓ has a big ṣila; أَخۡلَدَهُۥ ends the ayah, so its ṣila drops.
        assertEquals(listOf(2), found(AYAH_104_3)[MADD_SILA])
    }
}
