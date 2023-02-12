package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.data.PathOptionPreset
import com.aliernfrog.lactool.data.PrefEditItem

fun PrefEditItem.applyPathOptionPreset(preset: PathOptionPreset) {
    if (this.key == ConfigKey.KEY_MAPS_DIR && preset.lacMapsPath != null) this.mutableValue.value = preset.lacMapsPath
    if (this.key == ConfigKey.KEY_WALLPAPERS_DIR && preset.lacWallpapersPath != null) this.mutableValue.value = preset.lacWallpapersPath
    if (this.key == ConfigKey.KEY_SCREENSHOTS_DIR && preset.lacScreenshotsPath != null) this.mutableValue.value = preset.lacScreenshotsPath
    if (this.key == ConfigKey.KEY_MAPS_EXPORT_DIR && preset.appMapsExportPath != null) this.mutableValue.value = preset.appMapsExportPath
}