package com.quranapp.android.learning.progress

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Database access for [ConceptProgressEntity]. Room writes the implementation of this
 * interface at build time from the SQL in the annotations.
 */
@Dao
interface ConceptProgressDao {
    /** Emits the IDs of known concepts now, and again every time the table changes. */
    @Query("SELECT concept_id FROM concept_progress WHERE status = 'KNOWN'")
    fun observeKnownIds(): Flow<List<String>>

    @Query("SELECT * FROM concept_progress WHERE concept_id = :conceptId")
    suspend fun get(conceptId: String): ConceptProgressEntity?

    /** Inserts the row, or replaces it if the concept already has one. */
    @Upsert
    suspend fun upsert(progress: ConceptProgressEntity)

    @Query("DELETE FROM concept_progress WHERE concept_id = :conceptId")
    suspend fun delete(conceptId: String)
}
