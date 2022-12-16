package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.AppComposableShape

@Composable
fun ImageButton(
    model: Any?,
    title: String,
    description: String? = null,
    onClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().padding(8.dp).clip(AppComposableShape).clickable { onClick() }) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            if (description != null) Text(text = description, fontSize = 12.sp, modifier = Modifier.alpha(0.6f))
        }
    }
}