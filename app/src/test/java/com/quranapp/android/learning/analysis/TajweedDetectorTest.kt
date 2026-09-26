package com.quranapp.android.learning.analysis

import com.quranapp.android.learning.analysis.TestAyahs.AL_LAHAB
import com.quranapp.android.learning.analysis.TestAyahs.AL_LAHW
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_10_51
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_112_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_112_4
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_113_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_113_3
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_1_7
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_20_14
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_1
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_10
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_2
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_31
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_2_6
import com.quranapp.android.learning.analysis.TestAyahs.AYAH_68_1
import com.quranapp.android.learning.concepts.ConceptIds.GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.HEAVY_LETTERS
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_NO_GHUNNAH
import com.quranapp.android.learning.concepts.ConceptIds.IDGHAM_SHAFAWI
import com.quranapp.android.learning.concepts.ConceptIds.IKHFA
import com.quranapp.android.learning.concepts.ConceptIds.IKHFA_SHAFAWI
import com.quranapp.android.learning.concepts.ConceptIds.IQLAB
import com.quranapp.android.learning.concepts.ConceptIds.IZHAR
import com.quranapp.android.learning.concepts.ConceptIds.IZHAR_SHAFAWI
import com.quranapp.android.learning.concepts.ConceptIds.LAM_OF_ALLAH
import com.quranapp.android.learning.concepts.ConceptIds.LAM_QAMARIYYA
import com.quranapp.android.learning.concepts.ConceptIds.LAM_SHAMSIYYA
import com.quranapp.android.learning.concepts.ConceptIds.MADD_LAZIM
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUNFASIL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_MUTTASIL
import com.quranapp.android.learning.concepts.ConceptIds.MADD_SIGN
import com.quranapp.android.learning.concepts.ConceptIds.QALQALAH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Each test names a rule and the ayah it is checked in. `assertAt` lists the exact word
 * indexes (0-based) where the rule must be found, and nowhere else.
 */
class TajweedDetectorTest {
    private fun detect(words: List<String>): Map<String, List<Int>> =
        TajweedDetector.detect(words.map(::parseClusters))
            .groupBy({ it.conceptId }, { it.wordIndex })
            .mapValues { (_, indexes) -> indexes.distinct().sorted() }

    private fun assertAt(words: List<String>, conceptId: String, vararg wordIndexes: Int) {
        assertEquals(conceptId, wordIndexes.toList(), detect(words)[conceptId].orEmpty())
    }

    private fun assertAbsent(words: List<String>, conceptId: String) {
        assertFalse("$conceptId should not be found", conceptId in detect(words))
    }

    // ---- Noon sakinah and tanween ----

    @Test
    fun `izhar - noon with sukun and stacked tanween`() {
        assertAt(AYAH_1_7, IZHAR, 2) // أَنۡعَمۡتَ
        assertAt(AYAH_2_10, IZHAR, 7) // عَذَابٌ أَلِيمُۢ
        assertAt(AYAH_112_4, IZHAR, 3) // كُفُوًا أَحَدُۢ
    }

    @Test
    fun `idgham with ghunnah - staggered tanween before waw`() {
        assertAt(AYAH_2_10, IDGHAM_GHUNNAH, 5) // مَرَضٗاۖ وَلَهُمۡ
    }

    @Test
    fun `idgham without ghunnah - before lam and ra`() {
        assertAt(AYAH_2_2, IDGHAM_NO_GHUNNAH, 5) // هُدٗى لِّلۡمُتَّقِينَ
        assertAt(AYAH_112_4, IDGHAM_NO_GHUNNAH, 1) // يَكُن لَّهُۥ
    }

    @Test
    fun `iqlab - small meem before ba`() {
        assertAt(AYAH_2_10, IQLAB, 8) // أَلِيمُۢ بِمَا
        assertAt(AYAH_2_31, IQLAB, 9) // أَنۢبِـُٔونِي
    }

    @Test
    fun `ikhfa - bare noon and staggered tanween`() {
        assertAt(AYAH_2_6, IKHFA, 5, 8) // ءَأَنذَرۡتَهُمۡ, تُنذِرۡهُمۡ
        assertAt(AYAH_2_10, IKHFA, 2) // مَّرَضٞ فَزَادَهُمُ
        assertAt(AYAH_2_31, IKHFA, 12, 13) // إِن كُنتُمۡ
        assertAt(AYAH_113_3, IKHFA, 0) // وَمِن شَرِّ
    }

    @Test
    fun `no noon rule when stopping at the end of the ayah`() {
        assertAbsent(AYAH_112_1, IZHAR) // أَحَدٌ
        assertAbsent(AYAH_112_4, IQLAB) // أَحَدُۢ
    }

    @Test
    fun `a noon with a madd sign is a letter name, not noon sakinah (68 1)`() {
        assertAbsent(AYAH_68_1, IDGHAM_GHUNNAH)
        assertAbsent(AYAH_68_1, IZHAR)
    }

    // ---- Meem sakinah ----

    @Test
    fun `izhar shafawi - meem with sukun`() {
        assertAt(AYAH_1_7, IZHAR_SHAFAWI, 2, 3, 6)
        assertAt(AYAH_2_6, IZHAR_SHAFAWI, 4, 5, 6, 7, 8)
    }

    @Test
    fun `idgham shafawi - meem before meem`() {
        assertAt(AYAH_2_10, IDGHAM_SHAFAWI, 1) // قُلُوبِهِم مَّرَضٞ
    }

    @Test
    fun `ikhfa shafawi - meem before ba`() {
        assertAt(AYAH_10_51, IKHFA_SHAFAWI, 4, 8) // ءَامَنتُم بِهِۦٓ, كُنتُم بِهِۦ
    }

    // ---- Lam ----

    @Test
    fun `lam shamsiyya and qamariyya`() {
        assertAt(AYAH_1_1, LAM_SHAMSIYYA, 1, 2, 3)
        assertAt(AYAH_1_7, LAM_SHAMSIYYA, 8) // not ٱلَّذِينَ: only one lām is written
        assertAt(AYAH_1_7, LAM_QAMARIYYA, 5)
        assertAt(AYAH_2_2, LAM_QAMARIYYA, 1, 6) // ٱلۡكِتَٰبُ, لِّلۡمُتَّقِينَ
    }

    @Test
    fun `lam of Allah`() {
        assertAt(AYAH_1_1, LAM_OF_ALLAH, 1)
        assertAt(AYAH_2_10, LAM_OF_ALLAH, 4)
    }

    @Test
    fun `al-lahw and al-lahab are not the name Allah (62 11, 77 31)`() {
        assertAbsent(listOf(AL_LAHW), LAM_OF_ALLAH)
        assertAbsent(listOf(AL_LAHAB), LAM_OF_ALLAH)
        assertAt(listOf(AL_LAHW), LAM_SHAMSIYYA, 0)
    }

    // ---- Other letter rules ----

    @Test
    fun `ghunnah - noon or meem with shadda`() {
        assertAt(AYAH_2_6, GHUNNAH, 0) // إِنَّ
        assertAt(AYAH_2_10, GHUNNAH, 2) // مَّرَضٞ
        assertAt(AYAH_2_31, GHUNNAH, 4) // ثُمَّ
    }

    @Test
    fun `qalqalah - with sukun, or at the end of the ayah`() {
        assertAt(AYAH_10_51, QALQALAH, 7) // وَقَدۡ
        assertAt(AYAH_20_14, QALQALAH, 7) // فَٱعۡبُدۡنِي
        assertAt(AYAH_112_1, QALQALAH, 3) // أَحَدٌ (stop)
        assertAt(AYAH_113_1, QALQALAH, 3) // ٱلۡفَلَقِ (stop)
        assertAbsent(AYAH_1_7, QALQALAH)
    }

    @Test
    fun `heavy letters`() {
        assertAt(AYAH_1_7, HEAVY_LETTERS, 0, 4, 5, 8)
        assertAt(AYAH_113_3, HEAVY_LETTERS, 2, 4) // غَاسِقٍ, وَقَبَ
    }

    // ---- Madd ----

    @Test
    fun `madd muttasil - hamza after the madd in the same word`() {
        assertAt(AYAH_2_6, MADD_MUTTASIL, 3) // سَوَآءٌ
        assertAt(AYAH_2_31, MADD_MUTTASIL, 2, 7, 10, 11)
    }

    @Test
    fun `madd munfasil - hamza at the start of the next word`() {
        assertAt(AYAH_20_14, MADD_MUNFASIL, 0, 3, 5) // إِنَّنِيٓ أَنَا, لَآ إِلَٰهَ, إِلَّآ أَنَا۠
        assertAt(AYAH_10_51, MADD_MUNFASIL, 5) // بِهِۦٓۚ ءَآلۡـَٰٔنَ
    }

    @Test
    fun `madd munfasil - vocative written joined to the next word`() {
        assertAt(AYAH_2_31, MADD_MUNFASIL, 11) // هَٰٓؤُلَآءِ
    }

    @Test
    fun `madd lazim - shadda or sukun after the madd`() {
        assertAt(AYAH_1_7, MADD_LAZIM, 8) // ٱلضَّآلِّينَ
        assertAt(AYAH_10_51, MADD_LAZIM, 6) // ءَآلۡـَٰٔنَ
    }

    @Test
    fun `an unclassified madd sign is still reported (2 1)`() {
        assertAt(AYAH_2_1, MADD_SIGN, 0) // الٓمٓ
        assertAbsent(AYAH_2_1, MADD_LAZIM)
        assertAbsent(AYAH_2_1, MADD_MUTTASIL)
        assertAbsent(AYAH_2_1, MADD_MUNFASIL)
    }
}
