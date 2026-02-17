package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun ArrayKeyboard(
    showArrayLabels: Boolean = true,
    isLandscape: Boolean = false,
    onKeyPress: (Char) -> Unit,
    onNumberPress: (Int) -> Unit = {},
    onSwipeUp: ((Char) -> Unit)? = null,
    onAlternateSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val keyHeight: Dp? = if (isLandscape) KeyboardLayout.LANDSCAPE_KEY_HEIGHT else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KeyboardLayout.KEYBOARD_PADDING),
        verticalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        NumberRow(onNumberPress = onNumberPress, keyHeight = keyHeight)
        if (isLandscape) {
            SplitKeyRow(keys = KeyboardLayout.TOP_ROW, showArrayLabels = showArrayLabels, keyHeight = keyHeight, onKeyPress = onKeyPress, onSwipeUp = onSwipeUp, onAlternateSelected = onAlternateSelected)
            SplitKeyRow(keys = KeyboardLayout.MIDDLE_ROW, showArrayLabels = showArrayLabels, keyHeight = keyHeight, onKeyPress = onKeyPress, onSwipeUp = onSwipeUp, onAlternateSelected = onAlternateSelected)
            SplitKeyRow(keys = KeyboardLayout.BOTTOM_ROW, showArrayLabels = showArrayLabels, keyHeight = keyHeight, onKeyPress = onKeyPress, onSwipeUp = onSwipeUp, onAlternateSelected = onAlternateSelected)
        } else {
            KeyRow(keys = KeyboardLayout.TOP_ROW, showArrayLabels = showArrayLabels, onKeyPress = onKeyPress, onSwipeUp = onSwipeUp, onAlternateSelected = onAlternateSelected)
            KeyRow(keys = KeyboardLayout.MIDDLE_ROW, showArrayLabels = showArrayLabels, onKeyPress = onKeyPress, onSwipeUp = onSwipeUp, onAlternateSelected = onAlternateSelected)
            KeyRow(keys = KeyboardLayout.BOTTOM_ROW, showArrayLabels = showArrayLabels, onKeyPress = onKeyPress, onSwipeUp = onSwipeUp, onAlternateSelected = onAlternateSelected)
        }
    }
}

@Composable
private fun NumberRow(
    onNumberPress: (Int) -> Unit,
    keyHeight: Dp? = null,
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
                accessibilityLabel = "數字 ${key.displayChar}",
                keyHeight = keyHeight,
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
    onSwipeUp: ((Char) -> Unit)? = null,
    onAlternateSelected: ((String) -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        for (key in keys) {
            KeyButton(
                keyDef = key,
                showArrayLabel = showArrayLabels,
                onSwipeUp = onSwipeUp?.let { callback -> { callback(key.qwertyChar) } },
                onAlternateSelected = onAlternateSelected,
                modifier = Modifier.weight(1f),
                onClick = { onKeyPress(key.qwertyChar) },
            )
        }
    }
}

@Composable
private fun SplitKeyRow(
    keys: List<KeyDefinition>,
    showArrayLabels: Boolean,
    keyHeight: Dp?,
    onKeyPress: (Char) -> Unit,
    onSwipeUp: ((Char) -> Unit)? = null,
    onAlternateSelected: ((String) -> Unit)? = null,
) {
    val splitIndex = KeyboardLayout.SPLIT_INDEX
    val leftKeys = keys.take(splitIndex)
    val rightKeys = keys.drop(splitIndex)

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Left half
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
        ) {
            for (key in leftKeys) {
                KeyButton(
                    keyDef = key,
                    showArrayLabel = showArrayLabels,
                    keyHeight = keyHeight,
                    onSwipeUp = onSwipeUp?.let { callback -> { callback(key.qwertyChar) } },
                    onAlternateSelected = onAlternateSelected,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(key.qwertyChar) },
                )
            }
        }

        Spacer(modifier = Modifier.width(KeyboardLayout.LANDSCAPE_CENTER_GAP))

        // Right half
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
        ) {
            for (key in rightKeys) {
                KeyButton(
                    keyDef = key,
                    showArrayLabel = showArrayLabels,
                    keyHeight = keyHeight,
                    onSwipeUp = onSwipeUp?.let { callback -> { callback(key.qwertyChar) } },
                    onAlternateSelected = onAlternateSelected,
                    modifier = Modifier.weight(1f),
                    onClick = { onKeyPress(key.qwertyChar) },
                )
            }
        }
    }
}
