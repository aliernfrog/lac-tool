package com.aliernfrog.lactool.enum

import android.os.Build
import androidx.annotation.StringRes
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil

enum class StorageAccessType(
    @StringRes val label: Int,
    @StringRes val description: Int,
    val isCompatible: () -> Boolean,
    val enable: (PreferenceManager) -> Unit
) {
    SAF(
        label = R.string.settings_storage_storageAccessType_saf,
        description = R.string.settings_storage_storageAccessType_saf_description,
        isCompatible = { true },
        enable = {
            it.storageAccessType.value = SAF.ordinal
            it.lacMapsDir.value = FileUtil.getTreeUriForPath(it.lacMapsDir.value).toString()
            it.lacWallpapersDir.value = FileUtil.getTreeUriForPath(it.lacWallpapersDir.value).toString()
            it.lacScreenshotsDir.value = FileUtil.getTreeUriForPath(it.lacScreenshotsDir.value).toString()
            it.exportedMapsDir.value = FileUtil.getTreeUriForPath(it.exportedMapsDir.value).toString()
        }
    ),

    SHIZUKU(
        label = R.string.settings_storage_storageAccessType_shizuku,
        description = R.string.settings_storage_storageAccessType_shizuku_description,
        isCompatible = {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || isCompatibilityForced()
        },
        enable = {
            it.storageAccessType.value = SHIZUKU.ordinal
            it.lacMapsDir.value = FileUtil.getFilePath(it.lacMapsDir.value)
            it.lacWallpapersDir.value = FileUtil.getFilePath(it.lacWallpapersDir.value)
            it.lacScreenshotsDir.value = FileUtil.getFilePath(it.lacScreenshotsDir.value)
            it.exportedMapsDir.value = FileUtil.getFilePath(it.exportedMapsDir.value)
        }
    ),

    ALL_FILES(
        label = R.string.settings_storage_storageAccessType_allFiles,
        description = R.string.settings_storage_storageAccessType_allFiles_description,
        isCompatible = {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 || isCompatibilityForced()
        },
        enable = {
            it.storageAccessType.value = ALL_FILES.ordinal
            it.lacMapsDir.value = FileUtil.getFilePath(it.lacMapsDir.value)
            it.lacWallpapersDir.value = FileUtil.getFilePath(it.lacWallpapersDir.value)
            it.lacScreenshotsDir.value = FileUtil.getFilePath(it.lacScreenshotsDir.value)
            it.exportedMapsDir.value = FileUtil.getFilePath(it.exportedMapsDir.value)
        }
    )
}

private fun isCompatibilityForced() = getKoinInstance<PreferenceManager>().forceStorageAccessTypeCompatibility.value