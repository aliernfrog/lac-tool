package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppRoundnessSize

val SEGMENTOR_SMALL_ROUNDNESS = 5.dp
val SEGMENTOR_SPACING = 2.dp
val SEGMENTOR_DEFAULT_ROUNDNESS = AppRoundnessSize

@Composable
fun VerticalSegmentor(
    vararg components: (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    dynamic: Boolean = false,
    itemContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    roundness: Dp = SEGMENTOR_DEFAULT_ROUNDNESS
) {
    val visibleItemIndexes = remember {
        if (dynamic) mutableStateListOf() else components.indices.toMutableList()
    }
    val firstVisibleItemIndex = visibleItemIndexes.minOfOrNull { it }
    val lastVisibleItemIndex = visibleItemIndexes.lastOrNull()

    Column(
        modifier = modifier
    ) {
        components.forEachIndexed { index, component ->
            val isStart = firstVisibleItemIndex == index
            val isEnd = lastVisibleItemIndex == index
            val visible = visibleItemIndexes.contains(index)
            val topPadding by animateDpAsState(
                if (visible && !isStart) SEGMENTOR_SPACING else 0.dp
            )
            Box(
                modifier = Modifier
                    .padding(
                        top = topPadding
                    )
                    .clip(RoundedCornerShape(
                        topStart = if (isStart) roundness else SEGMENTOR_SMALL_ROUNDNESS,
                        topEnd = if (isStart) roundness else SEGMENTOR_SMALL_ROUNDNESS,
                        bottomStart = if (isEnd) roundness else SEGMENTOR_SMALL_ROUNDNESS,
                        bottomEnd = if (isEnd) roundness else SEGMENTOR_SMALL_ROUNDNESS
                    ))
                    .background(itemContainerColor)
                    .let {
                        if (dynamic) it.onSizeChanged { size ->
                            val isVisible = size.height > 0
                            if (isVisible && !visibleItemIndexes.contains(index)) visibleItemIndexes.add(index)
                            else if (!isVisible && visibleItemIndexes.contains(index)) visibleItemIndexes.remove(index)
                        }
                        else it
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
    dynamic: Boolean = false,
    itemContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    roundness: Dp = AppRoundnessSize
) {
    val visibleItemIndexes = if (dynamic) remember { mutableStateListOf() } else remember {
        val list = mutableListOf<Int>()
        for (i in components.indices) list.add(i)
        list
    }
    val firstVisibleItemIndex = visibleItemIndexes.minOfOrNull { it }
    val lastVisibleItemIndex = visibleItemIndexes.lastOrNull()

    Row(
        modifier = modifier
    ) {
        components.forEachIndexed { index, component ->
            val isStart = firstVisibleItemIndex == index
            val isEnd = lastVisibleItemIndex == index
            val visible = visibleItemIndexes.contains(index)
            val startPadding by animateDpAsState(
                if (visible && !isStart) SEGMENTOR_SPACING else 0.dp
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = startPadding
                    )
                    .clip(RoundedCornerShape(
                        topStart = if (isStart) roundness else SEGMENTOR_SMALL_ROUNDNESS,
                        bottomStart = if (isStart) roundness else SEGMENTOR_SMALL_ROUNDNESS,
                        topEnd = if (isEnd) roundness else SEGMENTOR_SMALL_ROUNDNESS,
                        bottomEnd = if (isEnd) roundness else SEGMENTOR_SMALL_ROUNDNESS
                    ))
                    .background(itemContainerColor)
                    .let {
                        if (dynamic) it.onSizeChanged { size ->
                            val isVisible = size.height > 0
                            if (isVisible && !visibleItemIndexes.contains(index)) visibleItemIndexes.add(index)
                            else if (!isVisible && visibleItemIndexes.contains(index)) visibleItemIndexes.remove(index)
                        }
                        else it
                    }
            ) {
                component()
            }
        }
    }
}

@Composable
fun Modifier.verticalSegmentedShape(
    index: Int = 0,
    totalSize: Int = 1,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    spacing: Dp = SEGMENTOR_SPACING
): Modifier {
    val isStart = index == 0
    val isEnd = index == totalSize - 1
    return this
        .padding(top = if (isStart) 0.dp else spacing)
        .clip(RoundedCornerShape(
            topStart = if (isStart) SEGMENTOR_DEFAULT_ROUNDNESS else SEGMENTOR_SMALL_ROUNDNESS,
            topEnd = if (isStart) SEGMENTOR_DEFAULT_ROUNDNESS else SEGMENTOR_SMALL_ROUNDNESS,
            bottomStart = if (isEnd) SEGMENTOR_DEFAULT_ROUNDNESS else SEGMENTOR_SMALL_ROUNDNESS,
            bottomEnd = if (isEnd) SEGMENTOR_DEFAULT_ROUNDNESS else SEGMENTOR_SMALL_ROUNDNESS
        ))
        .background(containerColor)
}