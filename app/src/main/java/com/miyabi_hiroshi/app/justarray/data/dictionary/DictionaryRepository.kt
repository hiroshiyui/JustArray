package com.miyabi_hiroshi.app.justarray.data.dictionary

import com.miyabi_hiroshi.app.justarray.data.db.DictionaryDao

class DictionaryRepository(
    private val dao: DictionaryDao,
    private var mainTrie: ArrayTrie = ArrayTrie(),
    private var shortTrie: ArrayTrie = ArrayTrie(),
    private var specialTrie: ArrayTrie = ArrayTrie(),
) {
    var isLoaded: Boolean = false
        private set

    var useShortCodes: Boolean = true
    var useSpecialCodes: Boolean = true

    fun setTries(main: ArrayTrie, short: ArrayTrie, special: ArrayTrie) {
        mainTrie = main
        shortTrie = short
        specialTrie = special
        isLoaded = true
    }

    /**
     * Lookup with priority: special codes → short codes (1-2 keys) → main dictionary.
     */
    fun lookup(code: String): List<String> {
        val results = mutableListOf<String>()

        // 1. Special codes (always exact match)
        if (useSpecialCodes) {
            results.addAll(specialTrie.lookup(code))
        }

        // 2. Short codes (for 1-2 key inputs)
        if (useShortCodes && code.length <= 2) {
            val shortResults = shortTrie.lookup(code)
            for (r in shortResults) {
                if (r !in results) results.add(r)
            }
        }

        // 3. Main dictionary
        val mainResults = mainTrie.lookup(code)
        for (r in mainResults) {
            if (r !in results) results.add(r)
        }

        return results
    }

    fun incrementFrequency(code: String, character: String) {
        try {
            dao.incrementFrequency(code, character)
        } catch (_: Exception) {
            // Non-critical operation
        }
    }
}
