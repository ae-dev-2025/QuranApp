package com.quranapp.android.learning.pack

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query

/** Where a word is: its ayah and its position in the ayah, counting from 0. */
data class WordLocation(
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "word_index")
    val wordIndex: Int,
)

/** Read-only queries on learning_pack.db. The pack is never written to by the app. */
@Dao
interface LearningPackDao {
    @Query("SELECT `value` FROM meta WHERE `key` = :key")
    suspend fun meta(key: String): String?

    @Query("SELECT * FROM credits ORDER BY credit_id")
    suspend fun credits(): List<PackCreditEntity>

    @Query("SELECT * FROM segments WHERE ayah_id = :ayahId ORDER BY word_index, segment_index")
    suspend fun segmentsOfAyah(ayahId: Int): List<SegmentEntity>

    @Query("SELECT * FROM syntax WHERE ayah_id = :ayahId ORDER BY word_index, segment_index")
    suspend fun syntaxOfAyah(ayahId: Int): List<SyntaxEntity>

    @Query("SELECT * FROM word_glosses WHERE ayah_id = :ayahId ORDER BY word_index")
    suspend fun glossesOfAyah(ayahId: Int): List<WordGlossEntity>

    @Query("SELECT * FROM lemmas WHERE lemma_id IN (:lemmaIds)")
    suspend fun lemmas(lemmaIds: List<Int>): List<LemmaEntity>

    @Query("SELECT * FROM lemmas WHERE lemma_key = :lemmaKey")
    suspend fun lemmaByKey(lemmaKey: String): LemmaEntity?

    @Query("SELECT * FROM roots WHERE root_id IN (:rootIds)")
    suspend fun roots(rootIds: List<Int>): List<RootEntity>

    @Query("SELECT * FROM roots WHERE root_key = :rootKey")
    suspend fun rootByKey(rootKey: String): RootEntity?

    /** The lemmas of a root, most frequent first. */
    @Query("SELECT * FROM lemmas WHERE root_id = :rootId ORDER BY occurrences DESC, lemma_id")
    suspend fun lemmasOfRoot(rootId: Int): List<LemmaEntity>

    /** Where a lemma occurs, in Quran order. */
    @Query(
        "SELECT DISTINCT ayah_id, word_index FROM segments WHERE lemma_id = :lemmaId " +
            "ORDER BY ayah_id, word_index LIMIT :limit",
    )
    suspend fun occurrencesOfLemma(lemmaId: Int, limit: Int): List<WordLocation>
}
