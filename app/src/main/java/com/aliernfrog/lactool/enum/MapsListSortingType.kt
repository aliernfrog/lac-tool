package com.aliernfrog.lactool.enum

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile

@Suppress("unused")
enum class MapsListSortingType(
    val labelId: Int,
    val iconVector: ImageVector,
    val comparator: Comparator<MapFile>
) {
    ALPHABETICAL(
        labelId = R.string.mapsList_sorting_name,
        iconVector = Icons.Default.SortByAlpha,
        comparator = compareBy(MapFile::name)
    ),

    DATE(
        labelId = R.string.mapsList_sorting_date,
        iconVector = Icons.Default.CalendarMonth,
        comparator = compareByDescending(MapFile::lastModified)
    ),

    SIZE(
        labelId = R.string.mapsList_sorting_size,
        iconVector = Icons.AutoMirrored.Filled.Note,
        comparator = compareByDescending(MapFile::size)
    )
}