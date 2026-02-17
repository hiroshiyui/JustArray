package com.miyabi_hiroshi.app.justarray.util

import android.content.Context
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictionaryRepository
import com.miyabi_hiroshi.app.justarray.data.prefs.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer private constructor(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database: ArrayDatabase = ArrayDatabase.getInstance(context)
    val dictionaryRepository: DictionaryRepository = DictionaryRepository(
        dao = database.dictionaryDao(),
        scope = applicationScope,
    )
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
