package com.miyabi_hiroshi.app.justarray.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DictionaryEntry::class, ShortCodeEntry::class, SpecialCodeEntry::class],
    version = 1,
    exportSchema = false
)
abstract class ArrayDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: ArrayDatabase? = null

        fun getInstance(context: Context): ArrayDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ArrayDatabase::class.java,
                    "array_dictionary.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
