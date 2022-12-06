package com.aliernfrog.lactool.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.LACToolComposableShape

@Composable
fun LACToolButtonRounded(
    title: String,
    description: String? = null,
    painter: Painter? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    painterTintColor: Color? = contentColor,
    enabled: Boolean = true,
    onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        enabled = enabled,
        shape = LACToolComposableShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(8.dp)
    ) {
        if (painter != null) Image(painter, title, Modifier.padding(end = 4.dp).size(40.dp).padding(4.dp), colorFilter = if (painterTintColor != null) ColorFilter.tint(painterTintColor) else null)
        Column(Modifier.fillMaxWidth()) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (description != null) Text(description, Modifier.alpha(0.8f), fontSize = 12.sp)
        }
    }
}