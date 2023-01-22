package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    var newVersionName by mutableStateOf("")
    var newVersionBody by mutableStateOf("")
    var newVersionIsPreRelease by mutableStateOf(false)
    var newVersionDownload by mutableStateOf("")
    var updateDialogShown by mutableStateOf(false)

    init {
        if (autoUpdatesEnabled) runBlocking {
            checkUpdates(context)
        }
    }

    suspend fun checkUpdates(
        context: Context,
        manuallyTriggered: Boolean = false,
        ignoreVersion: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            try {
                val json = JSONObject(URL(releaseUrl).readText())
                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val latestBody = json.getString("body")
                val latestIsPreRelease = json.getBoolean("preRelease")
                val latestDownload = json.getString("downloadUrl")
                val isUpToDate = !ignoreVersion && latestVersionCode == currentVersionCode
                if (!isUpToDate) {
                    newVersionName = latestVersionName
                    newVersionBody = latestBody
                    newVersionIsPreRelease = latestIsPreRelease
                    newVersionDownload = latestDownload
                    if (!manuallyTriggered) topToastState.showToast(
                        text = context.getString(R.string.updates_updateAvailable),
                        icon = Icons.Rounded.Update,
                        stayMs = 20000,
                        onToastClick = { updateDialogShown = true }
                    ) else updateDialogShown = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (manuallyTriggered) TODO()
            }
        }
    }
}