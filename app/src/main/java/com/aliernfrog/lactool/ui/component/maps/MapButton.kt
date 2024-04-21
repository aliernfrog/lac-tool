package com.aliernfrog.lactool.ui.component.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.util.extension.combinedClickableWithColor

@Composable
fun MapButton(
    map: MapFile,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = contentColorFor(containerColor),
    showMapThumbnail: Boolean = true,
    trailingComponent: @Composable () -> Unit = {},
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }

    val isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun invertIfRTL(list: List<Color>): List<Color> {
        return if (isRTL) list.reversed() else list
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(AppComponentShape)
            .background(containerColor)
            .combinedClickableWithColor(
                color = contentColor,
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        if (showMapThumbnail) AsyncImage(
            model = map.thumbnailModel,
            contentDescription = null,
            modifier = Modifier.height(headerHeight).fillMaxWidth(),
            placeholder = ColorPainter(containerColor),
            error = ColorPainter(containerColor),
            fallback = ColorPainter(containerColor),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(Brush.horizontalGradient(
                invertIfRTL(
                    listOf(containerColor, Color.Transparent)
                )
            ))
        ) {
            MapHeader(
                title = map.name,
                description = map.details,
                painter = rememberVectorPainter(Icons.Outlined.PinDrop),
                modifier = Modifier
                    .onSizeChanged {
                        density.run {
                            headerHeight = it.height.toDp()
                        }
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .weight(1f)
            )
            trailingComponent()
        }
    }
}