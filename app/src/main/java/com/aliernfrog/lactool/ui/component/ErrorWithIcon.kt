package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ErrorWithIcon(
    error: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    FadeVisibility(visible) {
        Column(
            modifier = modifier.padding(8.dp).alpha(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = contentColor
            )
            Text(
                text = error,
                textAlign = TextAlign.Center,
                color = contentColor
            )
        }
    }
}