package com.miyabi_hiroshi.app.justarray.ui.keyboard

import android.text.InputType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun NumberKeyboard(
    inputTypeClass: Int,
    onNumberPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPhone = inputTypeClass == InputType.TYPE_CLASS_PHONE
    val scale = LocalKeyboardHeightScale.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KeyboardLayout.KEYBOARD_PADDING),
        verticalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        // Row 1: 1 2 3
        NumpadRow(
            keys = listOf("1", "2", "3"),
            scale = scale,
            onPress = onNumberPress,
        )
        // Row 2: 4 5 6
        NumpadRow(
            keys = listOf("4", "5", "6"),
            scale = scale,
            onPress = onNumberPress,
        )
        // Row 3: 7 8 9
        NumpadRow(
            keys = listOf("7", "8", "9"),
            scale = scale,
            onPress = onNumberPress,
        )
        // Row 4: varies by type
        if (isPhone) {
            // Phone: + 0 #
            NumpadRow(
                keys = listOf("+", "0", "#"),
                scale = scale,
                onPress = onNumberPress,
            )
        } else {
            // Number: . 0 ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
            ) {
                NumpadKey(
                    label = ".",
                    scale = scale,
                    modifier = Modifier.weight(1f),
                    onClick = { onNumberPress(".") },
                )
                NumpadKey(
                    label = "0",
                    scale = scale,
                    modifier = Modifier.weight(1f),
                    onClick = { onNumberPress("0") },
                )
                NumpadKey(
                    label = "⌫",
                    scale = scale,
                    modifier = Modifier.weight(1f),
                    onClick = onBackspace,
                    onRepeat = onBackspace,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Bottom function row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
        ) {
            if (isPhone) {
                FunctionKey(
                    label = "⌫",
                    modifier = Modifier.weight(1f),
                    onClick = onBackspace,
                    onRepeat = onBackspace,
                )
            }
            FunctionKey(
                label = "↵",
                modifier = Modifier.weight(1f),
                onClick = onEnter,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun NumpadRow(
    keys: List<String>,
    scale: Float,
    onPress: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
    ) {
        for (key in keys) {
            NumpadKey(
                label = key,
                scale = scale,
                modifier = Modifier.weight(1f),
                onClick = { onPress(key) },
            )
        }
    }
}

@Composable
private fun NumpadKey(
    label: String,
    scale: Float,
    modifier: Modifier = Modifier,
    onRepeat: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val colors = KeyboardTheme.current
    val backgroundColor = if (isPressed) colors.keyPressedBackground else colors.keyBackground

    Box(
        modifier = modifier
            .height(KeyboardLayout.KEY_HEIGHT * scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .semantics {
                contentDescription = label
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = colors.keyTextColor,
        )
    }
}
