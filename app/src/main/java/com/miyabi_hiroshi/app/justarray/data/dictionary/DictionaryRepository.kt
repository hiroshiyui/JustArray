package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.db.DictionaryDao
import com.miyabi_hiroshi.app.justarray.data.db.UserPhrase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DictionaryRepository(
    private val dao: DictionaryDao,
    private val scope: CoroutineScope,
    private var mainTrie: ArrayTrie = ArrayTrie(),
    private var shortTrie: ArrayTrie = ArrayTrie(),
    private var specialTrie: ArrayTrie = ArrayTrie(),
    private var englishTrie: ArrayTrie = ArrayTrie(),
) {
    var isLoaded: Boolean = false
        private set

    var useShortCodes: Boolean = true
    var useSpecialCodes: Boolean = true
    var useUserCandidates: Boolean = true

    fun setTries(main: ArrayTrie, short: ArrayTrie, special: ArrayTrie) {
        mainTrie = main
        shortTrie = short
        specialTrie = special
        isLoaded = true
    }

    fun setEnglishTrie(trie: ArrayTrie) {
        englishTrie = trie
    }

    fun englishPrefixLookup(prefix: String, limit: Int = 50): List<String> {
        if (prefix.isEmpty()) return emptyList()
        return englishTrie.prefixLookup(prefix.lowercase()).take(limit)
    }

    /**
     * Lookup with priority: special codes → short codes (1-2 keys) → main dictionary.
     * Results are stable-sorted by user candidate frequency (descending) from the Room DB.
     */
    fun lookup(code: String): List<String> {
        val results = mutableListOf<String>()

        // 0. User phrases (highest priority)
        try {
            val userPhrases = dao.lookupUserPhrases(code)
            for (up in userPhrases) {
                if (up.phrase !in results) results.add(up.phrase)
            }
        } catch (_: Exception) {
            // Non-critical
        }

        // 1. Special codes (always exact match)
        if (useSpecialCodes) {
            results.addAll(specialTrie.lookup(code))
        }

        // 2. Short codes (for 1-2 key inputs)
        if (useShortCodes && code.length <= 2) {
            val shortResults = shortTrie.lookup(code)
            for (candidate in shortResults) {
                if (candidate !in results) results.add(candidate)
            }
        }

        // 3. Main dictionary
        val mainResults = mainTrie.lookup(code)
        for (candidate in mainResults) {
            if (candidate !in results) results.add(candidate)
        }

        // Sort by user candidate frequency (stable sort preserves relative order for ties)
        if (useUserCandidates) {
            val frequencyMap = try {
                dao.lookupUserCandidates(code).associate { it.character to it.frequency }
            } catch (_: Exception) {
                emptyMap()
            }
            if (frequencyMap.isNotEmpty()) {
                results.sortByDescending { frequencyMap[it] ?: 0 }
            }
        }

        return results
    }

    fun incrementFrequency(code: String, character: String) {
        scope.launch(Dispatchers.IO) {
            try {
                dao.incrementUserFrequency(code, character)
            } catch (_: Exception) {
                // Non-critical operation
            }
        }
    }

    fun clearUserCandidates() {
        scope.launch(Dispatchers.IO) {
            try {
                dao.clearUserCandidates()
            } catch (_: Exception) {
                // Non-critical operation
            }
        }
    }

    suspend fun addUserPhrase(code: String, phrase: String) = withContext(Dispatchers.IO) {
        dao.insertUserPhrase(UserPhrase(code = code, phrase = phrase))
    }

    suspend fun deleteUserPhrase(code: String, phrase: String) = withContext(Dispatchers.IO) {
        dao.deleteUserPhrase(code, phrase)
    }

    suspend fun getAllUserPhrases(): List<UserPhrase> = withContext(Dispatchers.IO) {
        dao.getAllUserPhrases()
    }

    suspend fun reimport(context: Context, database: ArrayDatabase) = withContext(Dispatchers.IO) {
        TrieSerializer.deleteAll(context)
        dao.clearDictionary()
        dao.clearShortCodes()
        dao.clearSpecialCodes()
        isLoaded = false
        DictionaryInitializer(context).initialize(this@DictionaryRepository, database)
    }
}
