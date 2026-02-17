package com.miyabi_hiroshi.app.justarray.ui.keyboard

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
import com.miyabi_hiroshi.app.justarray.ui.candidate.ComposeBar
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun KeyboardScreen(
    inputStateManager: InputStateManager,
    onKeyPress: () -> Unit = {},
) {
    val state by inputStateManager.state.collectAsState()
    val candidates by inputStateManager.candidates.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KeyboardTheme.current.keyboardBackground)
    ) {
        // Compose bar (shows current input sequence)
        when (val s = state) {
            is InputState.Composing -> {
                ComposeBar(composingText = s.keys)
            }
            is InputState.Selecting -> {
                ComposeBar(composingText = s.keys)
            }
            else -> {}
        }

        // Candidate bar
        if (state is InputState.Selecting) {
            val selecting = state as InputState.Selecting
            CandidateBar(
                candidates = selecting.candidates,
                page = selecting.page,
                onCandidateSelected = { index ->
                    onKeyPress()
                    inputStateManager.onCandidateSelected(index)
                },
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

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
                    showArrayLabels = true,
                    onKeyPress = { char ->
                        onKeyPress()
                        inputStateManager.onArrayKey(char)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Function row
        FunctionRow(
            isEnglishMode = state is InputState.EnglishMode,
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
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}
