package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun ChipIcon(
    painter: Painter,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .size(18.dp)
    )
}