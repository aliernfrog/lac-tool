package com.aliernfrog.lactool.ui.component.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
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
    shape: Shape = RectangleShape,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onCheckedChange: (Boolean) -> Unit
) {
    FormRow(
        title = title,
        modifier = modifier,
        description = description,
        painter = painter,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        onClick = {
            onCheckedChange(!checked)
        }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}