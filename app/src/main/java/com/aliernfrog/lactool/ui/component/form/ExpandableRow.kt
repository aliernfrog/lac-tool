package com.aliernfrog.lactool.ui.component.form

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveButtonRow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BaseExpandableRow(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    minimizedContainerColor: Color = Color.Transparent,
    expandedContainerColor: Color = getExpandableRowDefaultExpandedContainerColor(),
    onClickHeader: () -> Unit,
    header: @Composable RowScope.(containerColor: Color, contentColor: Color) -> Unit,
    expandedContent: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val containerColor by animateColorAsState(
        if (expanded) expandedContainerColor else minimizedContainerColor
    )
    val contentColor by animateColorAsState(contentColorFor(containerColor))

    Column(
        modifier = modifier.background(containerColor)
    ) {
        Row(
            Modifier.clickable(onClick = onClickHeader)
        ) {
            header(containerColor, contentColor)
        }
        FadeVisibility(
            visible = expanded,
            content = {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    expandedContent()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandableRow(
    expanded: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    showTrailingComponent: Boolean = true,
    minimizedHeaderTrailingButtonText: String? = null,
    minimizedContainerColor: Color = Color.Transparent,
    expandedContainerColor: Color = getExpandableRowDefaultExpandedContainerColor(),
    onClickHeader: () -> Unit,
    expandedContent: @Composable AnimatedVisibilityScope.() -> Unit
) {
    BaseExpandableRow(
        expanded = expanded,
        modifier = modifier,
        minimizedContainerColor = minimizedContainerColor,
        expandedContainerColor = expandedContainerColor,
        onClickHeader = onClickHeader,
        header = { containerColor, contentColor ->
            ExpressiveButtonRow(
                title = title,
                description = description,
                icon = icon,
                trailingComponent = if (showTrailingComponent) minimizedHeaderTrailingButtonText?.let { trailingButtonText -> {
                    Crossfade(
                        targetState = expanded
                    ) { showMinimizeButton ->
                        Box(
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (showMinimizeButton) ToggleExpandButton(
                                expanded = false,
                                onClick = onClickHeader,
                                modifier = Modifier.rotate(180f)
                            )
                            FilledTonalButton(
                                onClick = onClickHeader,
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier.alpha(if (showMinimizeButton) 0f else 1f)
                            ) {
                                Text(text = trailingButtonText)
                            }
                        }
                    }
                } } ?: {
                    ToggleExpandButton(
                        expanded = expanded,
                        onClick = onClickHeader
                    )
                } else null,
                containerColor = containerColor,
                contentColor = contentColor,
                onClick = onClickHeader
            )
        },
        expandedContent = expandedContent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToggleExpandButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
    FilledTonalIconButton(
        shapes = IconButtonDefaults.shapes(),
        onClick = onClick,
        modifier = modifier.rotate(rotation)
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null
        )
    }
}

@Composable
fun getExpandableRowDefaultExpandedContainerColor(fraction: Float = 0.15f) = lerp(
    MaterialTheme.colorScheme.surfaceContainerHigh,
    MaterialTheme.colorScheme.primary,
    fraction = fraction
)