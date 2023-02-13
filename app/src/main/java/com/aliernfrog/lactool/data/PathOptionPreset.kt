package com.aliernfrog.lactool.data

data class PathOptionPreset(
    val labelResourceId: Int,
    val lacMapsPath: String? = null,
    val lacWallpapersPath: String? = null,
    val lacScreenshotsPath: String? = null,
    val appMapsExportPath: String? = null
)
