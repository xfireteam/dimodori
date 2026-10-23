package com.dimodori.app.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimodori.app.ui.screens.dashboard.LibrarySkeleton
import com.jellycine.data.model.BaseItemDto
import com.jellycine.shared.R

/** Layout only: callers supply their existing mobile/TV cards and focus rules. */
@Composable
internal fun RecentHomeSection(
    titleRes: Int,
    items: List<BaseItemDto>,
    isLoading: Boolean,
    isError: Boolean,
    rowModifier: Modifier = Modifier,
    card: @Composable (Int, BaseItemDto) -> Unit
) {
    if (items.isEmpty() && !isLoading && !isError) return
    Column {
        Text(
            text = stringResource(titleRes),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        when {
            items.isNotEmpty() -> LazyRow(
                state = rememberLazyListState(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = rowModifier.fillMaxWidth()
            ) {
                itemsIndexed(items, key = { _, item -> requireNotNull(item.id) }) { index, item ->
                    card(index, item)
                }
            }
            isLoading -> LibrarySkeleton()
            isError -> Text(
                text = stringResource(R.string.dashboard_failed_recent_titles),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}
