package com.quranapp.android.learning.pack

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Opens a pack the way the app will: a file that Room didn't create, sitting in the app's
 * private files folder. Room checks every table against the entities on first open (and
 * throws if one differs), then the DAO reads it.
 */
@RunWith(AndroidJUnit4::class)
class LearningPackDatabaseTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val file = File(context.filesDir, "learning-test/learning_pack.db")

    @Before
    @After
    fun deleteTestPack() {
        file.parentFile?.deleteRecursively()
    }

    @Test
    fun opensAPackBuiltOutsideRoom() = runBlocking {
        writeTinyPack()

        val database = LearningPackDatabase.open(context, file)
        try {
            val dao = database.dao()
            assertEquals("1", dao.meta("pack_version"))
            assertNull(dao.meta("missing"))

            val segments = dao.segmentsOfAyah(1005)
            assertEquals(listOf(1), segments.mapNotNull { it.lemmaId })
            val lemma = dao.lemmas(listOf(1)).single()
            assertEquals("Eabada", lemma.lemmaKey)
            assertEquals(ROOT_LETTERS, dao.roots(listOf(lemma.rootId!!)).single().letters)
            assertEquals(listOf(WordLocation(1005, 1)), dao.occurrencesOfLemma(1, limit = 10))
            assertEquals("we worship", dao.glossesOfAyah(1005).single().gloss)
            assertEquals("NOMINATIVE", dao.syntaxOfAyah(1005).single().caseMood)
        } finally {
            database.close()
        }
    }

    /** The statements are pack.SCHEMA from tools/learning-pack (tests there compare it with Room). */
    private fun writeTinyPack() {
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            SCHEMA.forEach(db::execSQL)
            db.execSQL("INSERT INTO meta VALUES ('pack_version', '1')")
            db.execSQL("INSERT INTO roots VALUES (1, 'Ebd', ?, 275)", arrayOf(ROOT_LETTERS))
            db.execSQL("INSERT INTO lemmas VALUES (1, 'Eabada', 'headword', 'corpus', 'V', 1, 'I', 122, NULL)")
            db.execSQL("INSERT INTO segments VALUES (1005, 1, 0, 'form', 'STEM', 'V', 1, 'IMPF|1P')")
            db.execSQL(
                "INSERT INTO syntax VALUES (1005, 1, 1, 'text', 'IV', 'Stem', 'DECLN', 'IV', " +
                    "'NOT_CONSTRUCT', 'NOMINATIVE', 'DHAMMA', NULL, NULL)",
            )
            db.execSQL("INSERT INTO word_glosses VALUES (1005, 1, 'we worship')")
            db.version = LearningPackDatabase.SCHEMA_VERSION
        }
    }

    private companion object {
        const val ROOT_LETTERS = "\u0639 \u0628 \u062f"

        val SCHEMA = listOf(
            "CREATE TABLE IF NOT EXISTS `meta` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))",
            "CREATE TABLE IF NOT EXISTS `credits` (`credit_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `licence` " +
                "TEXT NOT NULL, `url` TEXT NOT NULL, `notice` TEXT NOT NULL, PRIMARY KEY(`credit_id`))",
            "CREATE TABLE IF NOT EXISTS `roots` (`root_id` INTEGER NOT NULL, `root_key` TEXT NOT NULL, `letters` " +
                "TEXT NOT NULL, `occurrences` INTEGER NOT NULL, PRIMARY KEY(`root_id`))",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_roots_root_key` ON `roots` (`root_key`)",
            "CREATE TABLE IF NOT EXISTS `lemmas` (`lemma_id` INTEGER NOT NULL, `lemma_key` TEXT NOT NULL, " +
                "`headword` TEXT NOT NULL, `headword_source` TEXT NOT NULL, `pos` TEXT NOT NULL, `root_id` INTEGER, " +
                "`verb_form` TEXT, `occurrences` INTEGER NOT NULL, `gloss` TEXT, PRIMARY KEY(`lemma_id`))",
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_lemmas_lemma_key` ON `lemmas` (`lemma_key`)",
            "CREATE INDEX IF NOT EXISTS `index_lemmas_root_id` ON `lemmas` (`root_id`)",
            "CREATE TABLE IF NOT EXISTS `segments` (`ayah_id` INTEGER NOT NULL, `word_index` INTEGER NOT NULL, " +
                "`segment_index` INTEGER NOT NULL, `form` TEXT NOT NULL, `kind` TEXT NOT NULL, `tag` TEXT NOT NULL, " +
                "`lemma_id` INTEGER, `features` TEXT NOT NULL, PRIMARY KEY(`ayah_id`, `word_index`, `segment_index`))",
            "CREATE INDEX IF NOT EXISTS `index_segments_lemma_id` ON `segments` (`lemma_id`)",
            "CREATE TABLE IF NOT EXISTS `syntax` (`ayah_id` INTEGER NOT NULL, `word_index` INTEGER NOT NULL, " +
                "`segment_index` INTEGER NOT NULL, `text` TEXT NOT NULL, `morph_tag` TEXT NOT NULL, `morph_type` TEXT" +
                " NOT NULL, `declinability` TEXT, `role` TEXT, `construct` TEXT, `case_mood` TEXT, `case_marker` " +
                "TEXT, `phrase` TEXT, `phrase_function` TEXT, PRIMARY KEY(`ayah_id`, `word_index`, `segment_index`))",
            "CREATE TABLE IF NOT EXISTS `word_glosses` (`ayah_id` INTEGER NOT NULL, `word_index` INTEGER NOT " +
                "NULL, `gloss` TEXT NOT NULL, PRIMARY KEY(`ayah_id`, `word_index`))",
        )
    }
}
