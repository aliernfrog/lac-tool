package com.aliernfrog.lactool.ui.component.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

const val DEFAULT_ACCESSIBILITY_SCROLL_THRESHOLD = 10 // pixels

@Composable
fun ScrollAccessibilityListener(
    scrollState: ScrollState,
    threshold: Int = DEFAULT_ACCESSIBILITY_SCROLL_THRESHOLD,
    onShowLabelsStateChange: (Boolean) -> Unit
) {
    var previousOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { currentOffset ->
                val delta = currentOffset - previousOffset
                if (delta > threshold) onShowLabelsStateChange(false)
                else if (delta < -threshold) onShowLabelsStateChange(true)

                previousOffset = currentOffset
                if (currentOffset == 0) onShowLabelsStateChange(true)
            }
    }
}

@Composable
fun LazyListScrollAccessibilityListener(
    lazyListState: LazyListState,
    threshold: Int = DEFAULT_ACCESSIBILITY_SCROLL_THRESHOLD,
    onShowLabelsStateChange: (Boolean) -> Unit
) {
    var previousItemIndex by remember { mutableIntStateOf(0) }
    var previousItemScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            Pair(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset)
        }
            .collect { (currentItemIndex, currentItemScrollOffset) ->
                if (currentItemIndex == 0 && currentItemScrollOffset == 0) onShowLabelsStateChange(true)
                else if (currentItemIndex > previousItemIndex) onShowLabelsStateChange(false)
                else if (currentItemIndex < previousItemIndex) onShowLabelsStateChange(true)
                else {
                    val delta = currentItemScrollOffset - previousItemScrollOffset
                    if (delta > threshold) onShowLabelsStateChange(false)
                    else if (delta < -threshold) onShowLabelsStateChange(true)
                }

                previousItemIndex = currentItemIndex
                previousItemScrollOffset = currentItemScrollOffset
            }
    }
}

@Composable
fun LazyGridScrollAccessibilityListener(
    lazyGridState: LazyGridState,
    threshold: Int = DEFAULT_ACCESSIBILITY_SCROLL_THRESHOLD,
    onShowLabelsStateChange: (Boolean) -> Unit
) {
    var previousItemIndex by remember { mutableIntStateOf(0) }
    var previousItemScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            Pair(lazyGridState.firstVisibleItemIndex, lazyGridState.firstVisibleItemScrollOffset)
        }
            .collect { (currentItemIndex, currentItemScrollOffset) ->
                if (currentItemIndex == 0 && currentItemScrollOffset == 0) onShowLabelsStateChange(true)
                else if (currentItemIndex > previousItemIndex) onShowLabelsStateChange(false)
                else if (currentItemIndex < previousItemIndex) onShowLabelsStateChange(true)
                else {
                    val delta = currentItemScrollOffset - previousItemScrollOffset
                    if (delta > threshold) onShowLabelsStateChange(false)
                    else if (delta < -threshold) onShowLabelsStateChange(true)
                }

                previousItemIndex = currentItemIndex
                previousItemScrollOffset = currentItemScrollOffset
            }
    }
}