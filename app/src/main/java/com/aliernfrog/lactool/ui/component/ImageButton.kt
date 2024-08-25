package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.util.extension.clickableWithColor

@Composable
fun ImageButton(
    model: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = contentColorFor(containerColor),
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(containerColor)
            .clickableWithColor(
                color = contentColor,
                onClick = onClick
            )
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxWidth()
        )
        overlay()
    }
}

@Composable
fun ImageButtonOverlay(
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .padding(8.dp)
            .clip(AppComponentShape)
            .background(containerColor.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            title?.let {
                Text(
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            content()
        }
    }
}

@Composable
fun ImageButtonInfo(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        icon?.let {
            val density = LocalDensity.current
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(
                    with(density) {
                        18.sp.toDp()
                    }
                )
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}