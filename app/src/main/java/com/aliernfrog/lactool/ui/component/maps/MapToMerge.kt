package com.aliernfrog.lactool.ui.component.maps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aliernfrog.laclib.util.LACLibUtil
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.laclib.MutableMapToMerge
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.SwitchRow

@Composable
fun MapToMerge(
    mapToMerge: MutableMapToMerge,
    isBaseMap: Boolean = false,
    expanded: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    onUpdateState: () -> Unit,
    onMakeBase: () -> Unit,
    onRemove: () -> Unit,
    onClickHeader: () -> Unit
) {
    val isCoordsValid = LACLibUtil.parseAsXYZ(mapToMerge.mergePosition) != null
    ExpandableRow(
        expanded = expanded,
        title = mapToMerge.mapName,
        description = if (!isBaseMap) null else stringResource(R.string.mapsMerge_base_description),
        painter = rememberVectorPainter(
            if (isBaseMap) Icons.Rounded.Home else Icons.Rounded.PinDrop
        ),
        onClickHeader = onClickHeader
    ) {
        if (!isBaseMap) OutlinedTextField(
            value = mapToMerge.mergePosition,
            onValueChange = {
                mapToMerge.mergePosition = it
                onUpdateState()
            },
            label = { Text(stringResource(R.string.mapsMerge_map_position)) },
            supportingText = { Text(stringResource(
                if (isCoordsValid) R.string.mapsMerge_map_position_description
                else R.string.mapsMerge_map_position_invalid
            )) },
            isError = !isCoordsValid,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                autoCorrectEnabled = false
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        )
        SwitchRow(
            title = stringResource(R.string.mapsMerge_map_includeSpawnpoints),
            checked = mapToMerge.mergeSpawnpoints,
            onCheckedChange = {
                mapToMerge.mergeSpawnpoints = it
                onUpdateState()
            },
            contentColor = contentColor
        )
        SwitchRow(
            title = stringResource(R.string.mapsMerge_map_includeRacingCheckpoints),
            checked = mapToMerge.mergeRacingCheckpoints,
            onCheckedChange = {
                mapToMerge.mergeRacingCheckpoints = it
                onUpdateState()
            },
            contentColor = contentColor
        )
        SwitchRow(
            title = stringResource(R.string.mapsMerge_map_includeTDMSpawnpoints),
            checked = mapToMerge.mergeTDMSpawnpoints,
            onCheckedChange = {
                mapToMerge.mergeTDMSpawnpoints = it
                onUpdateState()
            },
            contentColor = contentColor
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            if (!isBaseMap) OutlinedButton(
                onClick = onMakeBase
            ) {
                ButtonIcon(rememberVectorPainter(Icons.Default.Home))
                Text(stringResource(R.string.mapsMerge_map_makeBase))
            }
            Button(
                onClick = onRemove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                ButtonIcon(rememberVectorPainter(Icons.Default.Close))
                Text(stringResource(R.string.mapsMerge_map_remove))
            }
        }
    }
}