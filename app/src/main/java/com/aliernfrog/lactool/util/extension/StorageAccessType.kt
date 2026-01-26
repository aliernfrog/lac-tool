package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.util.manager.PreferenceManager
import io.github.aliernfrog.pftool_shared.enum.StorageAccessType
import io.github.aliernfrog.pftool_shared.util.staticutil.PFToolSharedUtil
import io.github.aliernfrog.shared.di.getKoinInstance

fun StorageAccessType.enable() {
    val prefs = getKoinInstance<PreferenceManager>()
    prefs.storageAccessType.value = this.ordinal

    val folderPrefs = listOf(
        prefs.lacMapsDir, prefs.lacWallpapersDir, prefs.lacScreenshotsDir, prefs.exportedMapsDir
    )

    folderPrefs.forEach { pref ->
        pref.value = when (this) {
            StorageAccessType.SAF -> PFToolSharedUtil.getTreeUriForPath(pref.value).toString()
            StorageAccessType.SHIZUKU, StorageAccessType.ALL_FILES -> PFToolSharedUtil.getFilePath(pref.value)
        }
    }
}