package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.enum.SAFWorkaroundLevel
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.hasAndroidDataRestrictions
import com.aliernfrog.lactool.util.extension.appHasPermissions
import com.aliernfrog.lactool.util.manager.PreferenceManager

class PermissionsViewModel(
    val prefs: PreferenceManager
) : ViewModel() {
    private var storageAccessType: StorageAccessType
        get() = StorageAccessType.entries[prefs.storageAccessType]
        set(value) { value.enable(prefs) }

    var safWorkaroundLevel by mutableStateOf(SAFWorkaroundLevel.entries.first())
    var showSAFWorkaroundDialog by mutableStateOf(false)

    fun pushSAFWorkaroundLevel(): SAFWorkaroundLevel {
        if (!hasAndroidDataRestrictions) return safWorkaroundLevel
        val newIndex = safWorkaroundLevel.ordinal+1
        if (newIndex >= SAFWorkaroundLevel.entries.size) return safWorkaroundLevel
        if (newIndex >= SAFWorkaroundLevel.SETUP_SHIZUKU.ordinal) storageAccessType = StorageAccessType.SHIZUKU
        safWorkaroundLevel = SAFWorkaroundLevel.entries[newIndex]
        return SAFWorkaroundLevel.entries[newIndex]
    }

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