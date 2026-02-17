package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.ime.ShiftState
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun FunctionRow(
    isEnglishMode: Boolean,
    shiftState: ShiftState = ShiftState.NONE,
    isLandscape: Boolean = false,
    enterLabel: String = "↵",
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onToggleEnglish: () -> Unit,
    onToggleSymbol: () -> Unit,
    onShift: () -> Unit = {},
    onSwitchIme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val functionKeyHeight: Dp? = if (isLandscape) KeyboardLayout.LANDSCAPE_FUNCTION_KEY_HEIGHT else null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KeyboardLayout.KEYBOARD_PADDING),
        horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        FunctionKey(
            label = if (isEnglishMode) stringResource(R.string.key_toggle_to_chinese) else stringResource(R.string.key_toggle_to_english),
            accessibilityLabel = stringResource(R.string.a11y_toggle_language),
            keyHeight = functionKeyHeight,
            modifier = Modifier.weight(1.2f),
            onClick = onToggleEnglish,
        )
        if (isEnglishMode) {
            FunctionKey(
                label = if (shiftState == ShiftState.CAPS_LOCK) "⇪" else "⇧",
                accessibilityLabel = stringResource(R.string.a11y_shift),
                isActive = shiftState != ShiftState.NONE,
                keyHeight = functionKeyHeight,
                modifier = Modifier.weight(1.2f),
                onClick = onShift,
            )
        } else {
            FunctionKey(
                label = stringResource(R.string.key_symbol),
                accessibilityLabel = stringResource(R.string.a11y_symbol),
                keyHeight = functionKeyHeight,
                modifier = Modifier.weight(1.2f),
                onClick = onToggleSymbol,
            )
        }
        FunctionKey(
            label = "\uD83C\uDF10",
            accessibilityLabel = stringResource(R.string.a11y_switch_ime),
            keyHeight = functionKeyHeight,
            modifier = Modifier.weight(0.8f),
            onClick = onSwitchIme,
        )
        FunctionKey(
            label = stringResource(R.string.key_space),
            accessibilityLabel = stringResource(R.string.a11y_space),
            keyHeight = functionKeyHeight,
            modifier = Modifier.weight(2.4f),
            onClick = onSpace,
        )
        FunctionKey(
            label = "⌫",
            accessibilityLabel = stringResource(R.string.a11y_backspace),
            keyHeight = functionKeyHeight,
            modifier = Modifier.weight(1.2f),
            onClick = onBackspace,
            onRepeat = onBackspace,
        )
        FunctionKey(
            label = enterLabel,
            accessibilityLabel = stringResource(R.string.a11y_enter),
            keyHeight = functionKeyHeight,
            modifier = Modifier.weight(1.2f),
            onClick = onEnter,
        )
    }
}

@Composable
internal fun FunctionKey(
    label: String,
    accessibilityLabel: String = label,
    isActive: Boolean = false,
    keyHeight: Dp? = null,
    modifier: Modifier = Modifier,
    onRepeat: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale = LocalKeyboardHeightScale.current
    val colors = KeyboardTheme.current
    val backgroundColor = when {
        isPressed -> colors.keyPressedBackground
        isActive -> colors.keyPressedBackground
        else -> colors.functionKeyBackground
    }

    Box(
        modifier = modifier
            .height(keyHeight ?: (KeyboardLayout.FUNCTION_KEY_HEIGHT * scale))
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            }
            .keyPressGesture(
                onClick = onClick,
                onRepeat = onRepeat,
                onPressedChange = { isPressed = it },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.functionKeyTextColor,
        )
    }
}
