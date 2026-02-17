package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KeyButton(
    keyDef: KeyDefinition,
    showArrayLabel: Boolean = true,
    accessibilityLabel: String = keyDef.displayChar,
    keyHeight: Dp? = null,
    onSwipeUp: (() -> Unit)? = null,
    onAlternateSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnSwipeUp by rememberUpdatedState(onSwipeUp)
    val currentOnAlternateSelected by rememberUpdatedState(onAlternateSelected)
    var isPressed by remember { mutableStateOf(false) }
    var showAlternates by remember { mutableStateOf(false) }
    var alternateIndex by remember { mutableIntStateOf(-1) }
    val scale = LocalKeyboardHeightScale.current
    val colors = KeyboardTheme.current
    val backgroundColor = if (isPressed) colors.keyPressedBackground else colors.keyBackground

    val alternates = KeyboardLayout.KEY_ALTERNATES[keyDef.qwertyChar] ?: emptyList()

    val density = LocalDensity.current
    val effectiveKeyHeight = keyHeight ?: (KeyboardLayout.KEY_HEIGHT * scale)
    val popupOffsetY = with(density) { -(effectiveKeyHeight + 12.dp).roundToPx() }
    val swipeThresholdPx = with(density) { 20.dp.toPx() }
    val alternateCellWidthPx = with(density) { 38.dp.toPx() }

    Box(
        modifier = modifier
            .height(effectiveKeyHeight)
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            showAlternates = false
                            alternateIndex = -1
                            val startY = down.position.y
                            val startX = down.position.x
                            var gestureHandled = false

                            val longPressJob = if (alternates.isNotEmpty()) {
                                launch {
                                    delay(400)
                                    // Long press triggered — show alternates
                                    showAlternates = true
                                    alternateIndex = 0
                                    gestureHandled = true
                                }
                            } else null

                            // Track pointer until release
                            var released = false
                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.firstOrNull() ?: break
                                    if (change.changedToUp()) {
                                        released = true
                                        break
                                    }

                                    val dy = change.position.y - startY

                                    if (!showAlternates && !gestureHandled && dy < -swipeThresholdPx) {
                                        // Swipe up detected
                                        longPressJob?.cancel()
                                        gestureHandled = true
                                        isPressed = false
                                        currentOnSwipeUp?.invoke()
                                        // Consume remaining until up
                                        while (true) {
                                            val ev = awaitPointerEvent(PointerEventPass.Main)
                                            if (ev.changes.all { it.changedToUp() }) break
                                        }
                                        released = true
                                        break
                                    }

                                    if (showAlternates) {
                                        // Track horizontal movement for alternate selection
                                        val dx = change.position.x - startX
                                        val idx = (dx / alternateCellWidthPx + 0.5f).toInt()
                                            .coerceIn(0, alternates.size - 1)
                                        alternateIndex = idx
                                    }
                                }
                            } catch (_: Exception) {
                                released = false
                            }

                            longPressJob?.cancel()
                            isPressed = false

                            if (showAlternates && released && alternateIndex in alternates.indices) {
                                val selected = alternates[alternateIndex]
                                showAlternates = false
                                currentOnAlternateSelected?.invoke(selected)
                            } else if (!gestureHandled && released) {
                                currentOnClick()
                            }
                            showAlternates = false
                        }
                    }
                }
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

        // Popup preview when pressed (no alternates showing)
        if (isPressed && !showAlternates) {
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

        // Alternates popup
        if (showAlternates && alternates.isNotEmpty()) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, popupOffsetY),
            ) {
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.keyBackground),
                ) {
                    alternates.forEachIndexed { index, alt ->
                        val cellBg = if (index == alternateIndex) colors.keyPressedBackground else colors.keyBackground
                        Box(
                            modifier = Modifier
                                .size(38.dp, 44.dp)
                                .background(cellBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = alt,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.keyTextColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
