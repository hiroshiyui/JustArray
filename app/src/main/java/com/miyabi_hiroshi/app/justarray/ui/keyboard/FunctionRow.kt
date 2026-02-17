package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FunctionRow(
    isEnglishMode: Boolean,
    enterLabel: String = "↵",
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onToggleEnglish: () -> Unit,
    onToggleSymbol: () -> Unit,
    onSwitchIme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KeyboardLayout.KEYBOARD_PADDING),
        horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        FunctionKey(
            label = if (isEnglishMode) "中" else "英",
            accessibilityLabel = "切換英文/中文",
            modifier = Modifier.weight(1.2f),
            onClick = onToggleEnglish,
        )
        FunctionKey(
            label = "符號",
            accessibilityLabel = "符號",
            modifier = Modifier.weight(1.2f),
            onClick = onToggleSymbol,
        )
        FunctionKey(
            label = "\uD83C\uDF10",
            accessibilityLabel = "切換輸入法",
            modifier = Modifier.weight(0.8f),
            onClick = onSwitchIme,
        )
        FunctionKey(
            label = "空白",
            accessibilityLabel = "空白鍵",
            modifier = Modifier.weight(2.4f),
            onClick = onSpace,
        )
        FunctionKey(
            label = "⌫",
            accessibilityLabel = "刪除",
            modifier = Modifier.weight(1.2f),
            onClick = onBackspace,
            onRepeat = onBackspace,
        )
        FunctionKey(
            label = enterLabel,
            accessibilityLabel = "輸入/Enter",
            modifier = Modifier.weight(1.2f),
            onClick = onEnter,
        )
    }
}

@Composable
internal fun FunctionKey(
    label: String,
    accessibilityLabel: String = label,
    modifier: Modifier = Modifier,
    onRepeat: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnRepeat by rememberUpdatedState(onRepeat)
    var isPressed by remember { mutableStateOf(false) }
    val scale = LocalKeyboardHeightScale.current
    val colors = KeyboardTheme.current
    val backgroundColor = if (isPressed) colors.keyPressedBackground else colors.functionKeyBackground

    Box(
        modifier = modifier
            .height(KeyboardLayout.FUNCTION_KEY_HEIGHT * scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            }
            .pointerInput(Unit) {
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            var longPressed = false

                            val repeatJob = if (currentOnRepeat != null) {
                                launch {
                                    delay(400)
                                    longPressed = true
                                    while (true) {
                                        currentOnRepeat?.invoke()
                                        delay(50)
                                    }
                                }
                            } else null

                            // Wait for up or cancellation
                            val released = try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.all { !it.pressed }) break
                                }
                                true
                            } catch (_: Exception) {
                                false
                            }

                            repeatJob?.cancel()
                            isPressed = false

                            if (!longPressed && released) {
                                currentOnClick()
                            }
                        }
                    }
                }
            },
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
