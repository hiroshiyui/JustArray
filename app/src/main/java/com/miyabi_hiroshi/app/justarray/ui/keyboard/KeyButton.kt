package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun KeyButton(
    keyDef: KeyDefinition,
    showArrayLabel: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(KeyboardLayout.KEY_HEIGHT)
            .widthIn(min = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(KeyboardTheme.current.keyBackground)
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Array label in top-left corner
        if (showArrayLabel) {
            Text(
                text = keyDef.arrayLabel,
                fontSize = 8.sp,
                color = KeyboardTheme.current.arrayLabelColor,
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
            color = KeyboardTheme.current.keyTextColor,
            textAlign = TextAlign.Center,
        )
    }
}
