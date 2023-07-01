package com.aliernfrog.lactool.ui.component.form

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@Composable
fun ButtonRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    expanded: Boolean? = null,
    arrowRotation: Float = if (expanded == true) 0f else 180f,
    trailingComponent: @Composable (() -> Unit)? = null,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    containerColor: Color = Color.Transparent,
    contentColor: Color =
        if (containerColor == Color.Transparent) MaterialTheme.colorScheme.onSurface
        else contentColorFor(containerColor),
    onClick: () -> Unit
) {
    val animatedRotation = animateFloatAsState(arrowRotation)
    FormRow(
        title = title,
        modifier = modifier,
        description = description,
        painter = painter,
        shape = shape,
        contentPadding = contentPadding,
        containerColor = containerColor,
        contentColor = contentColor,
        onClick = onClick
    ) {
        expanded?.let {
            Image(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .rotate(animatedRotation.value),
                colorFilter = ColorFilter.tint(contentColor)
            )
        }
        trailingComponent?.let {
            Column(Modifier.padding(horizontal = 8.dp)) {
                trailingComponent()
            }
        }
    }
}

@Composable
fun RoundedButtonRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    expanded: Boolean? = null,
    arrowRotation: Float = if (expanded == true) 0f else 180f,
    trailingComponent: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    onClick: () -> Unit
) {
    ButtonRow(
        title = title,
        modifier = modifier.padding(8.dp),
        description = description,
        painter = painter,
        expanded = expanded,
        arrowRotation = arrowRotation,
        trailingComponent = trailingComponent,
        shape = AppComponentShape,
        containerColor = containerColor,
        contentColor = contentColor,
        onClick = onClick
    )
}