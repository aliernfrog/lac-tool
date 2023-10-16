package com.aliernfrog.lactool.enum

import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel

enum class PickMapSheetSegments(
    val noMapsTextId: Int,
    val getMaps: (MapsViewModel) -> List<LACMap>
) {
    IMPORTED(
        noMapsTextId = R.string.maps_pickMap_noImportedMaps,
        getMaps = { it.importedMaps }
    ),
    EXPORTED(
        noMapsTextId = R.string.maps_pickMap_noExportedMaps,
        getMaps = { it.exportedMaps }
    )
}