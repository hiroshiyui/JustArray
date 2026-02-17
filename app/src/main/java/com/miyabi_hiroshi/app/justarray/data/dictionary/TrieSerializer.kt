package com.miyabi_hiroshi.app.justarray.data.dictionary

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object TrieSerializer {
    private const val MAIN_TRIE_FILE = "main_trie.dat"
    private const val SHORT_TRIE_FILE = "short_trie.dat"
    private const val SPECIAL_TRIE_FILE = "special_trie.dat"
    private const val ENGLISH_TRIE_FILE = "english_trie.dat"
    private const val FINGERPRINT_FILE = "dict_fingerprint.txt"

    private val MAGIC = byteArrayOf('A'.code.toByte(), 'T'.code.toByte())
    private const val FORMAT_VERSION = 1

    fun saveMainTrie(context: Context, trie: ArrayTrie) = save(context, MAIN_TRIE_FILE, trie)
    fun saveShortTrie(context: Context, trie: ArrayTrie) = save(context, SHORT_TRIE_FILE, trie)
    fun saveSpecialTrie(context: Context, trie: ArrayTrie) = save(context, SPECIAL_TRIE_FILE, trie)
    fun saveEnglishTrie(context: Context, trie: ArrayTrie) = save(context, ENGLISH_TRIE_FILE, trie)

    fun loadMainTrie(context: Context): ArrayTrie? = load(context, MAIN_TRIE_FILE)
    fun loadShortTrie(context: Context): ArrayTrie? = load(context, SHORT_TRIE_FILE)
    fun loadSpecialTrie(context: Context): ArrayTrie? = load(context, SPECIAL_TRIE_FILE)
    fun loadEnglishTrie(context: Context): ArrayTrie? = load(context, ENGLISH_TRIE_FILE)

    fun triesExist(context: Context): Boolean {
        val file = File(context.filesDir, MAIN_TRIE_FILE)
        if (!file.exists() || file.length() < MAGIC.size + 4) return false
        // Verify magic bytes to detect old Java Serializable format
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(MAGIC.size)
                if (fis.read(header) != MAGIC.size) return false
                header.contentEquals(MAGIC)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun englishTrieExists(context: Context): Boolean {
        val file = File(context.filesDir, ENGLISH_TRIE_FILE)
        if (!file.exists() || file.length() < MAGIC.size + 4) return false
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(MAGIC.size)
                if (fis.read(header) != MAGIC.size) return false
                header.contentEquals(MAGIC)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun deleteAll(context: Context) {
        val dir = context.filesDir
        listOf(MAIN_TRIE_FILE, SHORT_TRIE_FILE, SPECIAL_TRIE_FILE, ENGLISH_TRIE_FILE, FINGERPRINT_FILE).forEach {
            File(dir, it).delete()
        }
    }

    fun saveFingerprint(context: Context, fingerprint: String) {
        File(context.filesDir, FINGERPRINT_FILE).writeText(fingerprint)
    }

    fun loadFingerprint(context: Context): String? {
        val file = File(context.filesDir, FINGERPRINT_FILE)
        return if (file.exists()) file.readText() else null
    }

    private fun save(context: Context, fileName: String, trie: ArrayTrie) {
        val file = File(context.filesDir, fileName)
        DataOutputStream(BufferedOutputStream(FileOutputStream(file))).use { dos ->
            dos.write(MAGIC)
            dos.writeInt(FORMAT_VERSION)
            writeNode(dos, trie.root)
        }
    }

    private fun load(context: Context, fileName: String): ArrayTrie? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null
        return try {
            DataInputStream(BufferedInputStream(FileInputStream(file))).use { dis ->
                val header = ByteArray(MAGIC.size)
                dis.readFully(header)
                if (!header.contentEquals(MAGIC)) return null
                val version = dis.readInt()
                if (version != FORMAT_VERSION) return null
                val trie = ArrayTrie()
                readNode(dis, trie.root)
                trie
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun writeNode(dos: DataOutputStream, node: TrieNode) {
        dos.writeInt(node.values.size)
        for (value in node.values) {
            dos.writeUTF(value)
        }
        dos.writeInt(node.children.size)
        for ((key, child) in node.children) {
            dos.writeChar(key.code)
            writeNode(dos, child)
        }
    }

    private fun readNode(dis: DataInputStream, node: TrieNode) {
        val valueCount = dis.readInt()
        for (i in 0 until valueCount) {
            node.values.add(dis.readUTF())
        }
        val childCount = dis.readInt()
        for (i in 0 until childCount) {
            val key = dis.readChar()
            val child = TrieNode()
            readNode(dis, child)
            node.children[key] = child
        }
    }
}
