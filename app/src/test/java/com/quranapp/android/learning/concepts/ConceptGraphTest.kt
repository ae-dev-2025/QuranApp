package com.quranapp.android.learning.concepts

import com.quranapp.android.learning.concepts.ConceptIds.HAMZA
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_NO_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IKHFA
import com.quranapp.android.learning.concepts.ConceptIds.IQLAB
import com.quranapp.android.learning.concepts.ConceptIds.IZHAR
import com.quranapp.android.learning.concepts.ConceptIds.LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.MADD_LAZIM
import com.quranapp.android.learning.concepts.ConceptIds.MAKHRAJ_LIPS
import com.quranapp.android.learning.concepts.ConceptIds.MAKHRAJ_NOSE
import com.quranapp.android.learning.concepts.ConceptIds.MAKHRAJ_THROAT
import com.quranapp.android.learning.concepts.ConceptIds.MAKHRAJ_TONGUE
import com.quranapp.android.learning.concepts.ConceptIds.NOON_SAKINAH
import com.quranapp.android.learning.concepts.ConceptIds.SAJDAH
import com.quranapp.android.learning.concepts.ConceptIds.SHADDA
import com.quranapp.android.learning.concepts.ConceptIds.SHORT_VOWELS
import com.quranapp.android.learning.concepts.ConceptIds.SUKUN
import com.quranapp.android.learning.concepts.ConceptIds.TANWEEN
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConceptGraphTest {
    private val graph = ConceptGraph()

    @Test
    fun `withPrerequisites follows the chain down to the first concept`() {
        // ikhfa -> noon sakinah -> sukun + tanween -> short vowels -> letters
        assertEquals(
            setOf(IKHFA, NOON_SAKINAH, SUKUN, TANWEEN, SHORT_VOWELS, LETTERS),
            graph.withPrerequisites(listOf(IKHFA)),
        )
    }

    @Test
    fun `withPrerequisites ignores unknown IDs`() {
        assertEquals(setOf(LETTERS), graph.withPrerequisites(listOf(LETTERS, "no.such.concept")))
    }

    @Test
    fun `inLearningOrder puts prerequisites first`() {
        val ordered = graph.inLearningOrder(listOf(IKHFA, SHADDA, LETTERS, SUKUN))

        assertEquals(listOf(LETTERS, SUKUN, SHADDA, IKHFA), ordered.map { it.id })
    }

    @Test
    fun `a complete beginner can only start with the letters`() {
        assertEquals(listOf(LETTERS), graph.readyToLearn(known = emptySet()).map { it.id })
    }

    @Test
    fun `knowing the letters unlocks the next concepts`() {
        // Where letters are made (except the empty space, which needs long vowels) comes with the letters.
        assertEquals(
            listOf(SHORT_VOWELS, SAJDAH, MAKHRAJ_THROAT, MAKHRAJ_TONGUE, MAKHRAJ_LIPS, MAKHRAJ_NOSE),
            graph.readyToLearn(known = setOf(LETTERS)).map { it.id },
        )
    }

    @Test
    fun `prerequisitesOf lists direct prerequisites in learning order`() {
        assertEquals(listOf(SUKUN, TANWEEN), graph.prerequisitesOf(NOON_SAKINAH).map { it.id })
        assertEquals(emptyList<String>(), graph.prerequisitesOf(LETTERS).map { it.id })
    }

    @Test
    fun `dependentsOf lists what a concept unlocks`() {
        assertEquals(
            listOf(IZHAR, IDGHAM_GHUNNAH, IDGHAM_NO_GHUNNAH, IQLAB, IKHFA),
            graph.dependentsOf(NOON_SAKINAH).map { it.id },
        )
        assertEquals(listOf(ConceptIds.MADD_MUQATTAAT), graph.dependentsOf(MADD_LAZIM).map { it.id })
        assertEquals(emptyList<String>(), graph.dependentsOf(ConceptIds.MADD_MUQATTAAT).map { it.id })
    }

    @Test
    fun `known concepts are not suggested again`() {
        val known = setOf(LETTERS, SHORT_VOWELS)
        val ready = graph.readyToLearn(known).map { it.id }

        assertFalse(LETTERS in ready || SHORT_VOWELS in ready)
        assertTrue(SUKUN in ready && HAMZA in ready)
    }
}
