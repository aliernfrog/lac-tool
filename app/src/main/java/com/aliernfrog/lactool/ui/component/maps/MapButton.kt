package com.aliernfrog.lactool.ui.component.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.ui.component.form.FormHeader
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.util.extension.clickableWithColor
import com.aliernfrog.lactool.util.extension.getDetails

@Composable
fun MapButton(
    map: LACMap,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = contentColorFor(containerColor),
    showMapThumbnail: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
    fun invertIfRTL(list: List<Color>): List<Color> {
        return if (isRTL) list.reversed() else list
    }

    LaunchedEffect(map) {
        if (map.details.value.isNullOrBlank()) map.getDetails(context)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(8.dp)
            .clip(AppComponentShape)
            .background(containerColor)
            .clickableWithColor(
                color = contentColor,
                onClick = onClick
            )
    ) {
        if (showMapThumbnail) AsyncImage(
            model = map.thumbnailModel,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            placeholder = ColorPainter(containerColor),
            error = ColorPainter(containerColor),
            fallback = ColorPainter(containerColor),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )
        FormHeader(
                title = map.name,
        description = map.details.value ?: "",
        painter = rememberVectorPainter(Icons.Outlined.PinDrop),
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(
                invertIfRTL(
                    listOf(containerColor, Color.Transparent)
                )
            ))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}