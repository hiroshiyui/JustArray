package com.miyabi_hiroshi.app.justarray.ui.candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun ClipboardSuggestion(
    text: String,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayText = if (text.length > 20) text.take(20) + "…" else text
    val pasteDesc = stringResource(R.string.a11y_paste_clipboard)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(KeyboardTheme.current.candidateBarBackground)
            .clickable { onPaste() }
            .padding(horizontal = 8.dp)
            .semantics {
                contentDescription = pasteDesc
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\uD83D\uDCCB",
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 6.dp),
        )
        Text(
            text = displayText,
            fontSize = 14.sp,
            color = KeyboardTheme.current.candidateTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
