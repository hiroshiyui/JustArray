package com.miyabi_hiroshi.app.justarray.data.dictionary

import com.miyabi_hiroshi.app.justarray.data.db.*
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DictionaryRepositoryTest {

    private lateinit var repo: DictionaryRepository
    private lateinit var fakeDao: FakeDictionaryDao

    private val mainTrie = ArrayTrie().apply {
        insert("abc", "主")
        insert("abc", "注")
        insert("de", "的")
    }

    private val shortTrie = ArrayTrie().apply {
        insert("de", "得")
        insert("de", "的") // duplicate with main
    }

    private val specialTrie = ArrayTrie().apply {
        insert("abc", "特")
        insert("de", "地")
    }

    @Before
    fun setUp() {
        fakeDao = FakeDictionaryDao()
        repo = DictionaryRepository(
            dao = fakeDao,
            scope = TestScope(),
            mainTrie = mainTrie,
            shortTrie = shortTrie,
            specialTrie = specialTrie,
        )
        repo.isLoaded // not set via setTries, but tries are injected via constructor
    }

    @Test
    fun `lookup priority is special then short then main without duplicates`() {
        val results = repo.lookup("de")

        // special "地" first, then short "得", then "的" (not duplicated from main)
        assertEquals(listOf("地", "得", "的"), results)
    }

    @Test
    fun `useShortCodes false skips short codes`() {
        repo.useShortCodes = false

        val results = repo.lookup("de")

        // special "地", then main "的" (no short codes)
        assertEquals(listOf("地", "的"), results)
    }

    @Test
    fun `useSpecialCodes false skips special codes`() {
        repo.useSpecialCodes = false

        val results = repo.lookup("de")

        // short "得" and "的", then main adds nothing new
        assertEquals(listOf("得", "的"), results)
    }

    @Test
    fun `short codes only apply for 1-2 key inputs`() {
        // "abc" is 3 keys, so short codes should not apply
        val results = repo.lookup("abc")

        // special "特", then main "主" and "注" (no short lookup for 3-key code)
        assertEquals(listOf("特", "主", "注"), results)
    }

    @Test
    fun `lookup empty code returns empty list`() {
        assertEquals(emptyList<String>(), repo.lookup(""))
    }

    @Test
    fun `englishPrefixLookup delegates to english trie`() {
        val englishTrie = ArrayTrie().apply {
            insert("hel", "hello")
            insert("help", "help")
            insert("help", "helper")
        }
        repo.setEnglishTrie(englishTrie)

        val results = repo.englishPrefixLookup("hel")

        assertTrue("hello" in results)
        assertTrue("help" in results)
        assertTrue("helper" in results)
    }

    @Test
    fun `englishPrefixLookup with empty prefix returns empty`() {
        assertEquals(emptyList<String>(), repo.englishPrefixLookup(""))
    }

    @Test
    fun `isLoaded is false initially and true after setTries`() {
        val freshRepo = DictionaryRepository(dao = fakeDao, scope = TestScope())

        assertFalse(freshRepo.isLoaded)

        freshRepo.setTries(ArrayTrie(), ArrayTrie(), ArrayTrie())

        assertTrue(freshRepo.isLoaded)
    }

    /** Minimal fake that satisfies the DictionaryDao interface for unit tests. */
    private class FakeDictionaryDao : DictionaryDao {
        override fun lookupExact(code: String) = emptyList<DictionaryEntry>()
        override fun lookupPrefix(prefix: String) = emptyList<DictionaryEntry>()
        override fun insertDictionaryEntries(entries: List<DictionaryEntry>) {}
        override fun incrementFrequency(code: String, character: String) {}
        override fun lookupShortCode(code: String) = emptyList<ShortCodeEntry>()
        override fun insertShortCodeEntries(entries: List<ShortCodeEntry>) {}
        override fun lookupSpecialCode(code: String) = emptyList<SpecialCodeEntry>()
        override fun insertSpecialCodeEntries(entries: List<SpecialCodeEntry>) {}
        override fun lookupUserCandidates(code: String) = emptyList<UserCandidate>()
        override fun incrementUserFrequency(code: String, character: String) {}
        override fun clearUserCandidates() {}
        override fun clearDictionary() {}
        override fun clearShortCodes() {}
        override fun clearSpecialCodes() {}
        override fun lookupUserPhrases(code: String) = emptyList<UserPhrase>()
        override fun insertUserPhrase(userPhrase: UserPhrase) {}
        override fun deleteUserPhrase(code: String, phrase: String) {}
        override fun getAllUserPhrases() = emptyList<UserPhrase>()
        override fun getDictionaryCount() = 0
        override fun getShortCodeCount() = 0
        override fun getSpecialCodeCount() = 0
    }
}
