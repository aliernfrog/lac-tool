package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.AppComponentShape
import com.aliernfrog.lactool.util.extension.clickableWithColor

@Composable
fun ExpandableColumnRounded(
    title: String,
    description: String? = null,
    painter: Painter? = null,
    headerContainerColor: Color = MaterialTheme.colorScheme.primary,
    headerContentColor: Color = contentColorFor(headerContainerColor),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    expanded: Boolean,
    onExpandedChange: (expanded: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val arrowAnimatedRotation by animateFloatAsState(if (expanded) 0f else 180f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(AppComponentShape)
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .background(headerContainerColor)
                .clickableWithColor(headerContentColor) { onExpandedChange(!expanded) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (painter != null) Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp).size(40.dp).padding(1.dp),
                tint = headerContentColor
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = headerContentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (description != null) Text(
                    text = description,
                    color = headerContentColor,
                    modifier = Modifier.alpha(0.8f),
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.rotate(arrowAnimatedRotation),
                tint = headerContentColor
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column { content() }
        }
    }
}