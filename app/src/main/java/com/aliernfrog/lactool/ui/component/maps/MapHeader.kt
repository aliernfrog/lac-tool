package com.aliernfrog.lactool.ui.component.maps

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowHeader
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowIcon

@Composable
fun MapHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textShadowColor: Color? = null
) {
    ExpressiveRowHeader(
        title = title,
        description = description,
        icon = painter?.let { {
            ExpressiveRowIcon(
                painter = painter,
                containerColor = iconContainerColor
            )
        } },
        textShadow = textShadowColor?.let {
            Shadow(
                color = textShadowColor,
                offset = Offset(2f, 3f),
                blurRadius = 5f
            )
        },
        contentColor = contentColor,
        modifier = modifier
    )
}