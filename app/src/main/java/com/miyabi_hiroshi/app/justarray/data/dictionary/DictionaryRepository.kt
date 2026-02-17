package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.db.DictionaryDao
import com.miyabi_hiroshi.app.justarray.data.db.UserPhrase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DictLoadState {
    data object NotStarted : DictLoadState
    data object Loading : DictLoadState
    data object Loaded : DictLoadState
    data class Error(val message: String) : DictLoadState
}

class DictionaryRepository(
    private val dao: DictionaryDao,
    private val scope: CoroutineScope,
    private var mainTrie: ArrayTrie = ArrayTrie(),
    private var shortTrie: ArrayTrie = ArrayTrie(),
    private var specialTrie: ArrayTrie = ArrayTrie(),
    private var englishTrie: ArrayTrie = ArrayTrie(),
) {
    private val _loadState = MutableStateFlow<DictLoadState>(DictLoadState.NotStarted)
    val loadState: StateFlow<DictLoadState> = _loadState.asStateFlow()

    val isLoaded: Boolean get() = _loadState.value is DictLoadState.Loaded

    fun setLoadState(state: DictLoadState) {
        _loadState.value = state
    }

    var useShortCodes: Boolean = true
    var useSpecialCodes: Boolean = true
    var useUserCandidates: Boolean = true

    private val cacheLock = Any()
    private val userCandidateCache = HashMap<String, MutableMap<String, Int>>()
    private val englishFrequencyCache = HashMap<String, Int>()

    private var reverseMap: Map<String, List<String>> = emptyMap()

    fun setTries(main: ArrayTrie, short: ArrayTrie, special: ArrayTrie) {
        mainTrie = main
        shortTrie = short
        specialTrie = special
        buildReverseMap()
        _loadState.value = DictLoadState.Loaded
    }

    private fun buildReverseMap() {
        val map = HashMap<String, MutableList<String>>()
        for ((code, char) in mainTrie.allEntries()) {
            map.getOrPut(char) { mutableListOf() }.add(code)
        }
        for (list in map.values) list.sortBy { it.length }
        reverseMap = map
    }

    fun reverseLookup(character: String): String? =
        reverseMap[character]?.firstOrNull()

    fun setEnglishTrie(trie: ArrayTrie) {
        englishTrie = trie
    }

    fun englishPrefixLookup(prefix: String, limit: Int = 50): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val results = englishTrie.prefixLookup(prefix.lowercase()).take(limit).toList()
        if (results.size <= 1) return results

        val frequencyMap = synchronized(cacheLock) {
            val uncached = results.filter { it.lowercase() !in englishFrequencyCache }
            if (uncached.isNotEmpty()) {
                try {
                    val dbResults = dao.getEnglishWordFrequencies(uncached.map { it.lowercase() })
                    for (entry in dbResults) {
                        englishFrequencyCache[entry.word] = entry.frequency
                    }
                    for (word in uncached) {
                        if (word.lowercase() !in englishFrequencyCache) {
                            englishFrequencyCache[word.lowercase()] = 0
                        }
                    }
                } catch (_: Exception) {
                    // Non-critical
                }
            }
            results.associateWith { englishFrequencyCache[it.lowercase()] ?: 0 }
        }

        return results.sortedByDescending { frequencyMap[it] ?: 0 }
    }

    fun incrementEnglishFrequency(word: String) {
        synchronized(cacheLock) {
            englishFrequencyCache[word] = (englishFrequencyCache[word] ?: 0) + 1
        }
        scope.launch(Dispatchers.IO) {
            try {
                dao.incrementEnglishWordFrequency(word)
            } catch (_: Exception) {
                // Non-critical operation
            }
        }
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
            val frequencyMap = synchronized(cacheLock) {
                userCandidateCache.getOrPut(code) {
                    try {
                        dao.lookupUserCandidates(code)
                            .associate { it.character to it.frequency }
                            .toMutableMap()
                    } catch (_: Exception) {
                        mutableMapOf()
                    }
                }
            }
            if (frequencyMap.isNotEmpty()) {
                results.sortByDescending { frequencyMap[it] ?: 0 }
            }
        }

        return results
    }

    fun incrementFrequency(code: String, character: String) {
        synchronized(cacheLock) {
            val codeMap = userCandidateCache.getOrPut(code) { mutableMapOf() }
            codeMap[character] = (codeMap[character] ?: 0) + 1
        }
        scope.launch(Dispatchers.IO) {
            try {
                dao.incrementUserFrequency(code, character)
            } catch (_: Exception) {
                // Non-critical operation
            }
        }
    }

    fun clearUserCandidates() {
        synchronized(cacheLock) {
            userCandidateCache.clear()
            englishFrequencyCache.clear()
        }
        scope.launch(Dispatchers.IO) {
            try {
                dao.clearUserCandidates()
                dao.clearEnglishWordFrequencies()
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
        _loadState.value = DictLoadState.NotStarted
        DictionaryInitializer(context).initialize(this@DictionaryRepository, database)
    }
}
