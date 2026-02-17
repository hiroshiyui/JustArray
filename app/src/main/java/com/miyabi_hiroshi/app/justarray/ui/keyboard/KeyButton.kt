package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun KeyButton(
    keyDef: KeyDefinition,
    showArrayLabel: Boolean = true,
    accessibilityLabel: String = keyDef.displayChar,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    var isPressed by remember { mutableStateOf(false) }
    val scale = LocalKeyboardHeightScale.current
    val colors = KeyboardTheme.current
    val backgroundColor = if (isPressed) colors.keyPressedBackground else colors.keyBackground

    // Compute popup offset: position above the key
    val density = LocalDensity.current
    val keyHeightDp = KeyboardLayout.KEY_HEIGHT * scale
    val popupOffsetY = with(density) { -(keyHeightDp + 12.dp).roundToPx() }

    Box(
        modifier = modifier
            .height(keyHeightDp)
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = { currentOnClick() },
                )
            }
            .padding(2.dp)
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        // Array label in top-left corner
        if (showArrayLabel) {
            Text(
                text = keyDef.arrayLabel,
                fontSize = 8.sp,
                color = colors.arrayLabelColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp),
                lineHeight = 10.sp,
            )
        }
        // Main character label
        Text(
            text = keyDef.displayChar,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.keyTextColor,
            textAlign = TextAlign.Center,
        )

        // Popup preview when pressed
        if (isPressed) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, popupOffsetY),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp, 56.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.keyBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = keyDef.displayChar,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.keyTextColor,
                    )
                }
            }
        }
    }
}
