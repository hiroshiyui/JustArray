package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ArrayKeyboard(
    showArrayLabels: Boolean = true,
    onKeyPress: (Char) -> Unit,
    onNumberPress: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KeyboardLayout.KEYBOARD_PADDING),
        verticalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        NumberRow(onNumberPress = onNumberPress)
        KeyRow(keys = KeyboardLayout.TOP_ROW, showArrayLabels = showArrayLabels, onKeyPress = onKeyPress)
        KeyRow(keys = KeyboardLayout.MIDDLE_ROW, showArrayLabels = showArrayLabels, onKeyPress = onKeyPress)
        KeyRow(keys = KeyboardLayout.BOTTOM_ROW, showArrayLabels = showArrayLabels, onKeyPress = onKeyPress)
    }
}

@Composable
private fun NumberRow(
    onNumberPress: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        for (key in KeyboardLayout.NUMBER_ROW) {
            val digit = key.displayChar.toIntOrNull() ?: 0
            KeyButton(
                keyDef = key,
                showArrayLabel = false,
                modifier = Modifier.weight(1f),
                onClick = { onNumberPress(digit) },
            )
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<KeyDefinition>,
    showArrayLabels: Boolean,
    onKeyPress: (Char) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        for (key in keys) {
            KeyButton(
                keyDef = key,
                showArrayLabel = showArrayLabels,
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress(key.qwertyChar) },
            )
        }
    }
}
