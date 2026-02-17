package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shared pointer event handling for keyboard keys: tap to click, with optional
 * long-press key-repeat. Reports pressed state via [onPressedChange].
 *
 * Used by [FunctionKey] and `NumpadKey`. [KeyButton] has its own gesture handling
 * (swipe-up + long-press alternates popup).
 */
fun Modifier.keyPressGesture(
    onClick: () -> Unit,
    onRepeat: (() -> Unit)? = null,
    onPressedChange: (Boolean) -> Unit = {},
) = composed {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnRepeat by rememberUpdatedState(onRepeat)
    val currentOnPressedChange by rememberUpdatedState(onPressedChange)

    pointerInput(Unit) {
        coroutineScope {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    currentOnPressedChange(true)
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
                    currentOnPressedChange(false)

                    if (!longPressed && released) {
                        currentOnClick()
                    }
                }
            }
        }
    }
}
