package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.githubRepoURL
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
class MainViewModel(
    context: Context,
    val prefs: PreferenceManager,
    val topToastState: TopToastState
) : ViewModel() {
    lateinit var scope: CoroutineScope

    val updateSheetState = SheetState(skipPartiallyExpanded = false, Density(context))

    val applicationVersionName = "v${GeneralUtil.getAppVersionName(context)}"
    val applicationVersionCode = GeneralUtil.getAppVersionCode(context)
    private val applicationIsPreRelease = applicationVersionName.contains("-alpha")

    var showAlphaWarningDialog by mutableStateOf(
        applicationIsPreRelease && prefs.lastAlphaAck != applicationVersionName
    )

    var latestVersionInfo by mutableStateOf(ReleaseInfo(
        versionName = applicationVersionName,
        preRelease = applicationIsPreRelease,
        body = context.getString(R.string.updates_noUpdates),
        htmlUrl = githubRepoURL,
        downloadLink = githubRepoURL
    ))
        private set

    var updateAvailable by mutableStateOf(false)
        private set

    suspend fun checkUpdates(
        manuallyTriggered: Boolean = false,
        ignoreVersion: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            try {
                val updatesURL = prefs.updatesURL
                val responseJson = JSONObject(URL(updatesURL).readText())
                val branchKey = if (applicationIsPreRelease && responseJson.has("preRelease")) "preRelease" else "stable"
                val json = responseJson.getJSONObject(branchKey)
                val latestVersionCode = json.getInt("versionCode")
                val latestVersionName = json.getString("versionName")
                val latestIsPreRelease = json.getBoolean("preRelease")
                val latestBody = json.getString("body")
                val latestHtmlUrl = json.getString("htmlUrl")
                val latestDownload = json.getString("downloadUrl")
                updateAvailable = ignoreVersion || latestVersionCode > applicationVersionCode
                if (updateAvailable) {
                    latestVersionInfo = ReleaseInfo(
                        versionName = latestVersionName,
                        preRelease = latestIsPreRelease,
                        body = latestBody,
                        htmlUrl = latestHtmlUrl,
                        downloadLink = latestDownload
                    )
                    if (manuallyTriggered) coroutineScope {
                        updateSheetState.show()
                    } else {
                        showUpdateToast()
                        Destination.SETTINGS.hasNotification.value = true
                    }
                } else {
                    if (manuallyTriggered) topToastState.showToast(
                        text = R.string.updates_noUpdates,
                        icon = Icons.Rounded.Info,
                        iconTintColor = TopToastColor.ON_SURFACE
                    )
                }
            } catch (e: CancellationException) {
                e.printStackTrace()
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

    fun showUpdateToast() {
        topToastState.showToast(
            text = R.string.updates_updateAvailable,
            icon = Icons.Rounded.Update,
            stayMs = 20000,
            dismissOnClick = true,
            onToastClick = {
                scope.launch { updateSheetState.show() }
            }
        )
    }
}