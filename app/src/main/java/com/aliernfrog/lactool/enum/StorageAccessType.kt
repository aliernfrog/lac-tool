package com.aliernfrog.lactool.enum

import android.os.Build
import androidx.annotation.StringRes
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil

enum class StorageAccessType(
    @StringRes val label: Int,
    @StringRes val description: Int,
    val minSDK: Int = 0,
    val maxSDK: Int = Integer.MAX_VALUE,
    val enable: (PreferenceManager) -> Unit
) {
    SAF(
        label = R.string.settings_storage_storageAccessType_saf,
        description = R.string.settings_storage_storageAccessType_saf_description,
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
        minSDK = Build.VERSION_CODES.M,
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
        maxSDK = Build.VERSION_CODES.N_MR1,
        enable = {
            it.storageAccessType.value = ALL_FILES.ordinal
            it.lacMapsDir.value = FileUtil.getFilePath(it.lacMapsDir.value)
            it.lacWallpapersDir.value = FileUtil.getFilePath(it.lacWallpapersDir.value)
            it.lacScreenshotsDir.value = FileUtil.getFilePath(it.lacScreenshotsDir.value)
            it.exportedMapsDir.value = FileUtil.getFilePath(it.exportedMapsDir.value)
        }
    )
}

fun StorageAccessType.isCompatible(): Boolean {
    val sdk = Build.VERSION.SDK_INT
    return sdk in minSDK..maxSDK
}