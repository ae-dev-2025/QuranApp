package com.quranapp.android.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.learning.progress.ConceptProgressEntity
import com.quranapp.android.learning.progress.ConceptStatus
import com.quranapp.android.learning.progress.ReviewCardEntity
import com.quranapp.android.learning.progress.ReviewLogEntity
import com.quranapp.android.learning.progress.ReviewRating
import com.quranapp.android.learning.progress.ReviewState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on a device or emulator, like a real app update would:
 * 1. write a user database file exactly as version 2 of the app left it,
 * 2. open it with the current Room code, which runs MIGRATION_2_3 and MIGRATION_3_4 and
 *    then checks every table against the entities (it throws if anything differs),
 * 3. check old data survived and the new tables work.
 */
@RunWith(AndroidJUnit4::class)
class UserDatabaseMigrationTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    @After
    fun deleteTestDatabase() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migrate2To4_keepsBookmarksAndAddsLearningTables() = runBlocking {
        createVersion2Database()

        val database = Room.databaseBuilder(context, UserDatabase::class.java, TEST_DB)
            .addMigrations(UserDatabase.MIGRATION_2_3, UserDatabase.MIGRATION_3_4)
            .build()

        try {
            val bookmarks = database.bookmarkDao().getBookmarksFlow().first()
            assertEquals("Ayat al-Kursi", bookmarks.single().note)

            val dao = database.conceptProgressDao()
            dao.upsert(ConceptProgressEntity("reading.letters", ConceptStatus.KNOWN, 1L))
            assertEquals(listOf("reading.letters"), dao.observeKnownIds().first())

            val reviews = database.reviewDao()
            val card = ReviewCardEntity("word.Eabada", ReviewState.REVIEW, 3.2, 5.0, 10L, 1L, 1, 0)
            val log = ReviewLogEntity(itemId = card.itemId, rating = ReviewRating.GOOD, reviewedAt = 1L, stateBefore = null)
            reviews.record(card, log)
            assertEquals(card, reviews.card("word.Eabada"))
            assertEquals(listOf(card), reviews.due(now = 10L, limit = 5))
            assertEquals(emptyList<ReviewCardEntity>(), reviews.due(now = 9L, limit = 5))
        } finally {
            database.close()
        }
    }

    /** Version 2 of the user database. The SQL is copied from schemas/.../UserDatabase/2.json. */
    private fun createVersion2Database() {
        val file = context.getDatabasePath(TEST_DB).apply { parentFile?.mkdirs() }

        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `user_bookmarks` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`chapter_no` INTEGER NOT NULL, `from_verse_no` INTEGER NOT NULL, " +
                    "`to_verse_no` INTEGER NOT NULL, `note` TEXT, `date` INTEGER NOT NULL)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `read_history` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`read_type` TEXT NOT NULL, `reader_mode` TEXT NOT NULL, " +
                    "`division_no` INTEGER NOT NULL, `chapter_no` INTEGER NOT NULL, " +
                    "`from_verse_no` INTEGER NOT NULL, `to_verse_no` INTEGER NOT NULL, " +
                    "`mushaf_code` TEXT, `mushaf_variant` TEXT, `page_no` INTEGER, " +
                    "`datetime` INTEGER NOT NULL)",
            )
            db.execSQL(
                "INSERT INTO user_bookmarks (chapter_no, from_verse_no, to_verse_no, note, date) " +
                    "VALUES (2, 255, 255, 'Ayat al-Kursi', 0)",
            )
            db.version = 2
        }
    }

    private companion object {
        const val TEST_DB = "user-db-migration-test"
    }
}
