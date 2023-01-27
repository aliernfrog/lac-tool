package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.util.extension.clickableWithColor

@Composable
fun ButtonWithComponent(
    title: String,
    description: String? = null,
    painter: Painter? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    component: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(44.dp)
            .clickableWithColor(contentColor) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (painter != null) Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(40.dp).padding(1.dp),
            tint = contentColor
        )
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp).weight(1f)) {
            Text(text = title, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (description != null) Text(text = description, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
        }
        Column(Modifier.padding(horizontal = 8.dp)) {
            component()
        }
    }
}