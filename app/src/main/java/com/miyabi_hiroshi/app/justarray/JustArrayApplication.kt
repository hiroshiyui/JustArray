package com.miyabi_hiroshi.app.justarray

import android.app.Application
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictionaryInitializer
import com.miyabi_hiroshi.app.justarray.util.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JustArrayApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        val appContainer = AppContainer.getInstance(this)

        applicationScope.launch {
            val initializer = DictionaryInitializer(this@JustArrayApplication)
            initializer.initialize(appContainer.dictionaryRepository, appContainer.database)
        }
    }
}
