package com.aliernfrog.lactool.ui.component.expressive

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpressiveRowHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    textShadow: Shadow? = null,
    icon: (@Composable () -> Unit)? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = ROW_DEFAULT_ICON_SIZE,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(iconSize)
            ) {
                it()
            }
        }
        Column {
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 19.sp,
                    shadow = textShadow
                ),
                modifier = Modifier.animateContentSize()
            )
            description?.let {
                Text(
                    text = description,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        shadow = textShadow
                    ),
                    modifier = Modifier
                        .alpha(0.7f)
                        .animateContentSize()
                )
            }
        }
    }
}

