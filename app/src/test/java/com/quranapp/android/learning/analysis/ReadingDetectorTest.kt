package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds.DAGGER_ALIF
import com.quranapp.android.learning.concepts.ConceptIds.HAMZA
import com.quranapp.android.learning.concepts.ConceptIds.HAMZAT_WASL
import com.quranapp.android.learning.concepts.ConceptIds.LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.LONG_VOWELS
import com.quranapp.android.learning.concepts.ConceptIds.SAJDAH
import com.quranapp.android.learning.concepts.ConceptIds.SHADDA
import com.quranapp.android.learning.concepts.ConceptIds.SHORT_VOWELS
import com.quranapp.android.learning.concepts.ConceptIds.SILENT_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.SMALL_MADD_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.STOP_SIGNS
import com.quranapp.android.learning.concepts.ConceptIds.SUKUN
import com.quranapp.android.learning.concepts.ConceptIds.TANWEEN
import com.quranapp.android.learning.concepts.ConceptIds.TA_MARBUTA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** All words are copied from the app's Uthmani text, with the ayah they come from. */
class ReadingDetectorTest {
    private fun detect(word: String) = ReadingDetector.detect(word)

    @Test
    fun `bismi - vowels and sukun (1 1)`() {
        assertEquals(setOf(LETTERS, SHORT_VOWELS, SUKUN), detect("بِسۡمِ"))
    }

    @Test
    fun `allahi - hamzat al-wasl and shadda (1 1)`() {
        assertEquals(setOf(LETTERS, SHORT_VOWELS, SHADDA, HAMZAT_WASL), detect("ٱللَّهِ"))
    }

    @Test
    fun `ar-rahmani - dagger alif (1 1)`() {
        assertEquals(
            setOf(LETTERS, SHORT_VOWELS, SUKUN, SHADDA, HAMZAT_WASL, DAGGER_ALIF),
            detect("ٱلرَّحۡمَٰنِ"),
        )
    }

    @Test
    fun `kafaru - long vowel and silent alif (2 6)`() {
        val found = detect("كَفَرُواْ")

        assertTrue(LONG_VOWELS in found)
        assertTrue(SILENT_LETTERS in found)
    }

    @Test
    fun `ulaika - a silent waw is not a long vowel (2 5)`() {
        val found = detect("أُوْلَٰٓئِكَ")

        assertTrue(SILENT_LETTERS in found)
        assertTrue(HAMZA in found)
        assertFalse(LONG_VOWELS in found)
    }

    @Test
    fun `hudan - the alif maqsura after tanween is not a long vowel (2 2)`() {
        val found = detect("هُدٗى")

        assertTrue(TANWEEN in found)
        assertFalse(LONG_VOWELS in found)
    }

    @Test
    fun `muhitun - tanween written as a vowel plus small meem (2 19)`() {
        assertTrue(TANWEEN in detect("مُحِيطُۢ"))
    }

    @Test
    fun `anbiyaa - a small meem on a noon is not tanween (2 91)`() {
        assertFalse(TANWEEN in detect("أَنۢبِيَآءَ"))
    }

    @Test
    fun `baudatan - ta marbuta with tanween (2 26)`() {
        val found = detect("بَعُوضَةٗ")

        assertTrue(TA_MARBUTA in found)
        assertTrue(TANWEEN in found)
    }

    @Test
    fun `bihi - small yaa (2 26)`() {
        assertTrue(SMALL_MADD_LETTERS in detect("بِهِۦ"))
    }

    @Test
    fun `ana - letter silent when continuing (20 14)`() {
        assertTrue(SILENT_LETTERS in detect("أَنَا۠"))
    }

    @Test
    fun `raiba - stop sign (2 2)`() {
        assertTrue(STOP_SIGNS in detect("رَيۡبَۛ"))
    }

    @Test
    fun `yasjuduna - prostration sign (7 206)`() {
        assertTrue(SAJDAH in detect("يَسۡجُدُونَۤ۩"))
    }

    @Test
    fun `iyyaka - long vowel alif after fatha (1 5)`() {
        assertTrue(LONG_VOWELS in detect("إِيَّاكَ"))
    }

    @Test
    fun `the ayah number has no concepts`() {
        assertTrue(detect("٢").isEmpty())
    }
}
