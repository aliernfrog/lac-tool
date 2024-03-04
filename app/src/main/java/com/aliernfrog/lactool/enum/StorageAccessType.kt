package com.aliernfrog.lactool.enum

import androidx.annotation.StringRes
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil

enum class StorageAccessType(
    @StringRes val label: Int,
    @StringRes val description: Int,
    val enable: (PreferenceManager) -> Unit
) {
    SAF(
        label = R.string.settings_storage_storageAccessType_saf,
        description = R.string.settings_storage_storageAccessType_saf_description,
        enable = {
            it.storageAccessType = SAF.ordinal
            it.lacMapsDir = FileUtil.getTreeUriForPath(it.lacMapsDir).toString()
            it.exportedMapsDir = FileUtil.getTreeUriForPath(it.exportedMapsDir).toString()
        }
    ),

    SHIZUKU(
        label = R.string.settings_storage_storageAccessType_shizuku,
        description = R.string.settings_storage_storageAccessType_shizuku_description,
        enable = {
            it.storageAccessType = SHIZUKU.ordinal
            it.lacMapsDir = FileUtil.getFilePath(it.lacMapsDir) ?: ConfigKey.RECOMMENDED_MAPS_DIR
            it.exportedMapsDir = FileUtil.getFilePath(it.exportedMapsDir) ?: ConfigKey.RECOMMENDED_EXPORTED_MAPS_DIR
        }
    )
}