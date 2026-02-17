package com.miyabi_hiroshi.app.justarray.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class KeyboardColors(
    val keyboardBackground: Color,
    val keyBackground: Color,
    val keyPressedBackground: Color,
    val keyTextColor: Color,
    val arrayLabelColor: Color,
    val functionKeyBackground: Color,
    val functionKeyTextColor: Color,
    val candidateBarBackground: Color,
    val candidateTextColor: Color,
    val candidateNumberColor: Color,
)

fun KeyboardColors(colorScheme: ColorScheme): KeyboardColors = KeyboardColors(
    keyboardBackground = colorScheme.surfaceContainerLow,
    keyBackground = colorScheme.surfaceContainerHigh,
    keyPressedBackground = colorScheme.surfaceContainerHighest,
    keyTextColor = colorScheme.onSurface,
    arrayLabelColor = colorScheme.onSurfaceVariant,
    functionKeyBackground = colorScheme.secondaryContainer,
    functionKeyTextColor = colorScheme.onSecondaryContainer,
    candidateBarBackground = colorScheme.surfaceContainer,
    candidateTextColor = colorScheme.onSurface,
    candidateNumberColor = colorScheme.onSurfaceVariant,
)

val LocalKeyboardColors = staticCompositionLocalOf { KeyboardColors(lightColorScheme()) }

object KeyboardTheme {
    val current: KeyboardColors
        @Composable
        get() = LocalKeyboardColors.current
}

@Composable
fun KeyboardThemeProvider(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit,
) {
    val colors = KeyboardColors(colorScheme)
    CompositionLocalProvider(LocalKeyboardColors provides colors) {
        content()
    }
}
