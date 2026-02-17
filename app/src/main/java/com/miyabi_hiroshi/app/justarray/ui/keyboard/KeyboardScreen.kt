package com.miyabi_hiroshi.app.justarray.ui.keyboard

import android.content.res.Configuration
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.data.dictionary.DictLoadState
import com.miyabi_hiroshi.app.justarray.ime.InputState
import com.miyabi_hiroshi.app.justarray.ime.InputStateManager
import com.miyabi_hiroshi.app.justarray.ime.ShiftState
import com.miyabi_hiroshi.app.justarray.ui.candidate.CandidateBar
import com.miyabi_hiroshi.app.justarray.ui.candidate.ClipboardSuggestion

import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun KeyboardScreen(
    inputStateManager: InputStateManager,
    showArrayLabels: Boolean = true,
    dictLoadState: DictLoadState = DictLoadState.Loaded,
    clipboardText: String? = null,
    onKeyPress: () -> Unit = {},
    onSwitchIme: () -> Unit = {},
    onClipboardPaste: (String) -> Unit = {},
) {
    val state by inputStateManager.state.collectAsState()
    val candidates by inputStateManager.candidates.collectAsState()
    val inputTypeClass by inputStateManager.inputTypeClass.collectAsState()
    val imeAction by inputStateManager.imeAction.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isNumberField = inputTypeClass == InputType.TYPE_CLASS_NUMBER
            || inputTypeClass == InputType.TYPE_CLASS_PHONE

    val enterLabel = when (imeAction) {
        EditorInfo.IME_ACTION_GO -> stringResource(R.string.key_enter_go)
        EditorInfo.IME_ACTION_SEARCH -> stringResource(R.string.key_enter_search)
        EditorInfo.IME_ACTION_SEND -> stringResource(R.string.key_enter_send)
        EditorInfo.IME_ACTION_NEXT -> stringResource(R.string.key_enter_next)
        EditorInfo.IME_ACTION_DONE -> stringResource(R.string.key_enter_done)
        EditorInfo.IME_ACTION_PREVIOUS -> stringResource(R.string.key_enter_previous)
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
                dictLoadState is DictLoadState.Loading || dictLoadState is DictLoadState.NotStarted -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(KeyboardTheme.current.candidateBarBackground),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.dict_loading),
                            fontSize = 14.sp,
                            color = KeyboardTheme.current.candidateTextColor,
                        )
                    }
                }
                dictLoadState is DictLoadState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(KeyboardTheme.current.candidateBarBackground)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = dictLoadState.message,
                            fontSize = 12.sp,
                            color = KeyboardTheme.current.candidateTextColor,
                        )
                    }
                }
                state is InputState.Composing && candidates.isNotEmpty() -> {
                    val composing = state as InputState.Composing
                    CandidateBar(
                        candidates = candidates,
                        page = composing.page,
                        onCandidateSelected = { index ->
                            onKeyPress()
                            inputStateManager.onComposingCandidateSelected(index)
                        },
                        onPreviousPage = { inputStateManager.composingPreviousPage() },
                        onNextPage = { inputStateManager.composingNextPage() },
                    )
                }
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
                state is InputState.Idle && !clipboardText.isNullOrEmpty() -> {
                    ClipboardSuggestion(
                        text = clipboardText,
                        onPaste = { onClipboardPaste(clipboardText) },
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
                        isLandscape = isLandscape,
                    )
                }
                else -> {
                    ArrayKeyboard(
                        showArrayLabels = showArrayLabels,
                        isLandscape = isLandscape,
                        onKeyPress = { char ->
                            onKeyPress()
                            inputStateManager.onArrayKey(char)
                        },
                        onNumberPress = { digit ->
                            onKeyPress()
                            inputStateManager.onNumberKey(digit)
                        },
                        onSwipeUp = { char ->
                            onKeyPress()
                            inputStateManager.onSwipeUpKey(char)
                        },
                        onAlternateSelected = { alt ->
                            onKeyPress()
                            inputStateManager.commitText(alt)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Function row
            val shiftState = (state as? InputState.EnglishMode)?.shiftState ?: ShiftState.NONE
            FunctionRow(
                isEnglishMode = state is InputState.EnglishMode,
                shiftState = shiftState,
                isLandscape = isLandscape,
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
                onShift = {
                    onKeyPress()
                    inputStateManager.onShiftKey()
                },
                onSwitchIme = onSwitchIme,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
