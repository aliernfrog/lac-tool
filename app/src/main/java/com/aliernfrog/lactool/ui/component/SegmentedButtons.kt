package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@Composable
fun SegmentedButtons(
    options: List<String>,
    selectedOptionIndex: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .clip(AppComponentShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = AppComponentShape
            )
            .height(IntrinsicSize.Max)
            .width(IntrinsicSize.Max)
    ) {
        options.forEachIndexed { index, option ->
            val selected = selectedOptionIndex == index
            val containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
            val textColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(containerColor)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    .clickable {
                        onSelect(index)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Done),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = option,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    /*Crossfade(targetState = selectedIndex) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max).padding(8.dp).clip(AppComponentShape).background(backgroundColor).padding(3.dp)) {
            options.forEachIndexed { index, option ->
                val selected = it == index
                Text(
                    text = option,
                    color = if (selected) selectedContentColor else contentColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxSize().weight(1f).clip(AppComponentShape)
                        .clickable { onOptionSelect(index); onSelect(index) }
                        .background(if (selected) selectedBackgroundColor else backgroundColor)
                        .padding(8.dp)
                        .alpha(if (selected) 1f else 0.6f)
                )
            }
        }
    }*/
}