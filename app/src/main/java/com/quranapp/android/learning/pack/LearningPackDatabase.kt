package com.quranapp.android.learning.pack

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

/**
 * learning_pack.db, opened in place from the app's private storage.
 *
 * Unlike quranapp.db it isn't in the APK: it's an optional download (decision 13). The
 * version must equal the pack's `PRAGMA user_version`. A pack built for another version is
 * replaced by downloading the matching one, never migrated.
 */
@Database(
    entities = [
        PackMetaEntity::class,
        PackCreditEntity::class,
        RootEntity::class,
        LemmaEntity::class,
        SegmentEntity::class,
        SyntaxEntity::class,
        WordGlossEntity::class,
    ],
    version = LearningPackDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
abstract class LearningPackDatabase : RoomDatabase() {
    abstract fun dao(): LearningPackDao

    companion object {
        /** Must equal SCHEMA_VERSION in tools/learning-pack/learning_pack/pack.py. */
        const val SCHEMA_VERSION = 1

        /**
         * Opens the pack at [file]. Room checks on first open that every table matches the
         * entities, and throws if not. An absolute path makes Room open the file where it
         * is instead of in the databases folder.
         */
        fun open(context: Context, file: File): LearningPackDatabase =
            Room.databaseBuilder(context.applicationContext, LearningPackDatabase::class.java, file.absolutePath)
                .build()
    }
}
