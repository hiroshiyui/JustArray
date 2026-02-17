package com.miyabi_hiroshi.app.justarray.ime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictionaryRepository

class InputStateManager(
    private val dictionaryRepository: DictionaryRepository,
    private val onCommitText: (String) -> Unit,
    private val onSetComposingText: (String) -> Unit,
    private val onFinishComposing: () -> Unit,
    private val onPerformEditorAction: (Int) -> Unit = {},
) {
    private val _state = MutableStateFlow<InputState>(InputState.Idle)
    val state: StateFlow<InputState> = _state.asStateFlow()

    private val _candidates = MutableStateFlow<List<String>>(emptyList())
    val candidates: StateFlow<List<String>> = _candidates.asStateFlow()

    private val _inputTypeClass = MutableStateFlow(0)
    val inputTypeClass: StateFlow<Int> = _inputTypeClass.asStateFlow()

    private val _imeAction = MutableStateFlow(0)
    val imeAction: StateFlow<Int> = _imeAction.asStateFlow()

    private var isPasswordField = false

    fun updateInputTypeClass(inputType: Int) {
        _inputTypeClass.value = inputType and 0x0000000f // TYPE_MASK_CLASS
        // Detect password fields (TYPE_MASK_CLASS | TYPE_MASK_VARIATION)
        val classAndVariation = inputType and 0x00000fff
        isPasswordField = classAndVariation in setOf(
            0x081, // TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD
            0x091, // TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            0x0C1, // TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_WEB_PASSWORD
            0x012, // TYPE_CLASS_NUMBER | TYPE_NUMBER_VARIATION_PASSWORD
        )
    }

    fun updateImeOptions(imeOptions: Int) {
        _imeAction.value = imeOptions and 0x000000ff // IME_MASK_ACTION
    }

    fun commitText(text: String) {
        onCommitText(text)
    }

    companion object {
        const val MAX_KEYS = 4
        const val CANDIDATES_PER_PAGE = 10
    }

    /** Clear the composing region without committing its content to the text field. */
    private fun discardComposingText() {
        onSetComposingText("")
        onFinishComposing()
    }

    /** Update the composing region to show pre-edit buffer + 字根 labels inline in the text field. */
    private fun updateComposingText(preEditBuffer: String, keys: String) {
        val arrayLabels = if (keys.isNotEmpty()) KeyCode.toArrayLabels(keys) else ""
        val composingText = preEditBuffer + arrayLabels
        if (composingText.isNotEmpty()) {
            onSetComposingText(composingText)
        }
    }

    fun onArrayKey(qwertyChar: Char) {
        when (val current = _state.value) {
            is InputState.Idle -> {
                val newKeys = qwertyChar.toString()
                _state.value = InputState.Composing(keys = newKeys)
                updateComposingText("", newKeys)
                lookupCandidates(newKeys)
            }
            is InputState.Composing -> {
                if (current.keys.length < MAX_KEYS) {
                    val newKeys = current.keys + qwertyChar
                    _state.value = current.copy(keys = newKeys, page = 0)
                    updateComposingText(current.preEditBuffer, newKeys)
                    lookupCandidates(newKeys)
                }
                // If already at max keys, ignore additional keystrokes
            }
            is InputState.Selecting -> {
                // Add first candidate to pre-edit buffer, then start new composition
                val firstCandidate = current.candidates.getOrNull(0) ?: ""
                val newPreEdit = current.preEditBuffer + firstCandidate
                if (firstCandidate.isNotEmpty()) {
                    dictionaryRepository.incrementFrequency(current.keys, firstCandidate)
                }
                val newKeys = qwertyChar.toString()
                _state.value = InputState.Composing(keys = newKeys, preEditBuffer = newPreEdit)
                _candidates.value = emptyList()
                updateComposingText(newPreEdit, newKeys)
                lookupCandidates(newKeys)
            }
            is InputState.EnglishMode -> {
                if (isPasswordField) {
                    val ch = applyShift(qwertyChar, current.shiftState)
                    onCommitText(ch.toString())
                    if (current.shiftState == ShiftState.SHIFTED) {
                        _state.value = current.copy(shiftState = ShiftState.NONE)
                    }
                } else if (qwertyChar.isLetter()) {
                    val ch = applyShift(qwertyChar, current.shiftState)
                    val newText = current.typedText + ch
                    onSetComposingText(newText)
                    val predictions = dictionaryRepository.englishPrefixLookup(newText)
                    val newShift = if (current.shiftState == ShiftState.SHIFTED) ShiftState.NONE else current.shiftState
                    _state.value = current.copy(typedText = newText, candidates = predictions, page = 0, shiftState = newShift)
                } else {
                    // Punctuation (;,./): commit pending text, then commit punctuation
                    if (current.typedText.isNotEmpty()) {
                        onFinishComposing()
                    }
                    onCommitText(qwertyChar.toString())
                    _state.value = InputState.EnglishMode(shiftState = current.shiftState)
                }
            }
            is InputState.SymbolMode -> {
                // ignore array keys in symbol mode
            }
        }
    }

    fun onSpaceKey() {
        when (val current = _state.value) {
            is InputState.Composing -> {
                if (current.keys.isNotEmpty()) {
                    // Trigger candidate selection
                    val candidateList = _candidates.value
                    if (candidateList.size == 1) {
                        // Single candidate — add to pre-edit buffer
                        val selected = candidateList[0]
                        val newPreEdit = current.preEditBuffer + selected
                        dictionaryRepository.incrementFrequency(current.keys, selected)
                        _state.value = InputState.Composing(keys = "", preEditBuffer = newPreEdit)
                        _candidates.value = emptyList()
                        updateComposingText(newPreEdit, "")
                    } else if (candidateList.size > 1) {
                        _state.value = InputState.Selecting(
                            keys = current.keys,
                            candidates = candidateList,
                            page = 0,
                            preEditBuffer = current.preEditBuffer,
                        )
                        updateComposingText(current.preEditBuffer, current.keys)
                    } else {
                        // No candidates - beep or do nothing
                    }
                } else if (current.preEditBuffer.isNotEmpty()) {
                    // No composing keys — commit pre-edit buffer + space
                    discardComposingText()
                    onCommitText(current.preEditBuffer)
                    onCommitText(" ")
                    _state.value = InputState.Idle
                    _candidates.value = emptyList()
                }
            }
            is InputState.Selecting -> {
                nextPage()
            }
            is InputState.Idle -> {
                onCommitText(" ")
            }
            is InputState.EnglishMode -> {
                if (isPasswordField) {
                    onCommitText(" ")
                } else {
                    if (current.typedText.isNotEmpty()) {
                        onFinishComposing()
                    }
                    onCommitText(" ")
                    _state.value = InputState.EnglishMode()
                }
            }
            is InputState.SymbolMode -> {
                // handled by symbol keyboard
            }
        }
    }

    fun onBackspaceKey() {
        when (val current = _state.value) {
            is InputState.Composing -> {
                if (current.keys.isNotEmpty()) {
                    // Delete last composing key
                    val newKeys = current.keys.dropLast(1)
                    if (newKeys.isNotEmpty()) {
                        _state.value = current.copy(keys = newKeys, page = 0)
                        updateComposingText(current.preEditBuffer, newKeys)
                        lookupCandidates(newKeys)
                    } else if (current.preEditBuffer.isNotEmpty()) {
                        // Removed last key but pre-edit buffer remains
                        _state.value = InputState.Composing(keys = "", preEditBuffer = current.preEditBuffer)
                        updateComposingText(current.preEditBuffer, "")
                        _candidates.value = emptyList()
                    } else {
                        reset()
                    }
                } else if (current.preEditBuffer.isNotEmpty()) {
                    // No keys — delete last character from pre-edit buffer
                    val newPreEdit = current.preEditBuffer.dropLast(1)
                    if (newPreEdit.isNotEmpty()) {
                        _state.value = InputState.Composing(keys = "", preEditBuffer = newPreEdit)
                        updateComposingText(newPreEdit, "")
                    } else {
                        reset()
                    }
                } else {
                    // Both empty
                    onCommitText("\b")
                }
            }
            is InputState.Selecting -> {
                // Go back to composing, preserving pre-edit buffer
                _state.value = InputState.Composing(keys = current.keys, preEditBuffer = current.preEditBuffer)
                updateComposingText(current.preEditBuffer, current.keys)
            }
            is InputState.EnglishMode -> {
                if (isPasswordField) {
                    onCommitText("\b")
                } else if (current.typedText.isNotEmpty()) {
                    val newText = current.typedText.dropLast(1)
                    if (newText.isNotEmpty()) {
                        onSetComposingText(newText)
                        val predictions = dictionaryRepository.englishPrefixLookup(newText)
                        _state.value = current.copy(typedText = newText, candidates = predictions, page = 0)
                    } else {
                        discardComposingText()
                        _state.value = InputState.EnglishMode()
                    }
                } else {
                    onCommitText("\b")
                }
            }
            is InputState.Idle -> {
                onCommitText("\b")
            }
            is InputState.SymbolMode -> {
                _state.value = InputState.Idle
            }
        }
    }

    fun onEnterKey() {
        when (val current = _state.value) {
            is InputState.Composing -> {
                // Commit pre-edit buffer (discard composing keys)
                discardComposingText()
                if (current.preEditBuffer.isNotEmpty()) {
                    onCommitText(current.preEditBuffer)
                }
                _state.value = InputState.Idle
                _candidates.value = emptyList()
            }
            is InputState.Selecting -> {
                // Add first candidate to pre-edit, then commit all
                val firstCandidate = current.candidates.getOrNull(0) ?: ""
                val textToCommit = current.preEditBuffer + firstCandidate
                if (firstCandidate.isNotEmpty()) {
                    dictionaryRepository.incrementFrequency(current.keys, firstCandidate)
                }
                discardComposingText()
                if (textToCommit.isNotEmpty()) {
                    onCommitText(textToCommit)
                }
                _state.value = InputState.Idle
                _candidates.value = emptyList()
            }
            is InputState.EnglishMode -> {
                if (isPasswordField || current.typedText.isEmpty()) {
                    val action = _imeAction.value
                    if (action > 1) {
                        onPerformEditorAction(action)
                    } else {
                        onCommitText("\n")
                    }
                } else {
                    onFinishComposing()
                    _state.value = InputState.EnglishMode()
                }
            }
            else -> {
                val action = _imeAction.value
                if (action > 1) { // IME_ACTION_GO (2) and above
                    onPerformEditorAction(action)
                } else {
                    onCommitText("\n")
                }
            }
        }
    }

    fun onCandidateSelected(index: Int) {
        val current = _state.value
        if (current is InputState.Selecting) {
            val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
            selectCandidate(absoluteIndex)
        }
    }

    fun onNumberKey(number: Int) {
        when (val current = _state.value) {
            is InputState.Selecting -> {
                // 1-9 maps to indices 0-8, 0 maps to index 9
                val index = if (number == 0) 9 else number - 1
                val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
                selectCandidate(absoluteIndex)
            }
            is InputState.Composing -> {
                if (current.keys.isNotEmpty() && current.keys.length < MAX_KEYS) {
                    // Append digit to composing keys (e.g. w1, w2 for symbols)
                    val newKeys = current.keys + number.toString()
                    _state.value = current.copy(keys = newKeys)
                    updateComposingText(current.preEditBuffer, newKeys)
                    lookupCandidates(newKeys)
                } else {
                    onCommitText(number.toString())
                }
            }
            is InputState.EnglishMode -> {
                if (isPasswordField) {
                    onCommitText(number.toString())
                } else if (current.candidates.isNotEmpty()) {
                    val index = if (number == 0) 9 else number - 1
                    val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
                    if (absoluteIndex in current.candidates.indices) {
                        val selected = current.candidates[absoluteIndex]
                        discardComposingText()
                        onCommitText(selected)
                        onCommitText(" ")
                        _state.value = InputState.EnglishMode()
                    }
                } else {
                    if (current.typedText.isNotEmpty()) {
                        onFinishComposing()
                    }
                    onCommitText(number.toString())
                    _state.value = InputState.EnglishMode()
                }
            }
            else -> {
                onCommitText(number.toString())
            }
        }
    }

    fun nextPage() {
        val current = _state.value
        if (current is InputState.Selecting) {
            val maxPage = (current.candidates.size - 1) / CANDIDATES_PER_PAGE
            val nextPage = if (current.page < maxPage) current.page + 1 else 0
            _state.value = current.copy(page = nextPage)
        }
    }

    fun previousPage() {
        val current = _state.value
        if (current is InputState.Selecting) {
            val maxPage = (current.candidates.size - 1) / CANDIDATES_PER_PAGE
            val prevPage = if (current.page > 0) current.page - 1 else maxPage
            _state.value = current.copy(page = prevPage)
        }
    }

    fun onComposingCandidateSelected(index: Int) {
        val current = _state.value
        if (current is InputState.Composing) {
            val candidateList = _candidates.value
            val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
            if (absoluteIndex in candidateList.indices) {
                val selectedText = candidateList[absoluteIndex]
                val newPreEdit = current.preEditBuffer + selectedText
                dictionaryRepository.incrementFrequency(current.keys, selectedText)
                _state.value = InputState.Composing(keys = "", preEditBuffer = newPreEdit)
                _candidates.value = emptyList()
                updateComposingText(newPreEdit, "")
            }
        }
    }

    fun composingNextPage() {
        val current = _state.value
        if (current is InputState.Composing) {
            val maxPage = (_candidates.value.size - 1) / CANDIDATES_PER_PAGE
            val nextPage = if (current.page < maxPage) current.page + 1 else 0
            _state.value = current.copy(page = nextPage)
        }
    }

    fun composingPreviousPage() {
        val current = _state.value
        if (current is InputState.Composing) {
            val maxPage = (_candidates.value.size - 1) / CANDIDATES_PER_PAGE
            val prevPage = if (current.page > 0) current.page - 1 else maxPage
            _state.value = current.copy(page = prevPage)
        }
    }

    fun onEnglishCandidateSelected(index: Int) {
        val current = _state.value
        if (current is InputState.EnglishMode && current.candidates.isNotEmpty()) {
            val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
            if (absoluteIndex in current.candidates.indices) {
                val selected = current.candidates[absoluteIndex]
                discardComposingText()
                onCommitText(selected)
                onCommitText(" ")
                _state.value = InputState.EnglishMode()
            }
        }
    }

    fun englishNextPage() {
        val current = _state.value
        if (current is InputState.EnglishMode && current.candidates.isNotEmpty()) {
            val maxPage = (current.candidates.size - 1) / CANDIDATES_PER_PAGE
            val nextPage = if (current.page < maxPage) current.page + 1 else 0
            _state.value = current.copy(page = nextPage)
        }
    }

    fun englishPreviousPage() {
        val current = _state.value
        if (current is InputState.EnglishMode && current.candidates.isNotEmpty()) {
            val maxPage = (current.candidates.size - 1) / CANDIDATES_PER_PAGE
            val prevPage = if (current.page > 0) current.page - 1 else maxPage
            _state.value = current.copy(page = prevPage)
        }
    }

    fun onShiftKey() {
        val current = _state.value
        if (current is InputState.EnglishMode) {
            val next = when (current.shiftState) {
                ShiftState.NONE -> ShiftState.SHIFTED
                ShiftState.SHIFTED -> ShiftState.CAPS_LOCK
                ShiftState.CAPS_LOCK -> ShiftState.NONE
            }
            _state.value = current.copy(shiftState = next)
        }
    }

    fun onSwipeUpKey(qwertyChar: Char) {
        when (val current = _state.value) {
            is InputState.EnglishMode -> {
                if (isPasswordField) {
                    onCommitText(qwertyChar.uppercaseChar().toString())
                } else {
                    if (current.typedText.isNotEmpty()) {
                        onFinishComposing()
                        _state.value = InputState.EnglishMode(shiftState = current.shiftState)
                    }
                    onCommitText(qwertyChar.uppercaseChar().toString())
                }
            }
            is InputState.Idle -> {
                onCommitText(qwertyChar.uppercaseChar().toString())
            }
            else -> { /* ignored in other states */ }
        }
    }

    private fun applyShift(char: Char, shiftState: ShiftState): Char {
        return if (shiftState != ShiftState.NONE && char.isLetter()) char.uppercaseChar() else char
    }

    fun toggleEnglishMode() {
        val current = _state.value
        if (current is InputState.EnglishMode) {
            if (current.typedText.isNotEmpty()) {
                onFinishComposing()
            }
            _state.value = InputState.Idle
        } else {
            reset()
            _state.value = InputState.EnglishMode()
        }
    }

    fun toggleSymbolMode() {
        _state.value = when (_state.value) {
            is InputState.SymbolMode -> InputState.Idle
            else -> {
                reset()
                InputState.SymbolMode()
            }
        }
    }

    fun onSymbolSelected(symbol: String) {
        onCommitText(symbol)
    }

    fun reset() {
        val current = _state.value
        when (current) {
            is InputState.EnglishMode -> {
                if (current.typedText.isNotEmpty()) {
                    discardComposingText()
                }
                _state.value = InputState.Idle
                return
            }
            else -> {
                val preEdit = when (current) {
                    is InputState.Composing -> current.preEditBuffer
                    is InputState.Selecting -> current.preEditBuffer
                    else -> ""
                }
                discardComposingText()
                if (preEdit.isNotEmpty()) {
                    onCommitText(preEdit)
                }
                _state.value = InputState.Idle
                _candidates.value = emptyList()
            }
        }
    }

    private fun selectCandidate(index: Int) {
        val current = _state.value
        if (current is InputState.Selecting && index in current.candidates.indices) {
            val selectedText = current.candidates[index]
            val newPreEdit = current.preEditBuffer + selectedText
            dictionaryRepository.incrementFrequency(current.keys, selectedText)
            _state.value = InputState.Composing(keys = "", preEditBuffer = newPreEdit)
            _candidates.value = emptyList()
            updateComposingText(newPreEdit, "")
        }
    }

    private fun lookupCandidates(keys: String) {
        val results = dictionaryRepository.lookup(keys)
        _candidates.value = results
    }
}
