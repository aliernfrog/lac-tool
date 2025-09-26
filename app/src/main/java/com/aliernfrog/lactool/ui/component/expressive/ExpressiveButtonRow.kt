package com.aliernfrog.lactool.ui.component.expressive

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.util.extension.clickableWithColor

@Composable
fun ExpressiveButtonRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    trailingComponent: @Composable (() -> Unit)? = null,
    containerColor: Color = Color.Transparent,
    contentColor: Color =
        if (containerColor == Color.Transparent) MaterialTheme.colorScheme.onSurface
        else contentColorFor(containerColor),
    iconSize: Dp = ROW_DEFAULT_ICON_SIZE,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = modifier
            .heightIn(56.dp)
            .fillMaxWidth()
            .background(containerColor)
            .let {
                if (onClick != null && enabled) it.clickableWithColor(
                    color = contentColor,
                    interactionSource = interactionSource,
                    onClick = onClick
                ) else it
            }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressiveRowHeader(
            title = title,
            description = description,
            icon = icon,
            contentColor = contentColor,
            iconSize = iconSize,
            modifier = Modifier
                .alpha(if (enabled) 1f else 0.7f)
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    vertical = 8.dp,
                    horizontal = 18.dp
                )
        )
        trailingComponent?.let {
            Column(Modifier.padding(end = 8.dp)) {
                trailingComponent()
            }
        }
    }
}