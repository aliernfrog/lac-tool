package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@Composable
fun CardWithActions(
    title: String?,
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    buttons: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = AppComponentShape,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title != null || painter != null) Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                painter?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            content()
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                buttons()
            }
        }
    }
}