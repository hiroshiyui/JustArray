package com.miyabi_hiroshi.app.justarray.data.dictionary

import org.junit.Assert.*
import org.junit.Test
import java.io.BufferedReader
import java.io.StringReader

class CinParserTest {

    private fun parseString(input: String): List<CinEntry> {
        return CinParser.parse(BufferedReader(StringReader(input)))
    }

    @Test
    fun `parse valid chardef section returns entries`() {
        val input = """
            %chardef begin
            abc 字
            def 詞
            %chardef end
        """.trimIndent()

        val entries = parseString(input)

        assertEquals(2, entries.size)
        assertEquals(CinEntry("abc", "字"), entries[0])
        assertEquals(CinEntry("def", "詞"), entries[1])
    }

    @Test
    fun `lines outside chardef section are ignored`() {
        val input = """
            %gen_inp
            %ename Array30
            %cname 行列30
            abc 外
            %chardef begin
            xyz 內
            %chardef end
            def 外2
        """.trimIndent()

        val entries = parseString(input)

        assertEquals(1, entries.size)
        assertEquals(CinEntry("xyz", "內"), entries[0])
    }

    @Test
    fun `empty lines and malformed lines are skipped`() {
        val input = """
            %chardef begin
            abc 字

            malformed_no_space
            def 詞
            %chardef end
        """.trimIndent()

        val entries = parseString(input)

        assertEquals(2, entries.size)
        assertEquals(CinEntry("abc", "字"), entries[0])
        assertEquals(CinEntry("def", "詞"), entries[1])
    }

    @Test
    fun `multiple chardef sections are parsed`() {
        val input = """
            %chardef begin
            abc 字
            %chardef end
            %chardef begin
            def 詞
            %chardef end
        """.trimIndent()

        val entries = parseString(input)

        assertEquals(2, entries.size)
        assertEquals(CinEntry("abc", "字"), entries[0])
        assertEquals(CinEntry("def", "詞"), entries[1])
    }

    @Test
    fun `empty input returns empty list`() {
        val entries = parseString("")

        assertTrue(entries.isEmpty())
    }

    @Test
    fun `whitespace variations in entries are handled`() {
        val input = """
            %chardef begin
            abc	字
            def  詞 語
            %chardef end
        """.trimIndent()

        val entries = parseString(input)

        assertEquals(2, entries.size)
        assertEquals(CinEntry("abc", "字"), entries[0])
        // limit = 2 means everything after first split is the value
        assertEquals(CinEntry("def", "詞 語"), entries[1])
    }

    @Test
    fun `no chardef section returns empty list`() {
        val input = """
            %gen_inp
            %ename Array30
            abc 字
        """.trimIndent()

        val entries = parseString(input)

        assertTrue(entries.isEmpty())
    }
}
