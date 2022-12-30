package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScrollableRow(
    modifier: Modifier = Modifier,
    gradientColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable RowScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val canScrollToStart = scrollState.value > 0
    val canScrollToEnd = scrollState.value < scrollState.maxValue
    val gradientColors = listOf(Color.Transparent, gradientColor)
    Box(
        modifier = modifier.drawWithContent {
            drawContent()
            if (canScrollToStart) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors.reversed(),
                        endX = 150f
                    )
                )
            }
            if (canScrollToEnd) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = gradientColors,
                        startX = 750f
                    )
                )
            }
        }
    ) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}