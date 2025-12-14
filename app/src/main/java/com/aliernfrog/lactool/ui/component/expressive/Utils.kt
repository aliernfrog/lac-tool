package com.aliernfrog.lactool.ui.component.expressive

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ROW_DEFAULT_ICON_SIZE = 40.dp

val Color.toRowFriendlyColor
    get() = copy(alpha = 0.25f)

@Composable
fun getTextFieldColors(containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh) = TextFieldDefaults.colors(
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    disabledContainerColor = containerColor,
    errorContainerColor = containerColor
)