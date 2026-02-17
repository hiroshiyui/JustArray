package com.miyabi_hiroshi.app.justarray.ui.candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.ime.KeyCode
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun ComposeBar(
    composingText: String,
    modifier: Modifier = Modifier,
) {
    val displayText = KeyCode.toArrayLabels(composingText)

    Text(
        text = displayText,
        fontSize = 14.sp,
        color = KeyboardTheme.current.composeBarTextColor,
        modifier = modifier
            .fillMaxWidth()
            .background(KeyboardTheme.current.composeBarBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
