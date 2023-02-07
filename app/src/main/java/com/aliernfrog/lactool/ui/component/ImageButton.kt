package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.aliernfrog.lactool.AppComponentShape
import com.aliernfrog.lactool.util.extension.clickableWithColor

@Composable
fun ImageButton(
    model: Any?,
    title: String,
    description: String? = null,
    painter: Painter? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    showDetails: Boolean = true,
    onError: (AsyncImagePainter.State.Error) -> Unit = {},
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(AppComponentShape)
            .background(containerColor)
            .clickableWithColor(contentColor) { onClick() }
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
            onError = onError
        )
        if (showDetails) Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor.copy(alpha = 0.8f))
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (painter != null) Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp).size(40.dp).padding(1.dp),
                tint = contentColor
            )
            Column {
                Text(
                    text = title,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                if (description != null) Text(
                    text = description,
                    color = contentColor,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.alpha(0.6f)
                )
            }
        }
    }
}