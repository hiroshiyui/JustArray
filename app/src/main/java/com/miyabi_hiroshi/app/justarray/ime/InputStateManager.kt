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
) {
    private val _state = MutableStateFlow<InputState>(InputState.Idle)
    val state: StateFlow<InputState> = _state.asStateFlow()

    private val _candidates = MutableStateFlow<List<String>>(emptyList())
    val candidates: StateFlow<List<String>> = _candidates.asStateFlow()

    companion object {
        const val MAX_KEYS = 4
        const val CANDIDATES_PER_PAGE = 10
    }

    fun onArrayKey(qwertyChar: Char) {
        when (val current = _state.value) {
            is InputState.Idle -> {
                val newKeys = qwertyChar.toString()
                _state.value = InputState.Composing(keys = newKeys)
                onSetComposingText(KeyCode.toArrayLabels(newKeys))
                lookupCandidates(newKeys)
            }
            is InputState.Composing -> {
                if (current.keys.length < MAX_KEYS) {
                    val newKeys = current.keys + qwertyChar
                    _state.value = InputState.Composing(keys = newKeys)
                    onSetComposingText(KeyCode.toArrayLabels(newKeys))
                    lookupCandidates(newKeys)
                }
                // If already at max keys, ignore additional keystrokes
            }
            is InputState.Selecting -> {
                // Commit first candidate, then start new composition
                commitCandidate(0)
                val newKeys = qwertyChar.toString()
                _state.value = InputState.Composing(keys = newKeys)
                onSetComposingText(KeyCode.toArrayLabels(newKeys))
                lookupCandidates(newKeys)
            }
            is InputState.EnglishMode -> {
                onCommitText(qwertyChar.toString())
            }
            is InputState.SymbolMode -> {
                // ignore array keys in symbol mode
            }
        }
    }

    fun onSpaceKey() {
        when (val current = _state.value) {
            is InputState.Composing -> {
                // Trigger candidate selection
                val candidateList = _candidates.value
                if (candidateList.isNotEmpty()) {
                    _state.value = InputState.Selecting(
                        keys = current.keys,
                        candidates = candidateList,
                        page = 0
                    )
                    onSetComposingText(KeyCode.toArrayLabels(current.keys))
                } else {
                    // No candidates - beep or do nothing
                }
            }
            is InputState.Selecting -> {
                // Next page
                val current2 = _state.value as InputState.Selecting
                val maxPage = (current2.candidates.size - 1) / CANDIDATES_PER_PAGE
                val nextPage = if (current2.page < maxPage) current2.page + 1 else 0
                _state.value = current2.copy(page = nextPage)
            }
            is InputState.Idle -> {
                onCommitText(" ")
            }
            is InputState.EnglishMode -> {
                onCommitText(" ")
            }
            is InputState.SymbolMode -> {
                // handled by symbol keyboard
            }
        }
    }

    fun onBackspaceKey() {
        when (val current = _state.value) {
            is InputState.Composing -> {
                if (current.keys.length > 1) {
                    val newKeys = current.keys.dropLast(1)
                    _state.value = InputState.Composing(keys = newKeys)
                    onSetComposingText(KeyCode.toArrayLabels(newKeys))
                    lookupCandidates(newKeys)
                } else {
                    reset()
                }
            }
            is InputState.Selecting -> {
                // Go back to composing
                _state.value = InputState.Composing(keys = current.keys)
                onSetComposingText(KeyCode.toArrayLabels(current.keys))
            }
            is InputState.Idle, is InputState.EnglishMode -> {
                // Send backspace to editor
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
                // Commit the raw keys as text
                onFinishComposing()
                reset()
            }
            is InputState.Selecting -> {
                commitCandidate(0)
            }
            else -> {
                onCommitText("\n")
            }
        }
    }

    fun onCandidateSelected(index: Int) {
        val current = _state.value
        if (current is InputState.Selecting) {
            val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
            commitCandidate(absoluteIndex)
        }
    }

    fun onNumberKey(number: Int) {
        val current = _state.value
        if (current is InputState.Selecting) {
            // 1-9 maps to indices 0-8, 0 maps to index 9
            val index = if (number == 0) 9 else number - 1
            val absoluteIndex = current.page * CANDIDATES_PER_PAGE + index
            commitCandidate(absoluteIndex)
        } else {
            onCommitText(number.toString())
        }
    }

    fun toggleEnglishMode() {
        _state.value = when (_state.value) {
            is InputState.EnglishMode -> InputState.Idle
            else -> {
                reset()
                InputState.EnglishMode
            }
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
        _state.value = InputState.Idle
        _candidates.value = emptyList()
        onFinishComposing()
    }

    private fun commitCandidate(index: Int) {
        val current = _state.value
        if (current is InputState.Selecting && index in current.candidates.indices) {
            val text = current.candidates[index]
            onFinishComposing()
            onCommitText(text)
            dictionaryRepository.incrementFrequency(current.keys, text)
            _state.value = InputState.Idle
            _candidates.value = emptyList()
        }
    }

    private fun lookupCandidates(keys: String) {
        val results = dictionaryRepository.lookup(keys)
        _candidates.value = results
    }
}
