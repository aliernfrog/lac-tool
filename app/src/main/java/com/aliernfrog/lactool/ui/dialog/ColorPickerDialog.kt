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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolDialog
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker

@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onColorPick: (Color) -> Unit,
    initialColor: Color = Color.White
) {
    val configuration = LocalConfiguration.current
    val color = remember { mutableStateOf(initialColor) }
    LACToolDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.action_pickColor),
        icon = rememberVectorPainter(Icons.Filled.Palette)
    ) {
        HarmonyColorPicker(
            modifier = Modifier.height((configuration.screenHeightDp/2).dp),
            harmonyMode = ColorHarmonyMode.NONE,
            onColorChanged = { color.value = it.toColor() },
            color = color.value
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onColorPick(color.value) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Done")
            }
        }
    }
}