package com.aliernfrog.lactool.ui.component.expressive

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun ExpressiveSwitchRow(
    title: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    contentColor: Color =
        if (containerColor == Color.Transparent) MaterialTheme.colorScheme.onSurface
        else contentColorFor(containerColor),
    iconSize: Dp = ROW_DEFAULT_ICON_SIZE,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    ExpressiveButtonRow(
        title = title,
        modifier = modifier,
        description = description,
        icon = icon,
        enabled = enabled,
        trailingComponent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                interactionSource = interactionSource
            )
        },
        containerColor = containerColor,
        contentColor = contentColor,
        iconSize = iconSize,
        interactionSource = interactionSource,
        onClick = {
            onCheckedChange(!checked)
        }
    )
}