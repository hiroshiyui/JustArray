package com.miyabi_hiroshi.app.justarray.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun SymbolKeyboard(
    onSymbolSelected: (String) -> Unit,
    onBack: () -> Unit,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = KeyboardLayout.SYMBOL_CATEGORIES
    val scale = LocalKeyboardHeightScale.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategory,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 4.dp,
        ) {
            categories.forEachIndexed { index, (nameResId, _) ->
                Tab(
                    selected = selectedCategory == index,
                    onClick = { selectedCategory = index },
                    text = { Text(stringResource(nameResId), fontSize = 13.sp) },
                )
            }
            Tab(
                selected = false,
                onClick = onBack,
                text = { Text(stringResource(R.string.key_back), fontSize = 13.sp) },
            )
        }

        // Symbol grid
        val symbols = categories[selectedCategory].second
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .height((if (isLandscape) 100.dp else 160.dp) * scale)
                .padding(KeyboardLayout.KEYBOARD_PADDING),
            horizontalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
            verticalArrangement = Arrangement.spacedBy(KeyboardLayout.KEY_SPACING),
        ) {
            items(symbols) { symbol ->
                Box(
                    modifier = Modifier
                        .height(38.dp * scale)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KeyboardTheme.current.keyBackground)
                        .clickable { onSymbolSelected(symbol) }
                        .semantics {
                            contentDescription = symbol
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = symbol,
                        fontSize = 18.sp,
                        color = KeyboardTheme.current.keyTextColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
