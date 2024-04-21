package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.util.extension.appHasPermissions
import com.aliernfrog.lactool.util.manager.PreferenceManager

class PermissionsViewModel(
    val prefs: PreferenceManager
) : ViewModel() {
    private var storageAccessType: StorageAccessType
        get() = StorageAccessType.entries[prefs.storageAccessType]
        set(value) { value.enable(prefs) }

    var showShizukuIntroDialog by mutableStateOf(false)
    var showFilesDowngradeDialog by mutableStateOf(false)

    fun hasPermissions(
        vararg permissionsData: PermissionData,
        isShizukuFileServiceRunning: Boolean,
        context: Context
    ): Boolean {
        return when (storageAccessType) {
            StorageAccessType.SAF -> getMissingUriPermissions(
                *permissionsData, context = context
            ).isEmpty()
            StorageAccessType.SHIZUKU -> isShizukuFileServiceRunning
        }
    }

    fun getMissingUriPermissions(
        vararg permissionsData: PermissionData,
        context: Context
    ): List<PermissionData> {
        return permissionsData.filter {
            !Uri.parse(it.getUri()).appHasPermissions(context)
        }
    }
}