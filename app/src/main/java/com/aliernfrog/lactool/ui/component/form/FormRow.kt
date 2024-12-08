package com.aliernfrog.lactool.ui.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.util.extension.clickableWithColor

@Composable
fun FormRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    shape: Shape = RectangleShape,
    containerColor: Color = Color.Transparent,
    contentColor: Color = if (containerColor == Color.Transparent)
        MaterialTheme.colorScheme.onSurface else contentColorFor(containerColor),
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .heightIn(56.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .clickableWithColor(
                color = contentColor,
                interactionSource = interactionSource,
                onClick = onClick
            )
            .padding(end = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormHeader(
            title = title,
            description = description,
            painter = painter,
            contentColor = contentColor,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    vertical = 8.dp,
                    horizontal = 18.dp
                )
        )
        content()
    }
}