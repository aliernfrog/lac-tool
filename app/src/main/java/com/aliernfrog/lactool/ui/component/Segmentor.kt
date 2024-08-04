package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.theme.AppSmallComponentShape

@Composable
fun VerticalSegmentor(
    vararg components: (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    shape: Shape = AppComponentShape
) {
    val visibleItemIndexes = remember { mutableStateListOf<Int>() }
    val firstVisibleItemIndex = visibleItemIndexes.minOfOrNull { it }
    Column(
        modifier = modifier.clip(shape)
    ) {
        components.forEachIndexed { index, component ->
            val isStart = firstVisibleItemIndex == index
            val visible = visibleItemIndexes.contains(index)
            val topPadding by animateDpAsState(
                if (visible && !isStart) 2.dp else 0.dp
            )
            Box(
                modifier = Modifier
                    .padding(
                        top = topPadding
                    )
                    .clip(AppSmallComponentShape)
                    .onSizeChanged {
                        val isVisible = it.height > 0
                        if (isVisible && !visibleItemIndexes.contains(index)) visibleItemIndexes.add(index)
                        else if (!isVisible && visibleItemIndexes.contains(index)) visibleItemIndexes.remove(index)
                    }
            ) {
                component()
            }
        }
    }
}

@Composable
fun HorizontalSegmentor(
    vararg components: (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    shape: Shape = AppComponentShape
) {
    val visibleItemIndexes = remember { mutableStateListOf<Int>() }
    val firstVisibleItemIndex = visibleItemIndexes.minOfOrNull { it }
    Row(
        modifier = modifier.clip(shape)
    ) {
        components.forEachIndexed { index, component ->
            val isStart = firstVisibleItemIndex == index
            val visible = visibleItemIndexes.contains(index)
            val startPadding by animateDpAsState(
                if (visible && !isStart) 2.dp else 0.dp
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = startPadding
                    )
                    .clip(AppSmallComponentShape)
                    .onSizeChanged {
                        val isVisible = it.width > 0
                        if (isVisible && !visibleItemIndexes.contains(index)) visibleItemIndexes.add(index)
                        else if (!isVisible && visibleItemIndexes.contains(index)) visibleItemIndexes.remove(index)
                    }
            ) {
                component()
            }
        }
    }
}