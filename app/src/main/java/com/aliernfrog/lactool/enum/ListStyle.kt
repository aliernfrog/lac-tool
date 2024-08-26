package com.aliernfrog.lactool.enum

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliernfrog.lactool.R

enum class ListStyle(
    @StringRes val label: Int,
    val iconVector: ImageVector
) {
    LIST(
        label = R.string.list_style_list,
        iconVector = Icons.Default.ViewStream
    ),
    GRID(
        label = R.string.list_style_grid,
        iconVector = Icons.Default.GridView
    )
}