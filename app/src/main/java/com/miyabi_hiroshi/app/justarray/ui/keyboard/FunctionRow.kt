package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun FunctionRow(
    isEnglishMode: Boolean,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onToggleEnglish: () -> Unit,
    onToggleSymbol: () -> Unit,
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
            modifier = Modifier.weight(1.2f),
            onClick = onToggleEnglish,
        )
        FunctionKey(
            label = "符號",
            modifier = Modifier.weight(1.2f),
            onClick = onToggleSymbol,
        )
        FunctionKey(
            label = "空白",
            modifier = Modifier.weight(3f),
            onClick = onSpace,
        )
        FunctionKey(
            label = "⌫",
            modifier = Modifier.weight(1.2f),
            onClick = onBackspace,
        )
        FunctionKey(
            label = "↵",
            modifier = Modifier.weight(1.2f),
            onClick = onEnter,
        )
    }
}

@Composable
private fun FunctionKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(KeyboardLayout.FUNCTION_KEY_HEIGHT)
            .clip(RoundedCornerShape(6.dp))
            .background(KeyboardTheme.current.functionKeyBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = KeyboardTheme.current.functionKeyTextColor,
        )
    }
}
