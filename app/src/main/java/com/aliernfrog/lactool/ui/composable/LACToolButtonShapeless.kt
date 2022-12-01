package com.aliernfrog.lactool.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.LACToolComposableShape

@Composable
fun LACToolButtonShapeless(
    title: String,
    description: String? = null,
    painter: Painter? = null,
    rounded: Boolean = false,
    expanded: Boolean? = null,
    arrowRotation: Float = if (expanded == true) 0f else 180f,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val animatedRotation = animateFloatAsState(arrowRotation)
    Row(Modifier.fillMaxWidth().heightIn(44.dp)
        .clip(if (rounded) LACToolComposableShape else RectangleShape)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = rememberRipple(color = contentColor),
            onClick = onClick
        )
        .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (painter != null) Image(painter, title, Modifier.padding(end = 4.dp).size(40.dp).padding(4.dp), colorFilter = ColorFilter.tint(contentColor))
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).weight(1f)) {
            Text(text = title, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (description != null) Text(text = description, color = contentColor, fontSize = 14.sp)
        }
        if (expanded != null) Image(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.rotate(animatedRotation.value), colorFilter = ColorFilter.tint(contentColor))
    }
}