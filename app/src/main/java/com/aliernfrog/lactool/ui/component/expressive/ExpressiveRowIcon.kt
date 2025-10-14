package com.aliernfrog.lactool.ui.component.expressive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpressiveRowIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    shape: Shape = CircleShape,
    iconSize: Dp = 24.dp
) {
    Icon(
        painter = painter,
        contentDescription = null,
        tint = contentColor,
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .padding(8.dp)
            .alpha(0.75f)
            .size(iconSize)
    )
}