package com.miyabi_hiroshi.app.justarray.ime

enum class ShiftState { NONE, SHIFTED, CAPS_LOCK }

sealed interface InputState {
    data object Idle : InputState

    data class Composing(
        val keys: String = "",           // accumulated QWERTY keystrokes (max 4)
        val preEditBuffer: String = "",  // selected candidates not yet committed
        val page: Int = 0,              // candidate page for inline preview
    ) : InputState

    data class Selecting(
        val keys: String,
        val candidates: List<String>,
        val page: Int = 0,
        val preEditBuffer: String = "",  // selected candidates not yet committed
    ) : InputState

    data class EnglishMode(
        val typedText: String = "",
        val candidates: List<String> = emptyList(),
        val page: Int = 0,
        val shiftState: ShiftState = ShiftState.NONE,
    ) : InputState

    data class SymbolMode(
        val category: Int = 0
    ) : InputState
}
