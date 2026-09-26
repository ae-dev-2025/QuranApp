package com.quranapp.android.learning.progress

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** How far a learner has got with a concept. Stored by name, so never rename an entry. */
enum class ConceptStatus {
    /** The learner has started but doesn't know it yet. */
    LEARNING,

    /** The learner knows it. */
    KNOWN,
}

/**
 * One row per concept the learner has interacted with. Concepts without a row are "new".
 *
 * This lives in the user database (`user_db`), next to bookmarks and reading history, so
 * it is never overwritten by content updates.
 */
@Entity(tableName = "concept_progress")
data class ConceptProgressEntity(
    /** One of `ConceptIds`. */
    @PrimaryKey
    @ColumnInfo(name = "concept_id")
    val conceptId: String,

    @ColumnInfo(name = "status")
    val status: ConceptStatus,

    /** When the status last changed, in milliseconds since 1970 (System.currentTimeMillis). */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
