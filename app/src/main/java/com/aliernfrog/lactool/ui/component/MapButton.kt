package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.AppComposableShape
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.util.extension.clickableWithColor
import com.aliernfrog.lactool.util.staticutil.FileUtil

@Composable
fun MapButton(
    map: LACMap,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    showMapThumbnail: Boolean = true,
    expanded: Boolean? = null,
    expandable: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth().padding(8.dp).clip(AppComposableShape).background(containerColor).clickableWithColor(
        color = contentColor,
        onClick = onClick
    )) {
        Box(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            if (showMapThumbnail) AsyncImage(
                model = map.thumbnailPainterModel,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                placeholder = ColorPainter(containerColor),
                error = ColorPainter(containerColor),
                fallback = ColorPainter(containerColor),
                contentScale = ContentScale.Crop,
                alpha = 0.5f
            )
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(containerColor,Color.Transparent)))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Outlined.PinDrop),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp).size(40.dp).padding(1.dp),
                    tint = contentColor
                )
                Column {
                    Text(text = map.name, color = contentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (map.lastModified != null) Text(text = FileUtil.lastModifiedFromLong(map.lastModified, context), modifier = Modifier.alpha(0.9f), color = contentColor, fontSize = 12.sp)
                }
            }
        }
        if (expanded != null) {
            AnimatedVisibility(
                visible = expanded == true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(horizontal = 8.dp)) {
                    expandable()
                }
            }
        }
    }
}