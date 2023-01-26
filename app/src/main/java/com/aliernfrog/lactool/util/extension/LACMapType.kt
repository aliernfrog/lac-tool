package com.aliernfrog.lactool.util.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.lactool.R

@Composable
fun LACMapType.getName(): String {
    return stringResource(
        when(this.index) {
            1 -> R.string.mapsEdit_mapType_1
            2 -> R.string.mapsEdit_mapType_2
            3 -> R.string.mapsEdit_mapType_3
            4 -> R.string.mapsEdit_mapType_4
            5 -> R.string.mapsEdit_mapType_5
            else -> R.string.mapsEdit_mapType_0
        }
    )
}