package com.miyabi_hiroshi.app.justarray.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DictionaryEntry::class, ShortCodeEntry::class, SpecialCodeEntry::class, UserCandidate::class, UserPhrase::class, EnglishWordFrequency::class],
    version = 4,
    exportSchema = false
)
abstract class ArrayDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: ArrayDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS english_word_frequencies (" +
                        "word TEXT NOT NULL, " +
                        "frequency INTEGER NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY(word))"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS user_phrases (" +
                        "code TEXT NOT NULL, " +
                        "phrase TEXT NOT NULL, " +
                        "PRIMARY KEY(code, phrase))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_user_phrases_code ON user_phrases(code)"
                )
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS user_candidates (" +
                        "code TEXT NOT NULL, " +
                        "character TEXT NOT NULL, " +
                        "frequency INTEGER NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY(code, character))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_user_candidates_code ON user_candidates(code)"
                )
                db.execSQL(
                    "INSERT INTO user_candidates (code, character, frequency) " +
                        "SELECT code, character, frequency FROM dictionary WHERE frequency > 0"
                )
            }
        }

        fun getInstance(context: Context): ArrayDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ArrayDatabase::class.java,
                    "array_dictionary.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { INSTANCE = it }
            }
        }
    }
}
