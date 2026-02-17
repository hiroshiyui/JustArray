package com.miyabi_hiroshi.app.justarray.data.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DictionaryDaoTest {

    private lateinit var database: ArrayDatabase
    private lateinit var dao: DictionaryDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, ArrayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dictionaryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // --- Main dictionary ---

    @Test
    fun insertAndLookupExact() {
        val entries = listOf(
            DictionaryEntry("abc", "字"),
            DictionaryEntry("abc", "詞"),
            DictionaryEntry("def", "語"),
        )
        dao.insertDictionaryEntries(entries)

        val result = dao.lookupExact("abc")

        assertEquals(2, result.size)
        assertTrue(result.all { it.code == "abc" })
    }

    @Test
    fun lookupPrefix() {
        val entries = listOf(
            DictionaryEntry("ab", "字"),
            DictionaryEntry("abc", "詞"),
            DictionaryEntry("abcd", "語"),
            DictionaryEntry("xyz", "他"),
        )
        dao.insertDictionaryEntries(entries)

        val result = dao.lookupPrefix("ab")

        assertEquals(3, result.size)
        assertTrue(result.none { it.code == "xyz" })
    }

    @Test
    fun lookupExactReturnsOrderedByFrequency() {
        val entries = listOf(
            DictionaryEntry("abc", "低", frequency = 1),
            DictionaryEntry("abc", "高", frequency = 10),
            DictionaryEntry("abc", "中", frequency = 5),
        )
        dao.insertDictionaryEntries(entries)

        val result = dao.lookupExact("abc")

        assertEquals("高", result[0].character)
        assertEquals("中", result[1].character)
        assertEquals("低", result[2].character)
    }

    // --- Short codes ---

    @Test
    fun insertAndLookupShortCode() {
        val entries = listOf(
            ShortCodeEntry("1", "的", priority = 1),
            ShortCodeEntry("1", "一", priority = 2),
            ShortCodeEntry("1", "是", priority = 3),
        )
        dao.insertShortCodeEntries(entries)

        val result = dao.lookupShortCode("1")

        assertEquals(3, result.size)
        assertEquals("的", result[0].character)
        assertEquals("一", result[1].character)
        assertEquals("是", result[2].character)
    }

    // --- Special codes ---

    @Test
    fun insertAndLookupSpecialCode() {
        val entries = listOf(
            SpecialCodeEntry("w1", "○"),
            SpecialCodeEntry("w2", "●"),
        )
        dao.insertSpecialCodeEntries(entries)

        val result = dao.lookupSpecialCode("w1")

        assertEquals(1, result.size)
        assertEquals("○", result[0].character)
    }

    // --- User candidates ---

    @Test
    fun incrementUserFrequency() {
        dao.incrementUserFrequency("abc", "字")
        dao.incrementUserFrequency("abc", "字")
        dao.incrementUserFrequency("abc", "詞")

        val result = dao.lookupUserCandidates("abc")

        assertEquals(2, result.size)
        assertEquals("字", result[0].character)
        assertEquals(2, result[0].frequency)
        assertEquals("詞", result[1].character)
        assertEquals(1, result[1].frequency)
    }

    // --- Clear operations ---

    @Test
    fun clearDictionary() {
        dao.insertDictionaryEntries(listOf(DictionaryEntry("abc", "字")))
        dao.clearDictionary()

        assertEquals(0, dao.getDictionaryCount())
    }

    @Test
    fun clearShortCodes() {
        dao.insertShortCodeEntries(listOf(ShortCodeEntry("1", "的")))
        dao.clearShortCodes()

        assertEquals(0, dao.getShortCodeCount())
    }

    @Test
    fun clearSpecialCodes() {
        dao.insertSpecialCodeEntries(listOf(SpecialCodeEntry("w1", "○")))
        dao.clearSpecialCodes()

        assertEquals(0, dao.getSpecialCodeCount())
    }

    @Test
    fun clearUserCandidates() {
        dao.incrementUserFrequency("abc", "字")
        dao.clearUserCandidates()

        val result = dao.lookupUserCandidates("abc")
        assertTrue(result.isEmpty())
    }

    // --- Count operations ---

    @Test
    fun getDictionaryCount() {
        dao.insertDictionaryEntries(listOf(
            DictionaryEntry("abc", "字"),
            DictionaryEntry("def", "詞"),
        ))

        assertEquals(2, dao.getDictionaryCount())
    }

    @Test
    fun getShortCodeCount() {
        dao.insertShortCodeEntries(listOf(
            ShortCodeEntry("1", "的"),
            ShortCodeEntry("2", "一"),
        ))

        assertEquals(2, dao.getShortCodeCount())
    }

    @Test
    fun getSpecialCodeCount() {
        dao.insertSpecialCodeEntries(listOf(
            SpecialCodeEntry("w1", "○"),
        ))

        assertEquals(1, dao.getSpecialCodeCount())
    }
}
