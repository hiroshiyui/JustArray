package com.miyabi_hiroshi.app.justarray.ui.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.justarray.ime.InputState
import com.miyabi_hiroshi.app.justarray.ime.InputStateManager
import com.miyabi_hiroshi.app.justarray.ui.candidate.CandidateBar

import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun KeyboardScreen(
    inputStateManager: InputStateManager,
    showArrayLabels: Boolean = true,
    onKeyPress: () -> Unit = {},
    onSwitchIme: () -> Unit = {},
) {
    val state by inputStateManager.state.collectAsState()
    val candidates by inputStateManager.candidates.collectAsState()
    val inputTypeClass by inputStateManager.inputTypeClass.collectAsState()
    val imeAction by inputStateManager.imeAction.collectAsState()

    val isNumberField = inputTypeClass == InputType.TYPE_CLASS_NUMBER
            || inputTypeClass == InputType.TYPE_CLASS_PHONE

    val enterLabel = when (imeAction) {
        EditorInfo.IME_ACTION_GO -> "前往"
        EditorInfo.IME_ACTION_SEARCH -> "搜尋"
        EditorInfo.IME_ACTION_SEND -> "傳送"
        EditorInfo.IME_ACTION_NEXT -> "下個"
        EditorInfo.IME_ACTION_DONE -> "完成"
        EditorInfo.IME_ACTION_PREVIOUS -> "上個"
        else -> "↵"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeyboardTheme.current.keyboardBackground)
    ) {
        if (isNumberField) {
            NumberKeyboard(
                inputTypeClass = inputTypeClass,
                onNumberPress = { text ->
                    onKeyPress()
                    inputStateManager.commitText(text)
                },
                onBackspace = {
                    onKeyPress()
                    inputStateManager.onBackspaceKey()
                },
                onEnter = {
                    onKeyPress()
                    inputStateManager.onEnterKey()
                },
            )
        } else {
            // Candidate bar
            when {
                state is InputState.Selecting -> {
                    val selecting = state as InputState.Selecting
                    CandidateBar(
                        candidates = selecting.candidates,
                        page = selecting.page,
                        onCandidateSelected = { index ->
                            onKeyPress()
                            inputStateManager.onCandidateSelected(index)
                        },
                        onPreviousPage = { inputStateManager.previousPage() },
                        onNextPage = { inputStateManager.nextPage() },
                    )
                }
                state is InputState.EnglishMode && (state as InputState.EnglishMode).candidates.isNotEmpty() -> {
                    val english = state as InputState.EnglishMode
                    CandidateBar(
                        candidates = english.candidates,
                        page = english.page,
                        onCandidateSelected = { index ->
                            onKeyPress()
                            inputStateManager.onEnglishCandidateSelected(index)
                        },
                        onPreviousPage = { inputStateManager.englishPreviousPage() },
                        onNextPage = { inputStateManager.englishNextPage() },
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Keyboard content
            when (state) {
                is InputState.SymbolMode -> {
                    SymbolKeyboard(
                        onSymbolSelected = { symbol ->
                            onKeyPress()
                            inputStateManager.onSymbolSelected(symbol)
                        },
                        onBack = { inputStateManager.toggleSymbolMode() },
                    )
                }
                else -> {
                    ArrayKeyboard(
                        showArrayLabels = showArrayLabels,
                        onKeyPress = { char ->
                            onKeyPress()
                            inputStateManager.onArrayKey(char)
                        },
                        onNumberPress = { digit ->
                            onKeyPress()
                            inputStateManager.onNumberKey(digit)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Function row
            FunctionRow(
                isEnglishMode = state is InputState.EnglishMode,
                enterLabel = enterLabel,
                onBackspace = {
                    onKeyPress()
                    inputStateManager.onBackspaceKey()
                },
                onSpace = {
                    onKeyPress()
                    inputStateManager.onSpaceKey()
                },
                onEnter = {
                    onKeyPress()
                    inputStateManager.onEnterKey()
                },
                onToggleEnglish = {
                    onKeyPress()
                    inputStateManager.toggleEnglishMode()
                },
                onToggleSymbol = {
                    onKeyPress()
                    inputStateManager.toggleSymbolMode()
                },
                onSwitchIme = onSwitchIme,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
