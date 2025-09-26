package com.aliernfrog.lactool.ui.component.form

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
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
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveButtonRow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandableRow(
    expanded: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    minimizedHeaderTrailingButtonText: String? = null,
    minimizedContainerColor: Color = Color.Transparent,
    expandedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClickHeader: () -> Unit,
    expandedContent: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val containerColor by animateColorAsState(
        if (expanded) expandedContainerColor else minimizedContainerColor
    )
    val contentColor by animateColorAsState(contentColorFor(containerColor))

    Column(
        modifier = modifier.background(containerColor)
    ) {
        Row {
            ExpressiveButtonRow(
                title = title,
                description = description,
                icon = icon,
                trailingComponent = minimizedHeaderTrailingButtonText?.let { trailingButtonText -> {
                    Crossfade(
                        targetState = expanded
                    ) { showMinimizeButton ->
                        Box(
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (showMinimizeButton) ToggleExpandButton(
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
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                    ToggleExpandButton(
                        onClick = onClickHeader,
                        modifier = Modifier.rotate(rotation)
                    )
                },
                containerColor = containerColor,
                contentColor = contentColor,
                onClick = onClickHeader
            )
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

@Composable
private fun ToggleExpandButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalIconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            modifier = modifier
        )
    }
}