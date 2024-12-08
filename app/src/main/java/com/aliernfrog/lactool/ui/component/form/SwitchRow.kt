package com.aliernfrog.lactool.ui.component.form

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    FormRow(
        title = title,
        modifier = modifier,
        description = description,
        painter = painter,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor.let {
            if (enabled) it else it.copy(alpha = 0.7f)
        },
        interactionSource = interactionSource,
        onClick = {
            if (enabled) onCheckedChange(!checked)
        }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}