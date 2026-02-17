package com.miyabi_hiroshi.app.justarray.ime

import com.miyabi_hiroshi.app.justarray.data.db.*
import com.miyabi_hiroshi.app.justarray.data.dictionary.ArrayTrie
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictionaryRepository
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InputStateManagerTest {

    private lateinit var manager: InputStateManager
    private lateinit var repo: DictionaryRepository
    private val committed = mutableListOf<String>()
    private val composing = mutableListOf<String>()
    private var finishComposingCount = 0

    private val mainTrie = ArrayTrie().apply {
        // 'a' maps to Array label "1-", use it as a test key
        insert("a", "甲")
        insert("a", "乙")
        insert("a", "丙")
        insert("as", "明")
        insert("asd", "暗")
        insert("asdf", "晦")
        // Many candidates for pagination tests
        (1..25).forEach { i -> insert("fg", "字$i") }
    }

    @Before
    fun setUp() {
        committed.clear()
        composing.clear()
        finishComposingCount = 0

        val fakeDao = FakeDictionaryDao()
        repo = DictionaryRepository(
            dao = fakeDao,
            scope = TestScope(),
            mainTrie = mainTrie,
        )

        manager = InputStateManager(
            dictionaryRepository = repo,
            onCommitText = { committed.add(it) },
            onSetComposingText = { composing.add(it) },
            onFinishComposing = { finishComposingCount++ },
        )
    }

    // --- Idle state ---

    @Test
    fun `onArrayKey from Idle transitions to Composing`() {
        manager.onArrayKey('a')

        val state = manager.state.value
        assertTrue(state is InputState.Composing)
        assertEquals("a", (state as InputState.Composing).keys)
    }

    @Test
    fun `onArrayKey from Idle calls onSetComposingText with array label`() {
        manager.onArrayKey('a')

        // "a" → "1-"
        assertTrue(composing.last().contains("1-"))
    }

    // --- Composing state ---

    @Test
    fun `typing 4 keys reaches MAX_KEYS`() {
        manager.onArrayKey('a')
        manager.onArrayKey('s')
        manager.onArrayKey('d')
        manager.onArrayKey('f')

        val state = manager.state.value as InputState.Composing
        assertEquals("asdf", state.keys)
        assertEquals(4, state.keys.length)
    }

    @Test
    fun `5th key is ignored at MAX_KEYS`() {
        manager.onArrayKey('a')
        manager.onArrayKey('s')
        manager.onArrayKey('d')
        manager.onArrayKey('f')
        manager.onArrayKey('g') // should be ignored

        val state = manager.state.value as InputState.Composing
        assertEquals("asdf", state.keys)
    }

    // --- Composing → Selecting ---

    @Test
    fun `onSpaceKey with multiple candidates transitions to Selecting`() {
        manager.onArrayKey('a') // has 3 candidates: 甲, 乙, 丙

        manager.onSpaceKey()

        val state = manager.state.value
        assertTrue("Expected Selecting but got $state", state is InputState.Selecting)
        assertEquals(listOf("甲", "乙", "丙"), (state as InputState.Selecting).candidates)
    }

    // --- Composing → auto-commit single candidate ---

    @Test
    fun `onSpaceKey with single candidate adds to pre-edit buffer`() {
        // "asdf" has single candidate "晦"
        manager.onArrayKey('a')
        manager.onArrayKey('s')
        manager.onArrayKey('d')
        manager.onArrayKey('f')

        manager.onSpaceKey()

        val state = manager.state.value
        assertTrue("Expected Composing but got $state", state is InputState.Composing)
        val composingState = state as InputState.Composing
        assertEquals("晦", composingState.preEditBuffer)
        assertEquals("", composingState.keys)
    }

    // --- Selecting: candidate selection ---

    @Test
    fun `onCandidateSelected returns to Composing with pre-edit buffer`() {
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting with [甲, 乙, 丙]

        manager.onCandidateSelected(1) // select "乙"

        val state = manager.state.value
        assertTrue(state is InputState.Composing)
        assertEquals("乙", (state as InputState.Composing).preEditBuffer)
        assertEquals("", state.keys)
    }

    @Test
    fun `onNumberKey selects candidate in Selecting state`() {
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting

        manager.onNumberKey(2) // selects index 1 → "乙"

        val state = manager.state.value
        assertTrue(state is InputState.Composing)
        assertEquals("乙", (state as InputState.Composing).preEditBuffer)
    }

    @Test
    fun `onNumberKey 0 selects 10th candidate`() {
        // "fg" has 25 candidates
        manager.onArrayKey('f')
        manager.onArrayKey('g')
        manager.onSpaceKey() // → Selecting

        manager.onNumberKey(0) // index 9 → "字10"

        val state = manager.state.value
        assertTrue(state is InputState.Composing)
        assertEquals("字10", (state as InputState.Composing).preEditBuffer)
    }

    // --- Selecting: backspace ---

    @Test
    fun `onBackspaceKey in Selecting returns to Composing`() {
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting

        manager.onBackspaceKey()

        val state = manager.state.value
        assertTrue(state is InputState.Composing)
        assertEquals("a", (state as InputState.Composing).keys)
    }

    // --- Selecting: pagination ---

    @Test
    fun `nextPage advances and wraps around`() {
        manager.onArrayKey('f')
        manager.onArrayKey('g')
        manager.onSpaceKey() // → Selecting, 25 candidates → 3 pages (0,1,2)

        assertEquals(0, (manager.state.value as InputState.Selecting).page)

        manager.nextPage()
        assertEquals(1, (manager.state.value as InputState.Selecting).page)

        manager.nextPage()
        assertEquals(2, (manager.state.value as InputState.Selecting).page)

        manager.nextPage() // wraps to 0
        assertEquals(0, (manager.state.value as InputState.Selecting).page)
    }

    @Test
    fun `previousPage goes back and wraps around`() {
        manager.onArrayKey('f')
        manager.onArrayKey('g')
        manager.onSpaceKey() // → Selecting, 25 candidates → 3 pages

        assertEquals(0, (manager.state.value as InputState.Selecting).page)

        manager.previousPage() // wraps to last page
        assertEquals(2, (manager.state.value as InputState.Selecting).page)

        manager.previousPage()
        assertEquals(1, (manager.state.value as InputState.Selecting).page)
    }

    // --- Enter key ---

    @Test
    fun `onEnterKey in Composing commits pre-edit and discards keys`() {
        // Build up pre-edit buffer first
        manager.onArrayKey('a')
        manager.onArrayKey('s')
        manager.onArrayKey('d')
        manager.onArrayKey('f')
        manager.onSpaceKey() // single candidate "晦" → pre-edit buffer

        // Now type more keys
        manager.onArrayKey('a')

        committed.clear()
        composing.clear()

        manager.onEnterKey()

        // Should commit pre-edit "晦", discard composing key "a"
        assertTrue(committed.contains("晦"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `onEnterKey in Selecting commits pre-edit plus first candidate`() {
        // Build pre-edit
        manager.onArrayKey('a')
        manager.onArrayKey('s')
        manager.onArrayKey('d')
        manager.onArrayKey('f')
        manager.onSpaceKey() // "晦" in pre-edit

        // Now go to Selecting
        manager.onArrayKey('a')
        manager.onSpaceKey() // Selecting with [甲, 乙, 丙]

        committed.clear()

        manager.onEnterKey()

        // Should commit "晦" + "甲" (first candidate)
        assertTrue(committed.contains("晦甲"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    // --- Reset ---

    @Test
    fun `reset commits pre-edit buffer and goes to Idle`() {
        manager.onArrayKey('a')
        manager.onArrayKey('s')
        manager.onArrayKey('d')
        manager.onArrayKey('f')
        manager.onSpaceKey() // "晦" in pre-edit

        committed.clear()

        manager.reset()

        assertTrue(committed.contains("晦"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    // --- English mode ---

    @Test
    fun `toggleEnglishMode switches to EnglishMode and back`() {
        manager.toggleEnglishMode()
        assertTrue(manager.state.value is InputState.EnglishMode)

        manager.toggleEnglishMode()
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `typing letters in English mode accumulates text`() {
        manager.toggleEnglishMode()

        manager.onArrayKey('h')
        manager.onArrayKey('i')

        val state = manager.state.value as InputState.EnglishMode
        assertEquals("hi", state.typedText)
    }

    @Test
    fun `backspace in English mode removes last letter`() {
        manager.toggleEnglishMode()
        manager.onArrayKey('h')
        manager.onArrayKey('i')

        manager.onBackspaceKey()

        val state = manager.state.value as InputState.EnglishMode
        assertEquals("h", state.typedText)
    }

    @Test
    fun `space in English mode commits typed text`() {
        manager.toggleEnglishMode()
        manager.onArrayKey('h')
        manager.onArrayKey('i')

        committed.clear()
        manager.onSpaceKey()

        // Should have finished composing and committed space
        assertTrue(committed.contains(" "))
    }

    // --- Shift key ---

    @Test
    fun `onShiftKey cycles NONE to SHIFTED to CAPS_LOCK to NONE`() {
        manager.toggleEnglishMode()

        manager.onShiftKey()
        assertEquals(ShiftState.SHIFTED, (manager.state.value as InputState.EnglishMode).shiftState)

        manager.onShiftKey()
        assertEquals(ShiftState.CAPS_LOCK, (manager.state.value as InputState.EnglishMode).shiftState)

        manager.onShiftKey()
        assertEquals(ShiftState.NONE, (manager.state.value as InputState.EnglishMode).shiftState)
    }

    @Test
    fun `SHIFTED mode auto-reverts to NONE after typing a letter`() {
        manager.toggleEnglishMode()
        manager.onShiftKey() // → SHIFTED

        manager.onArrayKey('h')

        val state = manager.state.value as InputState.EnglishMode
        assertEquals("H", state.typedText)
        assertEquals(ShiftState.NONE, state.shiftState)
    }

    @Test
    fun `CAPS_LOCK stays active after typing letters`() {
        manager.toggleEnglishMode()
        manager.onShiftKey() // → SHIFTED
        manager.onShiftKey() // → CAPS_LOCK

        manager.onArrayKey('h')
        manager.onArrayKey('i')

        val state = manager.state.value as InputState.EnglishMode
        assertEquals("HI", state.typedText)
        assertEquals(ShiftState.CAPS_LOCK, state.shiftState)
    }

    @Test
    fun `shift has no effect outside EnglishMode`() {
        manager.onShiftKey() // in Idle, should do nothing
        assertEquals(InputState.Idle, manager.state.value)
    }

    // --- Symbol mode ---

    @Test
    fun `toggleSymbolMode switches to SymbolMode and back`() {
        manager.toggleSymbolMode()
        assertTrue(manager.state.value is InputState.SymbolMode)

        manager.toggleSymbolMode()
        assertEquals(InputState.Idle, manager.state.value)
    }

    // --- Password field ---

    @Test
    fun `password field in English mode directly commits characters`() {
        manager.toggleEnglishMode()
        // TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD = 0x081
        manager.updateInputTypeClass(0x081)

        committed.clear()
        manager.onArrayKey('a')
        manager.onArrayKey('b')

        assertEquals(listOf("a", "b"), committed)
        // typedText should remain empty since chars are committed directly
        val state = manager.state.value as InputState.EnglishMode
        assertEquals("", state.typedText)
    }

    @Test
    fun `password field visible password variant`() {
        manager.toggleEnglishMode()
        // TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 0x091
        manager.updateInputTypeClass(0x091)

        committed.clear()
        manager.onArrayKey('x')

        assertEquals(listOf("x"), committed)
    }

    // --- Array key in Selecting starts new composition ---

    @Test
    fun `onArrayKey in Selecting adds first candidate to pre-edit and starts new keys`() {
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting [甲, 乙, 丙]

        manager.onArrayKey('s') // should auto-select first candidate "甲"

        val state = manager.state.value
        assertTrue(state is InputState.Composing)
        val composingState = state as InputState.Composing
        assertEquals("甲", composingState.preEditBuffer)
        assertEquals("s", composingState.keys)
    }

    /** Minimal fake DAO for tests. */
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
