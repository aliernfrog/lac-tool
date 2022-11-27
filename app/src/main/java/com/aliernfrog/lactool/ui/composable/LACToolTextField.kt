package com.aliernfrog.lactool.ui.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.LACToolComposableShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LACToolTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = contentColorFor(containerColor),
    rounded: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(
        textColor = contentColor,
        containerColor = containerColor,
        cursorColor = contentColor,
        selectionColors = TextSelectionColors(handleColor = contentColor, backgroundColor = contentColor.copy(0.5f)),
        focusedLabelColor = contentColor,
        unfocusedLabelColor = contentColor.copy(0.7f),
        placeholderColor = contentColor.copy(0.7f)
    )
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().padding(all = 8.dp).clip(if (rounded) LACToolComposableShape else RectangleShape).animateContentSize(),
        label = label,
        placeholder = placeholder,
        singleLine = singleLine,
        colors = colors
    )
}