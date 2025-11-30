package com.aliernfrog.lactool.di

import com.aliernfrog.lactool.ui.viewmodel.*
import io.github.aliernfrog.pftool_shared.ui.viewmodel.InsetsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule = module {
    singleOf(::MainViewModel)
    singleOf(::InsetsViewModel)
    singleOf(::SettingsViewModel)
    singleOf(::ShizukuViewModel)
    singleOf(::PermissionsViewModel)

    singleOf(::MapsViewModel)
    singleOf(::MapsListViewModel)
    singleOf(::MapsEditViewModel)
    singleOf(::MapsMergeViewModel)

    singleOf(::WallpapersViewModel)
    singleOf(::ScreenshotsViewModel)
}