package com.aliernfrog.lactool.ui.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.LACToolComposableShape

@Composable
fun LACToolButtonCentered(title: String, modifier: Modifier = Modifier, containerColor: Color = MaterialTheme.colorScheme.secondary, contentColor: Color = MaterialTheme.colorScheme.onSecondary, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(all = 8.dp).fillMaxWidth(),
        shape = LACToolComposableShape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}