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
        // ≥2 candidates at each intermediate level to prevent premature auto-select
        insert("as", "明")
        insert("as", "暝")
        insert("asd", "暗")
        insert("asd", "闇")
        insert("asdf", "晦")
        insert("asdf", "誨")
        // Single candidate codes — auto-select immediately on keystroke
        insert("q", "球")
        insert("w", "我")
        // Single candidate 2-key code (first key "a" has multiple, so no premature auto-select)
        insert("az", "雜")
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

    // --- Composing → auto-select single candidate on keystroke ---

    @Test
    fun `single candidate auto-selects on keystroke`() {
        // "q" has single candidate "球" — auto-selects immediately
        manager.onArrayKey('q')

        val state = manager.state.value
        assertTrue("Expected Composing but got $state", state is InputState.Composing)
        val composingState = state as InputState.Composing
        assertEquals("球", composingState.preEditBuffer)
        assertEquals("", composingState.keys)
    }

    @Test
    fun `auto-select after multiple keystrokes when final key narrows to one`() {
        // "a" has 3 candidates (no auto-select), "az" has 1 candidate (auto-selects)
        manager.onArrayKey('a')
        val afterA = manager.state.value as InputState.Composing
        assertEquals("a", afterA.keys)
        assertEquals("", afterA.preEditBuffer)

        manager.onArrayKey('z')
        val state = manager.state.value as InputState.Composing
        assertEquals("雜", state.preEditBuffer)
        assertEquals("", state.keys)
    }

    @Test
    fun `no auto-select with multiple candidates`() {
        manager.onArrayKey('a') // 3 candidates

        val state = manager.state.value as InputState.Composing
        assertEquals("a", state.keys)
        assertEquals("", state.preEditBuffer)
        assertEquals(3, manager.candidates.value.size)
    }

    @Test
    fun `no auto-select with zero candidates`() {
        manager.onArrayKey('z') // no entries for just "z"

        val state = manager.state.value as InputState.Composing
        assertEquals("z", state.keys)
        assertEquals("", state.preEditBuffer)
        assertTrue(manager.candidates.value.isEmpty())
    }

    @Test
    fun `auto-select appends to existing pre-edit buffer`() {
        // Manually select to build pre-edit first
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting
        manager.onCandidateSelected(0) // 甲 → preEdit="甲"

        // Now type 'q' → auto-selects "球", appends to pre-edit
        manager.onArrayKey('q')
        val state = manager.state.value as InputState.Composing
        assertEquals("甲球", state.preEditBuffer)
        assertEquals("", state.keys)
    }

    @Test
    fun `consecutive auto-selects accumulate in pre-edit`() {
        manager.onArrayKey('q') // auto-selects "球"
        manager.onArrayKey('w') // auto-selects "我"

        val state = manager.state.value as InputState.Composing
        assertEquals("球我", state.preEditBuffer)
        assertEquals("", state.keys)
    }

    @Test
    fun `auto-select from Selecting state via new single-candidate key`() {
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting [甲, 乙, 丙]

        // Type 'q' in Selecting: first candidate "甲" added to pre-edit,
        // then lookup("q") → 1 candidate "球" → auto-selects too
        manager.onArrayKey('q')

        val state = manager.state.value as InputState.Composing
        assertEquals("甲球", state.preEditBuffer)
        assertEquals("", state.keys)
    }

    @Test
    fun `auto-select clears candidate list`() {
        manager.onArrayKey('q') // auto-selects "球"

        assertTrue(manager.candidates.value.isEmpty())
    }

    @Test
    fun `auto-select updates composing text with pre-edit content`() {
        composing.clear()
        manager.onArrayKey('q') // auto-selects "球"

        // Composing text should include the pre-edit buffer character
        assertTrue(composing.any { it.contains("球") })
    }

    @Test
    fun `space after auto-select commits pre-edit with space`() {
        manager.onArrayKey('q') // auto-selects "球" → preEdit="球", keys=""

        committed.clear()
        manager.onSpaceKey()

        // Space with empty keys and non-empty pre-edit → commit pre-edit + space
        assertTrue(committed.contains("球"))
        assertTrue(committed.contains(" "))
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `enter after auto-select commits pre-edit`() {
        manager.onArrayKey('q') // auto-selects "球"

        committed.clear()
        manager.onEnterKey()

        assertTrue(committed.contains("球"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `typing continues normally after auto-select`() {
        manager.onArrayKey('q') // auto-selects "球" → preEdit="球", keys=""

        // Continue typing a multi-candidate code
        manager.onArrayKey('a')
        val state = manager.state.value as InputState.Composing
        assertEquals("球", state.preEditBuffer)
        assertEquals("a", state.keys)
        assertEquals(3, manager.candidates.value.size)
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
        // Build up pre-edit buffer via auto-select
        manager.onArrayKey('q') // single candidate "球" → auto-selected into pre-edit

        // Now type more keys
        manager.onArrayKey('a')

        committed.clear()
        composing.clear()

        manager.onEnterKey()

        // Should commit pre-edit "球", discard composing key "a"
        assertTrue(committed.contains("球"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `onEnterKey in Selecting commits pre-edit plus first candidate`() {
        // Build pre-edit via auto-select
        manager.onArrayKey('q') // "球" in pre-edit

        // Now go to Selecting
        manager.onArrayKey('a')
        manager.onSpaceKey() // Selecting with [甲, 乙, 丙]

        committed.clear()

        manager.onEnterKey()

        // Should commit "球" + "甲" (first candidate)
        assertTrue(committed.contains("球甲"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    // --- Reset ---

    @Test
    fun `reset commits pre-edit buffer and goes to Idle`() {
        manager.onArrayKey('q') // "球" auto-selected into pre-edit

        committed.clear()

        manager.reset()

        assertTrue(committed.contains("球"))
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

    // --- Multi-step integration tests ---

    @Test
    fun `compose select continue composing then commit via enter`() {
        // Step 1: Type "a" → Composing with candidates [甲, 乙, 丙]
        manager.onArrayKey('a')
        assertTrue(manager.state.value is InputState.Composing)

        // Step 2: Space → Selecting
        manager.onSpaceKey()
        assertTrue(manager.state.value is InputState.Selecting)

        // Step 3: Select candidate 0 (甲) → Composing with preEditBuffer="甲"
        manager.onCandidateSelected(0)
        val afterSelect = manager.state.value as InputState.Composing
        assertEquals("甲", afterSelect.preEditBuffer)
        assertEquals("", afterSelect.keys)

        // Step 4: Type "q" → single candidate "球" auto-selects → preEditBuffer="甲球"
        manager.onArrayKey('q')
        val afterAutoSelect = manager.state.value as InputState.Composing
        assertEquals("甲球", afterAutoSelect.preEditBuffer)
        assertEquals("", afterAutoSelect.keys)

        // Step 5: Enter commits the full pre-edit buffer
        committed.clear()
        manager.onEnterKey()
        assertTrue(committed.contains("甲球"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `compose select via number then type next key auto-selects first`() {
        // Type "a", space → Selecting [甲, 乙, 丙]
        manager.onArrayKey('a')
        manager.onSpaceKey()

        // Select 乙 via number key 2
        manager.onNumberKey(2)
        val afterNumber = manager.state.value as InputState.Composing
        assertEquals("乙", afterNumber.preEditBuffer)

        // Type next key while in Composing with pre-edit
        manager.onArrayKey('a')
        manager.onSpaceKey() // → Selecting again

        // Now type another array key → auto-selects first candidate and starts new composition.
        // First candidate is "乙" (not "甲") because the earlier selection boosted its frequency.
        manager.onArrayKey('a')
        val afterAutoSelect = manager.state.value as InputState.Composing
        assertEquals("乙乙", afterAutoSelect.preEditBuffer)
        assertEquals("a", afterAutoSelect.keys)
    }

    @Test
    fun `compose backspace through pre-edit buffer to idle`() {
        // Build up pre-edit: type "q" → single candidate "球" auto-selected on keystroke
        manager.onArrayKey('q')

        val afterAutoSelect = manager.state.value as InputState.Composing
        assertEquals("球", afterAutoSelect.preEditBuffer)
        assertEquals("", afterAutoSelect.keys)

        // Backspace removes last char from preEditBuffer
        manager.onBackspaceKey()
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `compose select paginate then select from page 2`() {
        // "fg" has 25 candidates → 3 pages
        manager.onArrayKey('f')
        manager.onArrayKey('g')
        manager.onSpaceKey() // → Selecting

        // Go to page 2
        manager.nextPage()
        assertEquals(1, (manager.state.value as InputState.Selecting).page)

        // Select candidate 1 on page 2 (index 0 on page 2 = absolute index 10 = "字11")
        manager.onCandidateSelected(0)

        val state = manager.state.value as InputState.Composing
        assertEquals("字11", state.preEditBuffer)
    }

    @Test
    fun `compose reset with pre-edit commits pre-edit then idle`() {
        // Build pre-edit via auto-select, then start composing more
        manager.onArrayKey('q') // "球" in pre-edit
        manager.onArrayKey('a') // start composing "a"

        committed.clear()
        manager.reset()

        assertTrue(committed.contains("球"))
        assertEquals(InputState.Idle, manager.state.value)
    }

    @Test
    fun `full workflow compose select compose select enter`() {
        // First character: type "a", space, select 丙 (index 2)
        manager.onArrayKey('a')
        manager.onSpaceKey()
        manager.onCandidateSelected(2)
        assertEquals("丙", (manager.state.value as InputState.Composing).preEditBuffer)

        // Second character: type "q" → single candidate "球", auto-selects on keystroke
        manager.onArrayKey('q')
        assertEquals("丙球", (manager.state.value as InputState.Composing).preEditBuffer)

        // Commit with Enter
        committed.clear()
        manager.onEnterKey()
        assertTrue(committed.contains("丙球"))
        assertEquals(InputState.Idle, manager.state.value)
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
        override fun getEnglishWordFrequencies(words: List<String>) = emptyList<EnglishWordFrequency>()
        override fun incrementEnglishWordFrequency(word: String) {}
        override fun clearEnglishWordFrequencies() {}
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
