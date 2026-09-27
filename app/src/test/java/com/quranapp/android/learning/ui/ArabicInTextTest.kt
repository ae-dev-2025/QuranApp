package com.quranapp.android.learning.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicInTextTest {
    private val lrm = "\u200E"

    @Test
    fun aMarkAfterEachExampleKeepsAListInOrder() {
        assertEquals("time: رَبّ$lrm, هُوَ$lrm.", arabicExamplesInOrder("time: رَبّ, هُوَ."))
        assertEquals("ـَانِ$lrm or ـَيۡنِ$lrm: جَنَّتَانِ$lrm", arabicExamplesInOrder("ـَانِ or ـَيۡنِ: جَنَّتَانِ"))
    }

    @Test
    fun aPhraseStaysOneExample() {
        assertEquals("the term جَمْع مُذَكَّر سَالِم$lrm.", arabicExamplesInOrder("the term جَمْع مُذَكَّر سَالِم."))
    }

    @Test
    fun englishIsLeftAlone() {
        assertEquals("No Arabic here.", arabicExamplesInOrder("No Arabic here."))
    }
}
