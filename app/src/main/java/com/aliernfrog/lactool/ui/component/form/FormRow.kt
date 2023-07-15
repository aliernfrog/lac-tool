package com.aliernfrog.lactool.ui.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    containerColor: Color = Color.Transparent,
    contentColor: Color = if (containerColor == Color.Transparent)
        MaterialTheme.colorScheme.onSurface else contentColorFor(containerColor),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(56.dp)
            .clip(shape)
            .background(containerColor)
            .clickableWithColor(
                color = contentColor,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .padding(contentPadding),
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
        )
        content()
    }
}