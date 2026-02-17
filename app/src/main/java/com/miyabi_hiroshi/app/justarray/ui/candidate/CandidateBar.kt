package com.miyabi_hiroshi.app.justarray.ui.candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.miyabi_hiroshi.app.justarray.R
import com.miyabi_hiroshi.app.justarray.ime.InputStateManager
import com.miyabi_hiroshi.app.justarray.ui.theme.KeyboardTheme
import kotlin.math.ceil

private data class CandidatePage(
    val items: List<String>,
    val currentPage: Int,
    val totalPages: Int,
)

private fun paginate(allCandidates: List<String>, page: Int): CandidatePage {
    val pageSize = InputStateManager.CANDIDATES_PER_PAGE
    val startIndex = page * pageSize
    val endIndex = minOf(startIndex + pageSize, allCandidates.size)
    val items = if (startIndex < allCandidates.size) {
        allCandidates.subList(startIndex, endIndex)
    } else {
        emptyList()
    }
    val totalPages = ceil(allCandidates.size.toDouble() / pageSize).toInt()
    return CandidatePage(items = items, currentPage = page + 1, totalPages = totalPages)
}

@Composable
fun CandidateBar(
    candidates: List<String>,
    page: Int,
    reverseCodes: Map<String, String> = emptyMap(),
    typedWord: String? = null,
    onTypedWordTapped: (() -> Unit)? = null,
    onCandidateSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (pageCandidates, currentPage, totalPages) = paginate(candidates, page)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(KeyboardTheme.current.candidateBarBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!typedWord.isNullOrEmpty()) {
                item {
                    TypedWordChip(
                        word = typedWord,
                        onTapped = { onTypedWordTapped?.invoke() },
                    )
                }
            }
            itemsIndexed(pageCandidates) { index, candidate ->
                CandidateItem(
                    index = index,
                    candidate = candidate,
                    code = reverseCodes[candidate],
                    onSelected = { onCandidateSelected(index) },
                )
            }
        }
        if (totalPages >= 2) {
            PageNavigator(
                currentPage = currentPage,
                totalPages = totalPages,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
            )
        }
    }
}

@Composable
private fun PageNavigator(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    val prevPageDesc = stringResource(R.string.a11y_previous_page)
    val nextPageDesc = stringResource(R.string.a11y_next_page)
    val pageIndicatorDesc = stringResource(R.string.a11y_page_indicator, currentPage, totalPages)

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(36.dp)
                .clickable { onPreviousPage() }
                .semantics {
                    contentDescription = prevPageDesc
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "⬅",
                fontSize = 14.sp,
                color = KeyboardTheme.current.candidateNumberColor,
            )
        }
        Text(
            text = "$currentPage/$totalPages",
            fontSize = 12.sp,
            color = KeyboardTheme.current.candidateNumberColor,
            modifier = Modifier.semantics {
                contentDescription = pageIndicatorDesc
            },
        )
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(36.dp)
                .clickable { onNextPage() }
                .semantics {
                    contentDescription = nextPageDesc
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "➡",
                fontSize = 14.sp,
                color = KeyboardTheme.current.candidateNumberColor,
            )
        }
    }
}

@Composable
private fun CandidateItem(
    index: Int,
    candidate: String,
    code: String? = null,
    onSelected: () -> Unit,
) {
    val numberLabel = if (index == 9) "0" else "${index + 1}"
    val candidateDesc = stringResource(R.string.a11y_candidate, index + 1, candidate)
    Box(
        modifier = Modifier
            .widthIn(min = 40.dp)
            .height(36.dp)
            .clickable { onSelected() }
            .padding(horizontal = 6.dp)
            .semantics {
                contentDescription = candidateDesc
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = numberLabel,
                fontSize = 10.sp,
                color = KeyboardTheme.current.candidateNumberColor,
            )
            Text(
                text = candidate,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = KeyboardTheme.current.candidateTextColor,
            )
            if (code != null) {
                Text(
                    text = code,
                    fontSize = 9.sp,
                    color = KeyboardTheme.current.candidateNumberColor,
                )
            }
        }
    }
}

@Composable
private fun TypedWordChip(
    word: String,
    onTapped: () -> Unit,
) {
    val typedWordDesc = stringResource(R.string.a11y_typed_word, word)
    Box(
        modifier = Modifier
            .widthIn(min = 40.dp)
            .height(36.dp)
            .clickable { onTapped() }
            .padding(horizontal = 6.dp)
            .semantics {
                contentDescription = typedWordDesc
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = word,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = KeyboardTheme.current.candidateTextColor,
        )
    }
}
