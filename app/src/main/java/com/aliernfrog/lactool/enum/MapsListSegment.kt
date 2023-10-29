package com.aliernfrog.lactool.enum

import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel

enum class MapsListSegment(
    val labelId: Int,
    val noMapsTextId: Int,
    val getMaps: (MapsViewModel) -> List<LACMap>
) {
    IMPORTED(
        labelId = R.string.mapsList_imported,
        noMapsTextId = R.string.mapsList_noImportedMaps,
        getMaps = { it.importedMaps }
    ),
    EXPORTED(
        labelId = R.string.mapsList_exported,
        noMapsTextId = R.string.mapsList_noExportedMaps,
        getMaps = { it.exportedMaps }
    )
}