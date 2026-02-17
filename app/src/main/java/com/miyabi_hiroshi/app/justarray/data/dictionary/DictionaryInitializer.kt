package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.db.DictionaryEntry
import com.miyabi_hiroshi.app.justarray.data.db.ShortCodeEntry
import com.miyabi_hiroshi.app.justarray.data.db.SpecialCodeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryInitializer(private val context: Context) {

    companion object {
        // Default .cin file names from gontera/array30
        const val MAIN_CIN = "array30-OpenVanilla-big-v2023-1.0-20230211.cin"
        const val SHORT_CIN = "array-shortcode-20210725.cin"
        const val SPECIAL_CIN = "array-special-201509.cin"
    }

    suspend fun initialize(
        repository: DictionaryRepository,
        database: ArrayDatabase
    ) = withContext(Dispatchers.IO) {
        // Try to load serialized tries first
        if (TrieSerializer.triesExist(context)) {
            val main = TrieSerializer.loadMainTrie(context) ?: ArrayTrie()
            val short = TrieSerializer.loadShortTrie(context) ?: ArrayTrie()
            val special = TrieSerializer.loadSpecialTrie(context) ?: ArrayTrie()
            repository.setTries(main, short, special)
            return@withContext
        }

        // Parse .cin files from assets
        val parser = CinParser(context)

        val mainTrie = ArrayTrie()
        val shortTrie = ArrayTrie()
        val specialTrie = ArrayTrie()

        // Main dictionary
        val mainEntries = parser.parse(MAIN_CIN)
        val dbEntries = mutableListOf<DictionaryEntry>()
        for (entry in mainEntries) {
            mainTrie.insert(entry.code, entry.value)
            dbEntries.add(DictionaryEntry(code = entry.code, character = entry.value))
        }
        if (dbEntries.isNotEmpty()) {
            database.dictionaryDao().insertDictionaryEntries(dbEntries)
        }

        // Short codes
        val shortEntries = parser.parse(SHORT_CIN)
        val dbShortEntries = mutableListOf<ShortCodeEntry>()
        for ((index, entry) in shortEntries.withIndex()) {
            shortTrie.insert(entry.code, entry.value)
            dbShortEntries.add(ShortCodeEntry(code = entry.code, character = entry.value, priority = index))
        }
        if (dbShortEntries.isNotEmpty()) {
            database.dictionaryDao().insertShortCodeEntries(dbShortEntries)
        }

        // Special codes
        val specialEntries = parser.parse(SPECIAL_CIN)
        val dbSpecialEntries = mutableListOf<SpecialCodeEntry>()
        for (entry in specialEntries) {
            specialTrie.insert(entry.code, entry.value)
            dbSpecialEntries.add(SpecialCodeEntry(code = entry.code, character = entry.value))
        }
        if (dbSpecialEntries.isNotEmpty()) {
            database.dictionaryDao().insertSpecialCodeEntries(dbSpecialEntries)
        }

        // Serialize tries for fast loading next time
        TrieSerializer.saveMainTrie(context, mainTrie)
        TrieSerializer.saveShortTrie(context, shortTrie)
        TrieSerializer.saveSpecialTrie(context, specialTrie)

        repository.setTries(mainTrie, shortTrie, specialTrie)
    }
}
