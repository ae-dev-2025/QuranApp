package com.quranapp.android.learning.words

import com.quranapp.android.learning.analysis.TestAyahs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordFormsTest {
    @Test
    fun theSkeletonIsTheLettersAlone() {
        assertEquals("بسم", WordForms.skeleton(TestAyahs.AYAH_1_1[0]))
        assertEquals("الله", WordForms.skeleton(TestAyahs.AYAH_1_1[1])) // ٱ counts as ا
    }

    @Test
    fun sameSkeleton_ignoresMarksButNotLetters() {
        assertTrue(WordForms.sameSkeleton(TestAyahs.AYAH_112_1[0], "قُل")) // قُلۡ, whatever its marks
        assertFalse(WordForms.sameSkeleton("يَقُولُ", "قَالَ"))
        assertFalse("the article is a letter too", WordForms.sameSkeleton(TestAyahs.AYAH_1_1[1], "لله"))
    }
}
