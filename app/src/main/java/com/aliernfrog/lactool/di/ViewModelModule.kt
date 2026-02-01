package com.aliernfrog.lactool.di

import com.aliernfrog.lactool.ui.viewmodel.*
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::SettingsViewModel)

    viewModelOf(::MapsViewModel)
    viewModelOf(::MapsListViewModel)
    viewModelOf(::MapsEditViewModel)
    viewModelOf(::MapsMergeViewModel)

    viewModelOf(::WallpapersViewModel)
    viewModelOf(::ScreenshotsViewModel)
}