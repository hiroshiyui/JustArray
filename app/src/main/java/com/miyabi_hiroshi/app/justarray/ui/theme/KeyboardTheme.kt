package com.miyabi_hiroshi.app.justarray.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
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
    val composeBarBackground: Color,
    val composeBarTextColor: Color,
)

val LightKeyboardColors = KeyboardColors(
    keyboardBackground = Color(0xFFD2D3D9),
    keyBackground = Color(0xFFFFFFFF),
    keyPressedBackground = Color(0xFFBDBEC4),
    keyTextColor = Color(0xFF1B1B1F),
    arrayLabelColor = Color(0xFF6B6B73),
    functionKeyBackground = Color(0xFFAEB0B8),
    functionKeyTextColor = Color(0xFF1B1B1F),
    candidateBarBackground = Color(0xFFE8E8ED),
    candidateTextColor = Color(0xFF1B1B1F),
    candidateNumberColor = Color(0xFF6B6B73),
    composeBarBackground = Color(0xFFE0E1E6),
    composeBarTextColor = Color(0xFF3A3A42),
)

val DarkKeyboardColors = KeyboardColors(
    keyboardBackground = Color(0xFF1B1B1F),
    keyBackground = Color(0xFF3A3A42),
    keyPressedBackground = Color(0xFF525259),
    keyTextColor = Color(0xFFE4E2E6),
    arrayLabelColor = Color(0xFF9C9CA3),
    functionKeyBackground = Color(0xFF2E2E36),
    functionKeyTextColor = Color(0xFFE4E2E6),
    candidateBarBackground = Color(0xFF252529),
    candidateTextColor = Color(0xFFE4E2E6),
    candidateNumberColor = Color(0xFF9C9CA3),
    composeBarBackground = Color(0xFF2A2A30),
    composeBarTextColor = Color(0xFFC4C4CC),
)

val LocalKeyboardColors = staticCompositionLocalOf { LightKeyboardColors }

object KeyboardTheme {
    val current: KeyboardColors
        @Composable
        get() = LocalKeyboardColors.current
}

@Composable
fun KeyboardThemeProvider(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkKeyboardColors else LightKeyboardColors
    CompositionLocalProvider(LocalKeyboardColors provides colors) {
        content()
    }
}
