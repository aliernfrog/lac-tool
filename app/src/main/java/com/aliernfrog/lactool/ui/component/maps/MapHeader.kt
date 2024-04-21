package com.aliernfrog.lactool.ui.component.maps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun MapHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    painter: Painter? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(30.dp).padding(1.dp),
                tint = contentColor
            )
        }
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            description?.let {
                Text(
                    text = description,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                )
            }
        }
    }
}