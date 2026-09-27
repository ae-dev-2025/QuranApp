package com.quranapp.android.learning.progress

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM review_cards WHERE item_id = :itemId")
    suspend fun card(itemId: String): ReviewCardEntity?

    /** Every card; updates whenever one changes. */
    @Query("SELECT * FROM review_cards")
    fun observeCards(): Flow<List<ReviewCardEntity>>

    /** Cards due at [now], the most overdue first. */
    @Query("SELECT * FROM review_cards WHERE due_at <= :now ORDER BY due_at LIMIT :limit")
    suspend fun due(now: Long, limit: Int): List<ReviewCardEntity>

    @Upsert
    suspend fun upsert(card: ReviewCardEntity)

    @Insert
    suspend fun insertLog(log: ReviewLogEntity)

    /** Saves a review's result and its log entry together, or neither. */
    @Transaction
    suspend fun record(card: ReviewCardEntity, log: ReviewLogEntity) {
        upsert(card)
        insertLog(log)
    }

    @Query("DELETE FROM review_cards WHERE item_id = :itemId")
    suspend fun deleteCard(itemId: String)
}
