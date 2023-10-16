package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * An icon designed to use in Material 3 buttons.
 */
@Composable
fun ButtonIcon(
    painter: Painter,
    contentDescription: String? = null
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .padding(end = 8.dp)
            .size(18.dp)
    )
}