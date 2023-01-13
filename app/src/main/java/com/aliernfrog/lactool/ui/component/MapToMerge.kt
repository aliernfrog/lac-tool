package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.AppComposableShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMapToMerge
import com.aliernfrog.lactool.util.extension.clickableWithColor
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

@Composable
fun MapToMerge(
    mapToMerge: LACMapToMerge,
    isBaseMap: Boolean = false,
    expanded: Boolean = false,
    showExpandedIndicator: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    expandedHeaderContainerColor: Color = contentColor.copy(0.4f),
    expandedHeaderContentColor: Color = contentColorFor(expandedHeaderContainerColor),
    onMakeBase: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val animatedRotation = animateFloatAsState(if (expanded) 0f else 180f)
    val isCoordsValid = GeneralUtil.parseAsXYZ(mapToMerge.mergePosition.value) != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(AppComposableShape)
            .background(containerColor)
            .animateContentSize()
    ) {
        Crossfade(targetState = expanded) {
            val headerContainerColor = if (it) expandedHeaderContainerColor else containerColor
            val headerContentColor = if (it) expandedHeaderContentColor else contentColor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerContainerColor)
                    .clickableWithColor(headerContentColor, onClick)
                    .padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Outlined.PinDrop),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp).size(40.dp).padding(1.dp),
                        tint = headerContentColor
                    )
                    Text(
                        text = mapToMerge.map.name,
                        color = headerContentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )
                    if (showExpandedIndicator) Image(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).rotate(animatedRotation.value),
                        colorFilter = ColorFilter.tint(headerContentColor)
                    )
                }
                if (isBaseMap) Text(
                    text = stringResource(R.string.mapsMerge_base_description),
                    color = headerContentColor,
                    fontSize = 15.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 8.dp).alpha(0.85f)
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                if (!isBaseMap) TextField(
                    label = { Text(stringResource(R.string.mapsMerge_map_position)) },
                    supportingText = { Text(stringResource(
                        if (isCoordsValid) R.string.mapsMerge_map_position_description
                        else R.string.mapsMerge_map_position_invalid
                    )) },
                    isError = !isCoordsValid,
                    value = mapToMerge.mergePosition.value,
                    onValueChange = { mapToMerge.mergePosition.value = it },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Switch(
                    title = stringResource(R.string.mapsMerge_map_includeSpawnpoints),
                    checked = mapToMerge.mergeSpawnpoints.value,
                    onCheckedChange = { mapToMerge.mergeSpawnpoints.value = it },
                    contentColor = contentColor
                )
                Switch(
                    title = stringResource(R.string.mapsMerge_map_includeRacingCheckpoints),
                    checked = mapToMerge.mergeRacingCheckpoints.value,
                    onCheckedChange = { mapToMerge.mergeRacingCheckpoints.value = it },
                    contentColor = contentColor
                )
                Switch(
                    title = stringResource(R.string.mapsMerge_map_includeTDMSpawnpoints),
                    checked = mapToMerge.mergeTDMSpawnpoints.value,
                    onCheckedChange = { mapToMerge.mergeTDMSpawnpoints.value = it },
                    contentColor = contentColor
                )
                if (!isBaseMap) ButtonShapeless(
                    title = stringResource(R.string.mapsMerge_map_makeBase),
                    painter = rememberVectorPainter(Icons.Rounded.Home),
                    contentColor = contentColor
                ) {
                    onMakeBase()
                }
                ButtonShapeless(
                    title = stringResource(R.string.mapsMerge_map_remove),
                    painter = rememberVectorPainter(Icons.Rounded.Close),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    onRemove()
                }
            }
        }
    }
}