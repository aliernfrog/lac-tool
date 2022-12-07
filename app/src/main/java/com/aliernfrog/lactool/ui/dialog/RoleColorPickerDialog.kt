package com.aliernfrog.lactool.ui.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolDialog
import com.aliernfrog.lactool.ui.composable.LACToolTextField
import com.aliernfrog.lactool.util.extension.toHex
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker

@Composable
fun RoleColorPickerDialog(
    onDismissRequest: () -> Unit,
    onColorPick: (Color) -> Unit,
    onColorClear: () -> Unit,
    initialColorHex: String = "#ffffff"
) {
    val configuration = LocalConfiguration.current
    val color = remember { mutableStateOf(GeneralUtil.parseColor(initialColorHex)) }
    val hexEdit = remember { mutableStateOf(color.value.toHex()) }
    LACToolDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.action_pickColor),
        icon = rememberVectorPainter(Icons.Filled.Palette)
    ) {
        HarmonyColorPicker(
            modifier = Modifier.height((configuration.screenHeightDp/2.5).dp),
            harmonyMode = ColorHarmonyMode.NONE,
            color = color.value,
            onColorChanged = {
                color.value = it.toColor()
                hexEdit.value = it.toColor().toHex()
            }
        )
        LACToolTextField(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            value = hexEdit.value,
            onValueChange = {
                hexEdit.value = it
                color.value = GeneralUtil.parseColor(it)
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.action_cancel))
            }
            Button(
                onClick = onColorClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(stringResource(R.string.action_clear))
            }
            Button(
                onClick = { onColorPick(color.value) }
            ) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}