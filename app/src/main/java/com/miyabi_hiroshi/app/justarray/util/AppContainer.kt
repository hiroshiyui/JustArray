package com.miyabi_hiroshi.app.justarray.util

import android.content.Context
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictionaryRepository
import com.miyabi_hiroshi.app.justarray.data.prefs.UserPreferences

class AppContainer private constructor(context: Context) {
    val database: ArrayDatabase = ArrayDatabase.getInstance(context)
    val dictionaryRepository: DictionaryRepository = DictionaryRepository(dao = database.dictionaryDao())
    val userPreferences: UserPreferences = UserPreferences(context)

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
