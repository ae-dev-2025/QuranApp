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

    /** Every card, for Export. */
    @Query("SELECT * FROM review_cards")
    suspend fun allCards(): List<ReviewCardEntity>

    /** Words answered for the first time at or after [since]: the new words started today. */
    @Query("SELECT COUNT(DISTINCT item_id) FROM review_log WHERE state_before IS NULL AND item_id LIKE 'word.%' AND reviewed_at >= :since")
    suspend fun wordsStartedSince(since: Long): Int

    /** Reviews answered at or after [since]. */
    @Query("SELECT * FROM review_log WHERE reviewed_at >= :since")
    suspend fun logSince(since: Long): List<ReviewLogEntity>

    /** Every answered review, oldest first, for Export. */
    @Query("SELECT * FROM review_log ORDER BY id")
    suspend fun allLogs(): List<ReviewLogEntity>

    @Upsert
    suspend fun upsertAll(cards: List<ReviewCardEntity>)

    @Insert
    suspend fun insertLogs(logs: List<ReviewLogEntity>)

    @Query("DELETE FROM review_cards WHERE item_id = :itemId")
    suspend fun deleteCard(itemId: String)
}
