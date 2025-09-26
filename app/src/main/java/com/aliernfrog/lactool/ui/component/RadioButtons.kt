package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RadioButtons(
    options: List<String>,
    selectedOptionIndex: Int,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    onSelect: (Int) -> Unit
) {
    options.forEachIndexed { index, option ->
        val selected = selectedOptionIndex == index
        val onSelected = { onSelect(index) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(0.dp)
                .clickable { onSelected() }
                .padding(horizontal = 2.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = { onSelected() },
                colors = colors
            )
            Text(
                text = option,
                color = contentColor
            )
        }
    }
}