package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@Composable
fun VerticalSegmentedButtons(
    vararg components: (@Composable () -> Unit),
    modifier: Modifier = Modifier
) {
    val visibleItemIndexes = remember { mutableStateListOf<Int>() }
    val firstVisibleItemIndex = visibleItemIndexes.minOfOrNull { it }
    Column(
        modifier = modifier
            .clip(AppComponentShape)
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
                    .clip(RoundedCornerShape(5.dp))
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