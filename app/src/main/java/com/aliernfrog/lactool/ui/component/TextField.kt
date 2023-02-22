package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.AppComponentShape
import com.aliernfrog.lactool.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: Painter? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    doneIconShown: Boolean = false,
    doneIcon: Painter = rememberVectorPainter(Icons.Rounded.Done),
    onDone: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = contentColorFor(containerColor),
    rounded: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(
        focusedTextColor = contentColor,
        unfocusedTextColor = contentColor,
        containerColor = containerColor,
        cursorColor = contentColor,
        selectionColors = TextSelectionColors(handleColor = contentColor, backgroundColor = contentColor.copy(0.5f)),
        focusedLabelColor = contentColor,
        unfocusedLabelColor = contentColor.copy(0.7f),
        focusedPlaceholderColor = contentColor.copy(0.7f),
        unfocusedPlaceholderColor = contentColor.copy(0.7f)
    )
) {
    Box(
        modifier = modifier
            .padding(all = 8.dp)
            .clip(if (rounded) AppComponentShape else RectangleShape)
            .background(containerColor)
            .padding(bottom = if (supportingText != null) 4.dp else 0.dp)
            .animateContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon?.let { { Icon(it, null) } },
            supportingText = supportingText,
            isError = isError,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            colors = colors
        )
        AnimatedVisibility(
            visible = doneIconShown,
            modifier = Modifier.padding(end = 16.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = { onDone?.invoke() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = contentColor,
                    contentColor = containerColor
                )
            ) {
                Icon(
                    painter = doneIcon,
                    contentDescription = stringResource(R.string.action_done)
                )
            }
        }
    }
}