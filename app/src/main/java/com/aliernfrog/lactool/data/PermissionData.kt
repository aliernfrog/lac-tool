package com.aliernfrog.lactool.data

import android.os.Environment
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager

data class PermissionData(
    @StringRes val title: Int,
    val pref: BasePreferenceManager.Preference<String>,
    val recommendedPath: String? = pref.defaultValue,
    @StringRes val recommendedPathDescription: Int?,
    @StringRes val recommendedPathWarning: Int? = null,
    @StringRes val useUnrecommendedAnywayDescription: Int? = null,
    val forceRecommendedPath: Boolean = true,
    val content: @Composable () -> Unit
)

val PermissionData.requiresAndroidData: Boolean
    get() = forceRecommendedPath && recommendedPath?.startsWith(
        "${Environment.getExternalStorageDirectory()}/Android/data"
    ) == true