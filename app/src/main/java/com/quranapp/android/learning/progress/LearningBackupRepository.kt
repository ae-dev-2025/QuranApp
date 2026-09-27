package com.quranapp.android.learning.progress

import androidx.room.withTransaction
import com.quranapp.android.db.UserDatabase
import com.quranapp.android.learning.path.LearningPreferences

/** What an import changed, for the message shown afterwards. */
data class ImportResult(val items: Int, val skipped: Int)

/** Moves learning progress in and out of the Export/Import file. */
class LearningBackupRepository(private val database: UserDatabase) {
    private val progress = database.conceptProgressDao()
    private val reviews = database.reviewDao()

    suspend fun export(): LearningBackup =
        LearningBackups.of(progress.known(), reviews.allCards(), reviews.allLogs()).copy(settings = LearningSettingsEntry.current())

    /** Deletes all learning progress: known items, review cards and the log, together. The pack stays. */
    suspend fun resetAll() {
        database.withTransaction {
            progress.deleteAll()
            reviews.deleteAllCards()
            reviews.deleteAllLogs()
        }
        LearningPreferences.reset()
    }

    /**
     * Adds [backup] to this phone's progress in one transaction: either all of it is saved or
     * none. Nothing is deleted or un-known: known items are added, each card keeps its more
     * recent review, and log entries already here aren't added twice.
     */
    suspend fun import(backup: CheckedBackup): ImportResult = database.withTransaction {
        val knownHere = progress.known().mapTo(HashSet()) { it.conceptId }
        val newKnown = backup.known.filter { it.conceptId !in knownHere }
        newKnown.forEach { progress.upsert(it) }

        val cardsHere = reviews.allCards().associateBy { it.itemId }
        reviews.upsertAll(backup.cards.map { LearningBackups.merge(cardsHere[it.itemId], it) })

        val logHere = reviews.allLogs().mapTo(HashSet()) { Triple(it.itemId, it.reviewedAt, it.rating) }
        reviews.insertLogs(backup.log.filter { Triple(it.itemId, it.reviewedAt, it.rating) !in logHere })

        val items = (backup.known.map { it.conceptId } + backup.cards.map { it.itemId }).toSet().size
        ImportResult(items = items, skipped = backup.skipped)
    }.also { backup.settings?.apply() }
}
