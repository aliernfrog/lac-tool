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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge

@Composable
fun ScrollableRow(
    modifier: Modifier = Modifier,
    gradientColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable RowScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .horizontalFadingEdge(
                scrollState = scrollState,
                edgeColor = gradientColor,
                isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
            )
    ) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}