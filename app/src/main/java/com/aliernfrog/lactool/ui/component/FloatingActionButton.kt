package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FloatingActionButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    text: String? = null,
    showText: Boolean = true,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    onClick: () -> Unit
) {
    if (text == null) FloatingActionButton(
        onClick = onClick,
        modifier = modifier.systemBarsPadding(),
        shape = RoundedCornerShape(16.dp),
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
    } else SmallExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.systemBarsPadding(),
        shape = RoundedCornerShape(16.dp),
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Row(Modifier.animateContentSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(showText) {
                Text(
                    text = text,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}