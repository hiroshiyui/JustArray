package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object TrieSerializer {
    private const val MAIN_TRIE_FILE = "main_trie.dat"
    private const val SHORT_TRIE_FILE = "short_trie.dat"
    private const val SPECIAL_TRIE_FILE = "special_trie.dat"
    private const val ENGLISH_TRIE_FILE = "english_trie.dat"

    fun saveMainTrie(context: Context, trie: ArrayTrie) = save(context, MAIN_TRIE_FILE, trie)
    fun saveShortTrie(context: Context, trie: ArrayTrie) = save(context, SHORT_TRIE_FILE, trie)
    fun saveSpecialTrie(context: Context, trie: ArrayTrie) = save(context, SPECIAL_TRIE_FILE, trie)
    fun saveEnglishTrie(context: Context, trie: ArrayTrie) = save(context, ENGLISH_TRIE_FILE, trie)

    fun loadMainTrie(context: Context): ArrayTrie? = load(context, MAIN_TRIE_FILE)
    fun loadShortTrie(context: Context): ArrayTrie? = load(context, SHORT_TRIE_FILE)
    fun loadSpecialTrie(context: Context): ArrayTrie? = load(context, SPECIAL_TRIE_FILE)
    fun loadEnglishTrie(context: Context): ArrayTrie? = load(context, ENGLISH_TRIE_FILE)

    fun triesExist(context: Context): Boolean {
        val dir = context.filesDir
        return File(dir, MAIN_TRIE_FILE).exists()
    }

    fun englishTrieExists(context: Context): Boolean {
        return File(context.filesDir, ENGLISH_TRIE_FILE).exists()
    }

    fun deleteAll(context: Context) {
        val dir = context.filesDir
        listOf(MAIN_TRIE_FILE, SHORT_TRIE_FILE, SPECIAL_TRIE_FILE, ENGLISH_TRIE_FILE).forEach {
            File(dir, it).delete()
        }
    }

    private fun save(context: Context, fileName: String, trie: ArrayTrie) {
        val file = File(context.filesDir, fileName)
        ObjectOutputStream(file.outputStream().buffered()).use { oos ->
            oos.writeObject(trie)
        }
    }

    private fun load(context: Context, fileName: String): ArrayTrie? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null
        return try {
            ObjectInputStream(file.inputStream().buffered()).use { ois ->
                ois.readObject() as ArrayTrie
            }
        } catch (e: Exception) {
            null
        }
    }
}
