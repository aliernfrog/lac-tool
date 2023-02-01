package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Update
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class UpdateState(
    private val topToastState: TopToastState,
    config: SharedPreferences,
    context: Context
) {
    private val releaseUrl = config.getString(ConfigKey.KEY_APP_UPDATES_URL, ConfigKey.DEFAULT_UPDATES_URL)!!
    private val autoUpdatesEnabled = config.getBoolean(ConfigKey.KEY_APP_AUTO_UPDATES, true)
    private val currentVersionCode = GeneralUtil.getAppVersionCode(context)
    private val isCurrentAlpha = GeneralUtil.getAppVersionName(context).contains("-alpha")

    var newVersionName by mutableStateOf("")
    var newVersionBody by mutableStateOf("")
    var newVersionDownload by mutableStateOf("")
    var updateDialogShown by mutableStateOf(false)

    init {
        if (autoUpdatesEnabled) CoroutineScope(Dispatchers.Main).launch {
            checkUpdates()
        }
    }

    suspend fun checkUpdates(
        manuallyTriggered: Boolean = false,
        ignoreVersion: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            try {
                val responseJson = JSONObject(URL(releaseUrl).readText())
                val branchKey = if (isCurrentAlpha && responseJson.has("preRelease")) "preRelease" else "stable"
                val json = responseJson.getJSONObject(branchKey)
                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val latestBody = json.getString("body")
                val latestDownload = json.getString("downloadUrl")
                val isUpToDate = !ignoreVersion && latestVersionCode <= currentVersionCode
                if (!isUpToDate) {
                    newVersionName = latestVersionName
                    newVersionBody = latestBody
                    newVersionDownload = latestDownload
                    if (!manuallyTriggered) topToastState.showToast(
                        text = R.string.updates_updateAvailable,
                        icon = Icons.Rounded.Update,
                        stayMs = 20000,
                        onToastClick = { updateDialogShown = true }
                    ) else updateDialogShown = true
                } else {
                    if (manuallyTriggered) topToastState.showToast(
                        text = R.string.updates_noUpdates,
                        icon = Icons.Rounded.Info,
                        iconTintColor = TopToastColor.ON_SURFACE
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (manuallyTriggered) topToastState.showToast(
                    text = R.string.updates_error,
                    icon = Icons.Rounded.PriorityHigh,
                    iconTintColor = TopToastColor.ERROR
                )
            }
        }
    }
}