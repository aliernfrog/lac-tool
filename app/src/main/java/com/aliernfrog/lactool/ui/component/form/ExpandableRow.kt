package com.aliernfrog.lactool.ui.component.form

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.component.FadeVisibilityColumn
import com.aliernfrog.lactool.ui.theme.AppRoundnessSize

@Composable
fun ExpandableRow(
    expanded: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    trailingButtonText: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    minimizedHeaderColor: Color = Color.Transparent,
    minimizedHeaderContentColor: Color =
        if (minimizedHeaderColor == Color.Transparent) MaterialTheme.colorScheme.onSurface
        else contentColorFor(minimizedHeaderColor),
    expandedHeaderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    expandedHeaderContentColor: Color = contentColorFor(expandedHeaderColor),
    expandedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    expandedPadding: Dp = 8.dp,
    onClickHeader: () -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    val headerColor by animateColorAsState(
        if (expanded) expandedHeaderColor else minimizedHeaderColor
    )
    val headerContentColor by animateColorAsState(
        if (expanded) expandedHeaderContentColor else minimizedHeaderContentColor
    )

    BaseExpandableRow(
        expanded = expanded,
        modifier = modifier,
        headerContent = {
            ButtonRow(
                title = title,
                description = description,
                painter = painter,
                expanded = if (trailingButtonText == null) expanded else null,
                trailingComponent = trailingButtonText?.let { {
                    FilledTonalButton(
                        onClick = onClickHeader
                    ) {
                        Text(text = trailingButtonText)
                    }
                } },
                containerColor = headerColor,
                contentColor = headerContentColor,
                onClick = onClickHeader
            )
        },
        backgroundColor = backgroundColor,
        expandedContainerColor = expandedContainerColor,
        expandedPadding = expandedPadding,
        expandedContent = expandedContent
    )
}

@Composable
fun BaseExpandableRow(
    expanded: Boolean,
    headerContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    expandedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    expandedPadding: Dp = 8.dp,
    expandedContent: @Composable ColumnScope.() -> Unit,
) {
    val roundness by animateDpAsState(
        if (expanded) AppRoundnessSize else 0.dp
    )
    val padding by animateDpAsState(
        if (expanded) expandedPadding else 0.dp
    )
    val elevation by animateDpAsState(
        if (expanded) 2.dp else 0.dp
    )
    val containerColor by animateColorAsState(
        if (expanded) expandedContainerColor else Color.Transparent
    )
    val contentColor = contentColorFor(containerColor)
    val shape = RoundedCornerShape(roundness)

    Column(
        modifier = modifier
            .padding(padding)
            .shadow(
                elevation = elevation,
                shape = shape
            )
            .clip(shape)
            // adding a fixed background so we don't see behind the scenes of .shadow()
            .background(backgroundColor)
            .background(containerColor)
    ) {
        Row(content = headerContent)
        FadeVisibilityColumn(visible = expanded) {
            DividerRow(
                color = contentColor,
                thickness = 0.5.dp,
                alpha = 0.3f
            )
            expandedContent()
        }
    }
}