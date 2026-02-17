package com.miyabi_hiroshi.app.justarray.data.dictionary

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class TrieSerializerTest {

    @Test
    fun `round trip preserves all entries`() {
        val original = ArrayTrie().apply {
            insert("abc", "字")
            insert("abc", "詞")
            insert("de", "的")
            insert("f", "發")
        }

        val bytes = ByteArrayOutputStream()
        TrieSerializer.writeTo(bytes, original)

        val restored = TrieSerializer.readFrom(ByteArrayInputStream(bytes.toByteArray()))

        assertNotNull(restored)
        assertEquals(listOf("字", "詞"), restored!!.lookup("abc"))
        assertEquals(listOf("的"), restored.lookup("de"))
        assertEquals(listOf("發"), restored.lookup("f"))
        assertEquals(emptyList<String>(), restored.lookup("xyz"))
    }

    @Test
    fun `round trip preserves empty trie`() {
        val original = ArrayTrie()

        val bytes = ByteArrayOutputStream()
        TrieSerializer.writeTo(bytes, original)

        val restored = TrieSerializer.readFrom(ByteArrayInputStream(bytes.toByteArray()))

        assertNotNull(restored)
        assertTrue(restored!!.isEmpty)
    }

    @Test
    fun `round trip preserves CJK and special characters`() {
        val original = ArrayTrie().apply {
            insert("a", "中")
            insert("a", "文")
            insert("b", "日本語")
            insert("c", "émoji🎉")
        }

        val bytes = ByteArrayOutputStream()
        TrieSerializer.writeTo(bytes, original)

        val restored = TrieSerializer.readFrom(ByteArrayInputStream(bytes.toByteArray()))

        assertNotNull(restored)
        assertEquals(listOf("中", "文"), restored!!.lookup("a"))
        assertEquals(listOf("日本語"), restored.lookup("b"))
        assertEquals(listOf("émoji🎉"), restored.lookup("c"))
    }

    @Test
    fun `round trip preserves deep trie structure`() {
        val original = ArrayTrie().apply {
            insert("a", "一")
            insert("ab", "二")
            insert("abc", "三")
            insert("abcd", "四")
        }

        val bytes = ByteArrayOutputStream()
        TrieSerializer.writeTo(bytes, original)

        val restored = TrieSerializer.readFrom(ByteArrayInputStream(bytes.toByteArray()))

        assertNotNull(restored)
        assertEquals(listOf("一"), restored!!.lookup("a"))
        assertEquals(listOf("二"), restored.lookup("ab"))
        assertEquals(listOf("三"), restored.lookup("abc"))
        assertEquals(listOf("四"), restored.lookup("abcd"))
    }

    @Test
    fun `readFrom returns null for invalid data`() {
        val garbage = byteArrayOf(0, 1, 2, 3, 4, 5)

        val result = TrieSerializer.readFrom(ByteArrayInputStream(garbage))

        assertNull(result)
    }

    @Test
    fun `readFrom returns null for empty input`() {
        val result = TrieSerializer.readFrom(ByteArrayInputStream(byteArrayOf()))

        assertNull(result)
    }
}
