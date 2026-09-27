package com.quranapp.android.learning.letters

import com.quranapp.android.learning.analysis.TestAyahs
import com.quranapp.android.learning.examples.AyahWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LettersTest {
    @Test
    fun twentyEightLettersAndHamzaInSevenGroups() {
        assertEquals(29, Letters.all.size)
        assertEquals(29, Letters.all.map { it.id }.toSet().size)
        assertEquals(29, Letters.all.map { it.char }.toSet().size)
        assertEquals((1..7).toList(), Letters.groups.keys.sorted())
        // The design's groups: 6, 3, 5, 4, 4, 5 and 2 (ة is taught as its own reading concept).
        assertEquals(listOf(6, 3, 5, 4, 4, 5, 2), (1..7).map { Letters.groups.getValue(it).size })
    }

    @Test
    fun theArabicNamesUseTheAppFontsSukun() {
        // In the app's Quran font U+0652 draws the "silent letter" circle; U+06E1 is the sukun.
        Letters.all.forEach { assertFalse(it.arabicName, it.arabicName.contains('ْ')) }
    }

    @Test
    fun shapesJoinOnlyWhereTheLetterCan() {
        val ba = Letters.shapesOf(Letters["letter.ba"]!!)
        assertEquals("ب‍", ba.start)
        assertEquals("‍ب‍", ba.middle)
        assertEquals("‍ب", ba.end)
        val dal = Letters.shapesOf(Letters["letter.dal"]!!)
        assertEquals("د", dal.start) // never joins the next letter
        assertEquals("‍د", dal.middle)
        val hamza = Letters.shapesOf(Letters["letter.hamza"]!!)
        assertEquals(setOf("ء"), setOf(hamza.alone, hamza.start, hamza.middle, hamza.end))
    }
}

class LetterFinderTest {
    private fun ids(vararg names: String) = names.map { "letter.$it" }.toSet()

    @Test
    fun lettersIn_readsTheSpelling() {
        assertEquals(ids("ba", "sin", "mim"), LetterFinder.lettersIn("بِسۡمِ"))
        assertEquals(ids("alif", "lam", "ra", "hha", "mim", "nun"), LetterFinder.lettersIn("ٱلرَّحۡمَٰنِ"))
    }

    @Test
    fun aHamzaOnASeatIsBothLetters() {
        assertEquals(ids("alif", "hamza", "ya", "kaf"), LetterFinder.lettersIn("إِيَّاكَ"))
        assertTrue("letter.hamza" in LetterFinder.lettersIn("يُؤۡمِنُونَ")) // ؤ
    }

    @Test
    fun exampleOf_prefersAWordThatStartsWithTheLetter() {
        val ayahs = listOf(
            AyahWords(1, 1, TestAyahs.AYAH_1_1 + "١"),
            AyahWords(112, 1, TestAyahs.AYAH_112_1 + "١"),
        )
        assertEquals(LetterExample(1, 1, 0, "بِسۡمِ"), LetterFinder.exampleOf(Letters["letter.ba"]!!, ayahs))
        // ق first starts a word in قُلۡ, 112:1, though Al-Fātiḥah comes first.
        assertEquals(LetterExample(112, 1, 0, "قُلۡ"), LetterFinder.exampleOf(Letters["letter.qaf"]!!, ayahs))
        // ح starts no word here: the first word that has it.
        // (Compared with the fixture: the app's text isn't Unicode-normalised, so a retyped word can differ.)
        assertEquals(LetterExample(1, 1, 2, TestAyahs.AYAH_1_1[2]), LetterFinder.exampleOf(Letters["letter.hha"]!!, ayahs))
        assertNull(LetterFinder.exampleOf(Letters["letter.zza"]!!, ayahs))
    }
}
