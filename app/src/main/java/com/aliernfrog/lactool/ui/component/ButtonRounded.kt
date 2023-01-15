package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.AppComponentShape

@Composable
fun ButtonRounded(
    title: String,
    description: String? = null,
    painter: Painter? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    painterTintColor: Color = contentColor,
    enabled: Boolean = true,
    onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        enabled = enabled,
        shape = AppComponentShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(8.dp)
    ) {
        if (painter != null) Icon(painter, title, Modifier.padding(end = 4.dp).size(40.dp).padding(1.dp), tint = painterTintColor)
        Column(Modifier.fillMaxWidth()) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (description != null) Text(description, Modifier.alpha(0.8f), fontSize = 12.sp)
        }
    }
}