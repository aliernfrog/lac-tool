package com.aliernfrog.lactool.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.LACToolComposableShape

@Composable
fun LACToolColorButton(
    label: String,
    description: String? = null,
    color: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = contentColor),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).weight(1f)) {
            Text(text = label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (!description.isNullOrBlank()) Text(text = description, color = contentColor, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .height(44.dp)
                .width(44.dp)
                .padding(1.dp)
                .clip(LACToolComposableShape)
                .background(color)
                .border(BorderStroke(1.dp, contentColor), LACToolComposableShape),
            content = {}
        )
    }
}