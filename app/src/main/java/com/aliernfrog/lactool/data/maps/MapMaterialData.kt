package com.aliernfrog.lactool.data.maps

import com.aliernfrog.laclib.data.LACMapDownloadableMaterial

data class MapMaterialData(
    val material: LACMapDownloadableMaterial,
    val local: Boolean,
    val loadSuccess: Boolean
)
