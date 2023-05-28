package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ui.theme.AppComponentShape

@Composable
fun SegmentedButtons(
    options: List<String>,
    initialIndex: Int = 0,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    selectedBackgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSecondary,
    onSelect: (Int) -> Unit
) {
    val (selectedIndex, onOptionSelect) = remember { mutableIntStateOf(initialIndex) }
    Crossfade(targetState = selectedIndex) {
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
    }
}