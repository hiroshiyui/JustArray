package com.miyabi_hiroshi.app.justarray.data.dictionary

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArrayTrieTest {

    private lateinit var trie: ArrayTrie

    @Before
    fun setUp() {
        trie = ArrayTrie()
    }

    @Test
    fun `insert and lookup returns matching entries`() {
        trie.insert("abc", "字")
        trie.insert("def", "詞")

        assertEquals(listOf("字"), trie.lookup("abc"))
        assertEquals(listOf("詞"), trie.lookup("def"))
    }

    @Test
    fun `lookup nonexistent code returns empty list`() {
        trie.insert("abc", "字")

        assertEquals(emptyList<String>(), trie.lookup("xyz"))
    }

    @Test
    fun `no duplicates when inserting same code and value twice`() {
        trie.insert("abc", "字")
        trie.insert("abc", "字")

        assertEquals(listOf("字"), trie.lookup("abc"))
    }

    @Test
    fun `multiple values for same code returns all values`() {
        trie.insert("abc", "字")
        trie.insert("abc", "詞")
        trie.insert("abc", "詩")

        assertEquals(listOf("字", "詞", "詩"), trie.lookup("abc"))
    }

    @Test
    fun `prefixLookup returns values from all descendants`() {
        trie.insert("ab", "甲")
        trie.insert("abc", "乙")
        trie.insert("abd", "丙")
        trie.insert("abcd", "丁")

        val results = trie.prefixLookup("ab")

        assertTrue("甲" in results)
        assertTrue("乙" in results)
        assertTrue("丙" in results)
        assertTrue("丁" in results)
        assertEquals(4, results.size)
    }

    @Test
    fun `prefixLookup nonexistent prefix returns empty list`() {
        trie.insert("abc", "字")

        assertEquals(emptyList<String>(), trie.prefixLookup("xyz"))
    }

    @Test
    fun `isEmpty returns true for empty trie`() {
        assertTrue(trie.isEmpty)
    }

    @Test
    fun `isEmpty returns false after insert`() {
        trie.insert("a", "字")

        assertFalse(trie.isEmpty)
    }
}
