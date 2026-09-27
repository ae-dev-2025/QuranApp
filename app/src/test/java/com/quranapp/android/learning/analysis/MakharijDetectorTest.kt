package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.concepts.ConceptIds
import com.quranapp.android.learning.letters.Letters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MakharijDetectorTest {
    private fun placesOf(ayah: List<String>, word: Int): Set<String> =
        AyahAnalyzer.analyze(ayah).wordsByConcept.filter { (id, words) -> id.startsWith("tajweed.makhraj_") && word in words }.keys

    @Test
    fun theLettersOfAWordGiveItsPlaces() {
        // بِسۡمِ: ب and م are lip letters, س a tongue letter.
        assertEquals(setOf(ConceptIds.MAKHRAJ_LIPS, ConceptIds.MAKHRAJ_TONGUE), placesOf(TestAyahs.AYAH_1_1, 0))
        // قُلۡ هُوَ: ق ل tongue; ه throat, and و with a fatḥa is a consonant on the lips.
        assertEquals(setOf(ConceptIds.MAKHRAJ_TONGUE), placesOf(TestAyahs.AYAH_112_1, 0))
        assertEquals(setOf(ConceptIds.MAKHRAJ_THROAT, ConceptIds.MAKHRAJ_LIPS), placesOf(TestAyahs.AYAH_112_1, 1))
    }

    @Test
    fun longVowelsComeFromTheEmptySpaceAndGhunnahFromTheNose() {
        // ٱلرَّحِيمِ has a long ī; ٱلرَّحۡمَٰنِ a dagger alif.
        assertTrue(ConceptIds.MAKHRAJ_JAWF in placesOf(TestAyahs.AYAH_1_1, 3))
        assertTrue(ConceptIds.MAKHRAJ_JAWF in placesOf(TestAyahs.AYAH_1_1, 2))
        // لِلَّهِ has no long vowel; ٱللَّهِ's lām has a shadda but isn't noon or meem: no ghunnah.
        assertFalse(ConceptIds.MAKHRAJ_NOSE in placesOf(TestAyahs.AYAH_1_1, 1))
    }

    @Test
    fun aSilentLetterIsNotSaidSoItHasNoPlace() {
        // كَفَرُواْ (2:6): the last alif is silent; ر و ف ك are all said.
        val word = TestAyahs.AYAH_2_6[2]
        val clusters = parseClusters(word)
        assertTrue(clusters.any { it.hasAny(Arabic.SILENT_MARKS) })
        assertEquals(setOf(ConceptIds.MAKHRAJ_TONGUE, ConceptIds.MAKHRAJ_LIPS), MakharijDetector.detect(clusters, emptySet()))
    }

    @Test
    fun everyLetterHasAPlaceConcept() {
        Letters.all.forEach { letter ->
            assertTrue(letter.name, letter.place.conceptId.startsWith("tajweed.makhraj_"))
        }
    }
}
