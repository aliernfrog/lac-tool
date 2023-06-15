package com.aliernfrog.lactool.ui.component.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FormSection(
    title: String?,
    modifier: Modifier = Modifier,
    topDivider: Boolean = false,
    bottomDivider: Boolean = true,
    innerModifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
        if (topDivider) DividerRow()

        if (title != null) Text(
            text = title,
            color = titleColor,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )
        Column(
            modifier = innerModifier,
            content = content
        )

        if (bottomDivider) DividerRow()
    }
}