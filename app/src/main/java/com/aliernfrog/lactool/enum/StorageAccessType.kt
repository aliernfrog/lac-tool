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
            it.storageAccessType = SAF.ordinal
            it.lacMapsDir = FileUtil.getTreeUriForPath(it.lacMapsDir).toString()
            it.lacWallpapersDir = FileUtil.getTreeUriForPath(it.lacWallpapersDir).toString()
            it.lacScreenshotsDir = FileUtil.getTreeUriForPath(it.lacScreenshotsDir).toString()
            it.exportedMapsDir = FileUtil.getTreeUriForPath(it.exportedMapsDir).toString()
        }
    ),

    SHIZUKU(
        label = R.string.settings_storage_storageAccessType_shizuku,
        description = R.string.settings_storage_storageAccessType_shizuku_description,
        minSDK = Build.VERSION_CODES.M,
        enable = {
            it.storageAccessType = SHIZUKU.ordinal
            it.lacMapsDir = FileUtil.getFilePath(it.lacMapsDir)
            it.lacWallpapersDir = FileUtil.getFilePath(it.lacWallpapersDir)
            it.lacScreenshotsDir = FileUtil.getFilePath(it.lacScreenshotsDir)
            it.exportedMapsDir = FileUtil.getFilePath(it.exportedMapsDir)
        }
    ),

    ALL_FILES(
        label = R.string.settings_storage_storageAccessType_allFiles,
        description = R.string.settings_storage_storageAccessType_allFiles_description,
        maxSDK = Build.VERSION_CODES.N_MR1,
        enable = {
            it.storageAccessType = ALL_FILES.ordinal
            it.lacMapsDir = FileUtil.getFilePath(it.lacMapsDir)
            it.lacWallpapersDir = FileUtil.getFilePath(it.lacWallpapersDir)
            it.lacScreenshotsDir = FileUtil.getFilePath(it.lacScreenshotsDir)
            it.exportedMapsDir = FileUtil.getFilePath(it.exportedMapsDir)
        }
    )
}

fun StorageAccessType.isCompatible(): Boolean {
    val sdk = Build.VERSION.SDK_INT
    return sdk in minSDK..maxSDK
}