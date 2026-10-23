package com.dimodori.app.tv.ui.screens.dashboard.home

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.dimodori.app.ui.components.common.RecentHomeSection
import com.jellycine.data.model.BaseItemDto
import com.jellycine.data.repository.MediaRepository

@Composable
internal fun RecentTitlesSection(
    titleRes: Int,
    items: List<BaseItemDto>,
    isLoading: Boolean,
    isError: Boolean,
    mediaRepository: MediaRepository,
    disablePosterEnhancers: Boolean,
    initialFocusRequester: FocusRequester?,
    returnFocusRequester: FocusRequester?,
    onReturnTargetChanged: ((FocusRequester) -> Unit)?,
    onItemClick: (BaseItemDto) -> Unit
) {
    RecentHomeSection(titleRes, items, isLoading, isError, Modifier.focusGroup()) { index, item ->
        val requester = remember(item.id) { FocusRequester() }
        LibraryItemCard(
            item = item,
            cardModifier = entryCardModifier(
                cardFocusRequester = requester,
                initialFocusRequester = if (index == 0) initialFocusRequester else null,
                returnFocusRequester = if (index == 0) returnFocusRequester else null,
                onReturnTargetChanged = onReturnTargetChanged
            ),
            mediaRepository = mediaRepository,
            disableImageEnhancers = disablePosterEnhancers,
            onClick = { onItemClick(item) }
        )
    }
}
