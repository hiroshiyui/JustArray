package com.miyabi_hiroshi.app.justarray.ui.candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.justarray.ime.InputStateManager
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme

@Composable
fun CandidateBar(
    candidates: List<String>,
    page: Int,
    onCandidateSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageSize = InputStateManager.CANDIDATES_PER_PAGE
    val startIndex = page * pageSize
    val endIndex = minOf(startIndex + pageSize, candidates.size)
    val pageCandidates = if (startIndex < candidates.size) {
        candidates.subList(startIndex, endIndex)
    } else {
        emptyList()
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(KeyboardTheme.current.candidateBarBackground)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(pageCandidates) { index, candidate ->
            val selectionLabel = if (index == 9) "0" else "${index + 1}"
            Box(
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .height(36.dp)
                    .clickable { onCandidateSelected(index) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = selectionLabel,
                        fontSize = 10.sp,
                        color = KeyboardTheme.current.candidateNumberColor,
                    )
                    Text(
                        text = candidate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = KeyboardTheme.current.candidateTextColor,
                    )
                }
            }
        }
    }
}
