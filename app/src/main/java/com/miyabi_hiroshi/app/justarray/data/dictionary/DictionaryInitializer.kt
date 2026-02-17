package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import com.miyabi_hiroshi.app.justarray.data.db.ArrayDatabase
import com.miyabi_hiroshi.app.justarray.data.db.DictionaryEntry
import com.miyabi_hiroshi.app.justarray.data.db.ShortCodeEntry
import com.miyabi_hiroshi.app.justarray.data.db.SpecialCodeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class DictionaryInitializer(private val context: Context) {

    companion object {
        // Default .cin file names from gontera/array30
        const val MAIN_CIN = "array30-OpenVanilla-big-v2023-1.0-20230211.cin"
        const val SHORT_CIN = "array-shortcode-20210725.cin"
        const val SPECIAL_CIN = "array-special-201509.cin"
        const val ENGLISH_WORDS_FILE = "english_words.txt"

        private const val FINGERPRINT_SAMPLE_SIZE = 256
    }

    suspend fun initialize(
        repository: DictionaryRepository,
        database: ArrayDatabase
    ) = withContext(Dispatchers.IO) {
        val currentFingerprint = computeAssetFingerprint()
        val storedFingerprint = TrieSerializer.loadFingerprint(context)

        val cacheValid = TrieSerializer.triesExist(context) && currentFingerprint == storedFingerprint

        if (cacheValid) {
            val main = TrieSerializer.loadMainTrie(context) ?: ArrayTrie()
            val short = TrieSerializer.loadShortTrie(context) ?: ArrayTrie()
            val special = TrieSerializer.loadSpecialTrie(context) ?: ArrayTrie()
            repository.setTries(main, short, special)
        } else {
            // Cache invalid or missing — delete old files and re-parse
            TrieSerializer.deleteAll(context)

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
                database.runInTransaction {
                    dbEntries.chunked(500).forEach { batch ->
                        database.dictionaryDao().insertDictionaryEntries(batch)
                    }
                }
            }

            // Short codes
            val shortEntries = parser.parse(SHORT_CIN)
            val dbShortEntries = mutableListOf<ShortCodeEntry>()
            for ((index, entry) in shortEntries.withIndex()) {
                shortTrie.insert(entry.code, entry.value)
                dbShortEntries.add(ShortCodeEntry(code = entry.code, character = entry.value, priority = index))
            }
            if (dbShortEntries.isNotEmpty()) {
                database.runInTransaction {
                    dbShortEntries.chunked(500).forEach { batch ->
                        database.dictionaryDao().insertShortCodeEntries(batch)
                    }
                }
            }

            // Special codes
            val specialEntries = parser.parse(SPECIAL_CIN)
            val dbSpecialEntries = mutableListOf<SpecialCodeEntry>()
            for (entry in specialEntries) {
                specialTrie.insert(entry.code, entry.value)
                dbSpecialEntries.add(SpecialCodeEntry(code = entry.code, character = entry.value))
            }
            if (dbSpecialEntries.isNotEmpty()) {
                database.runInTransaction {
                    dbSpecialEntries.chunked(500).forEach { batch ->
                        database.dictionaryDao().insertSpecialCodeEntries(batch)
                    }
                }
            }

            // Serialize tries for fast loading next time
            TrieSerializer.saveMainTrie(context, mainTrie)
            TrieSerializer.saveShortTrie(context, shortTrie)
            TrieSerializer.saveSpecialTrie(context, specialTrie)
            TrieSerializer.saveFingerprint(context, currentFingerprint)

            repository.setTries(mainTrie, shortTrie, specialTrie)
        }

        // English trie (independent of Chinese tries)
        loadEnglishTrie(repository)
    }

    private fun loadEnglishTrie(repository: DictionaryRepository) {
        if (TrieSerializer.englishTrieExists(context)) {
            val trie = TrieSerializer.loadEnglishTrie(context) ?: ArrayTrie()
            repository.setEnglishTrie(trie)
        } else {
            val trie = ArrayTrie()
            try {
                context.assets.open(ENGLISH_WORDS_FILE).bufferedReader().useLines { lines ->
                    for (word in lines) {
                        val trimmed = word.trim()
                        if (trimmed.isNotEmpty()) {
                            trie.insert(trimmed.lowercase(), trimmed)
                        }
                    }
                }
            } catch (_: Exception) {
                // English word list not available — feature degrades gracefully
            }
            if (!trie.isEmpty) {
                TrieSerializer.saveEnglishTrie(context, trie)
            }
            repository.setEnglishTrie(trie)
        }
    }

    private fun computeAssetFingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val assetFiles = listOf(MAIN_CIN, SHORT_CIN, SPECIAL_CIN)
        for (fileName in assetFiles) {
            try {
                context.assets.open(fileName).use { stream ->
                    val sample = ByteArray(FINGERPRINT_SAMPLE_SIZE)
                    val bytesRead = stream.read(sample)
                    if (bytesRead > 0) {
                        digest.update(sample, 0, bytesRead)
                    }
                }
                // Include file size via AssetFileDescriptor
                context.assets.openFd(fileName).use { fd ->
                    val sizeBytes = fd.length.toString().toByteArray()
                    digest.update(sizeBytes)
                }
            } catch (_: Exception) {
                // Asset missing — include sentinel so fingerprint changes if it appears later
                digest.update("MISSING:$fileName".toByteArray())
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
