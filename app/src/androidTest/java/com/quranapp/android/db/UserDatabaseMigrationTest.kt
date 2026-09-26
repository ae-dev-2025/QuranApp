package com.quranapp.android.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quranapp.android.learning.progress.ConceptProgressEntity
import com.quranapp.android.learning.progress.ConceptStatus
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
 * 2. open it with the current Room code, which runs MIGRATION_2_3 and then checks every
 *    table against the entities (it throws if anything differs),
 * 3. check old data survived and the new table works.
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
    fun migrate2To3_keepsBookmarksAndAddsConceptProgress() = runBlocking {
        createVersion2Database()

        val database = Room.databaseBuilder(context, UserDatabase::class.java, TEST_DB)
            .addMigrations(UserDatabase.MIGRATION_2_3)
            .build()

        try {
            val bookmarks = database.bookmarkDao().getBookmarksFlow().first()
            assertEquals("Ayat al-Kursi", bookmarks.single().note)

            val dao = database.conceptProgressDao()
            dao.upsert(ConceptProgressEntity("reading.letters", ConceptStatus.KNOWN, 1L))
            assertEquals(listOf("reading.letters"), dao.observeKnownIds().first())
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
