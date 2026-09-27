package com.quranapp.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quranapp.android.db.converters.DbConverters
import com.quranapp.android.db.dao.BookmarkDao
import com.quranapp.android.db.dao.ReadHistoryDao
import com.quranapp.android.db.entities.user.BookmarkEntity
import com.quranapp.android.db.entities.user.ReadHistoryEntity
import com.quranapp.android.learning.progress.ConceptProgressDao
import com.quranapp.android.learning.progress.ConceptProgressEntity
import com.quranapp.android.learning.progress.ReviewCardEntity
import com.quranapp.android.learning.progress.ReviewDao
import com.quranapp.android.learning.progress.ReviewLogEntity

@Database(
    entities = [
        BookmarkEntity::class,
        ReadHistoryEntity::class,
        ConceptProgressEntity::class,
        ReviewCardEntity::class,
        ReviewLogEntity::class,
    ],
    version = 4,
)
@TypeConverters(DbConverters::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readHistoryDao(): ReadHistoryDao
    abstract fun conceptProgressDao(): ConceptProgressDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `read_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `read_type` TEXT NOT NULL,
                        `reader_mode` TEXT NOT NULL,
                        `division_no` INTEGER NOT NULL DEFAULT 0,
                        `chapter_no` INTEGER NOT NULL DEFAULT 0,
                        `from_verse_no` INTEGER NOT NULL DEFAULT 0,
                        `to_verse_no` INTEGER NOT NULL DEFAULT 0,
                        `mushaf_id` INTEGER NOT NULL DEFAULT 0,
                        `page_no` INTEGER,
                        `datetime` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        /** Adds learning-mode progress. The SQL matches schemas/.../UserDatabase/3.json. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `concept_progress` (
                        `concept_id` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`concept_id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /** Adds learning-mode reviews (FSRS cards and their log). The SQL matches schemas/.../4.json. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `review_cards` (`item_id` TEXT NOT NULL, `state` TEXT NOT NULL, " +
                        "`stability` REAL NOT NULL, `difficulty` REAL NOT NULL, `due_at` INTEGER NOT NULL, " +
                        "`last_review_at` INTEGER NOT NULL, `reps` INTEGER NOT NULL, `lapses` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`item_id`))",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_cards_due_at` ON `review_cards` (`due_at`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `review_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`item_id` TEXT NOT NULL, `rating` TEXT NOT NULL, `reviewed_at` INTEGER NOT NULL, " +
                        "`state_before` TEXT)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_review_log_item_id` ON `review_log` (`item_id`)")
            }
        }
    }
}
