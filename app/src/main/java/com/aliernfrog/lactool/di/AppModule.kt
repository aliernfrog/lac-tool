package com.aliernfrog.lactool.di

import com.aliernfrog.lactool.BuildConfig
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.domain.AppState
import com.aliernfrog.lactool.domain.MapsState
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.shared.impl.VersionManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::PreferenceManager)
    singleOf(::AppState)
    singleOf(::MapsState)

    single {
        get<PreferenceManager>().let { prefs ->
            @Suppress("KotlinConstantConditions") VersionManager(
                tag = TAG,
                appName = "LAC Tool",
                releasesURLPref = prefs.releasesURL,
                debugInfoPrefs = prefs.debugInfoPrefs,
                defaultInstallURL = "https://github.com/aliernfrog/lac-tool",
                buildCommit = BuildConfig.GIT_COMMIT,
                buildBranch = BuildConfig.GIT_BRANCH,
                buildHasLocalChanges = BuildConfig.GIT_LOCAL_CHANGES,
                context = get()
            )
        }
    }

    single {
        TopToastState(
            composeView = null,
            appTheme = null,
            allowSwipingByDefault = false
        )
    }
}