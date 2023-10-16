package com.aliernfrog.lactool.ui.component.form

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DividerRow(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = DividerDefaults.color,
    alpha: Float = 0.7f
) {
    HorizontalDivider(
        modifier = modifier.alpha(alpha),
        thickness = thickness,
        color = color
    )
}