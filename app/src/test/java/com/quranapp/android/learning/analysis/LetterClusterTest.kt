package com.quranapp.android.learning.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LetterClusterTest {
    @Test
    fun `splits a word into letters with their marks`() {
        val clusters = parseClusters("بِسۡمِ")

        assertEquals(listOf('ب', 'س', 'م'), clusters.map { it.letter })
        assertTrue(clusters[0].has(Arabic.KASRA))
        assertTrue(clusters[1].has(Arabic.SUKUN))
        assertTrue(clusters[2].has(Arabic.KASRA))
    }

    @Test
    fun `a letter can carry several marks`() {
        // The ل of لِّلۡمُتَّقِينَ has both a shadda and a kasra.
        val lam = parseClusters("لِّلۡمُتَّقِينَ").first()

        assertTrue(lam.has(Arabic.SHADDA))
        assertTrue(lam.has(Arabic.KASRA))
    }

    @Test
    fun `ignores the hizb marker before a word`() {
        val clusters = parseClusters("۞ إِنَّ")

        assertEquals(listOf('إ', 'ن'), clusters.map { it.letter })
    }

    @Test
    fun `the stretching line is kept as a seat for marks`() {
        // In أَنۢبِـُٔونِي the hamza and damma sit on a tatweel (ـ).
        val tatweel = parseClusters("أَنۢبِـُٔونِي").single { it.letter == Arabic.TATWEEL }

        assertTrue(tatweel.has(Arabic.HAMZA_ABOVE))
        assertTrue(tatweel.has(Arabic.DAMMA))
    }

    @Test
    fun `a letter without vowel, sukun or shadda is bare`() {
        // ٱللَّهِ = ٱ + ل + لَّ + هِ. The first lām has no mark at all (it is not pronounced).
        val clusters = parseClusters("ٱللَّهِ")

        assertEquals(4, clusters.size)
        assertTrue(clusters[1].isBare)
        assertFalse(clusters[2].isBare)
    }

    @Test
    fun `the ayah number is not a word`() {
        assertTrue(parseClusters("٢").isEmpty())
    }
}
