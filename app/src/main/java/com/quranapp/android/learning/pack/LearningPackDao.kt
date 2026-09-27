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

/** A dictionary word inside one word of an ayah. */
data class WordLemmaKey(
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "word_index")
    val wordIndex: Int,
    @ColumnInfo(name = "lemma_key")
    val lemmaKey: String,
)

/** A segment with its dictionary word and root, for the grammar detector. */
data class GrammarSegmentRow(
    @ColumnInfo(name = "ayah_id")
    val ayahId: Int,
    @ColumnInfo(name = "word_index")
    val wordIndex: Int,
    @ColumnInfo(name = "form")
    val form: String,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "tag")
    val tag: String,
    @ColumnInfo(name = "features")
    val features: String,
    @ColumnInfo(name = "lemma_key")
    val lemmaKey: String?,
    @ColumnInfo(name = "root_key")
    val rootKey: String?,
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

    /** Dictionary words with a meaning, closest in frequency to [occurrences] first: wrong options for questions. */
    @Query(
        "SELECT * FROM lemmas WHERE gloss IS NOT NULL AND lemma_id != :excludeId " +
            "ORDER BY ABS(occurrences - :occurrences), lemma_id LIMIT :limit",
    )
    suspend fun lemmasNearFrequency(occurrences: Int, excludeId: Int, limit: Int): List<LemmaEntity>

    /** Where a lemma occurs, in Quran order. */
    @Query(
        "SELECT DISTINCT ayah_id, word_index FROM segments WHERE lemma_id = :lemmaId " +
            "ORDER BY ayah_id, word_index LIMIT :limit",
    )
    suspend fun occurrencesOfLemma(lemmaId: Int, limit: Int): List<WordLocation>

    /** Every segment of a range of ayahs with its dictionary word and root, in reading order. */
    @Query(
        "SELECT s.ayah_id, s.word_index, s.form, s.kind, s.tag, s.features, l.lemma_key, r.root_key FROM segments s " +
            "LEFT JOIN lemmas l ON l.lemma_id = s.lemma_id LEFT JOIN roots r ON r.root_id = l.root_id " +
            "WHERE s.ayah_id BETWEEN :firstAyahId AND :lastAyahId ORDER BY s.ayah_id, s.word_index, s.segment_index",
    )
    suspend fun grammarSegmentsBetween(firstAyahId: Int, lastAyahId: Int): List<GrammarSegmentRow>

    /** How many dictionary words the pack has. */
    @Query("SELECT COUNT(*) FROM lemmas")
    suspend fun lemmaCount(): Int

    /** The dictionary words of every word in a range of ayahs, in reading order. Uses the primary key. */
    @Query(
        "SELECT s.ayah_id, s.word_index, l.lemma_key FROM segments s JOIN lemmas l ON l.lemma_id = s.lemma_id " +
            "WHERE s.ayah_id BETWEEN :firstAyahId AND :lastAyahId ORDER BY s.ayah_id, s.word_index, s.segment_index",
    )
    suspend fun lemmaKeysBetween(firstAyahId: Int, lastAyahId: Int): List<WordLemmaKey>
}
