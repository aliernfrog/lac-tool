package com.aliernfrog.lactool.di

import com.aliernfrog.lactool.ui.viewmodel.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val viewModelModule = module {
    singleOf(::MainViewModel)
    singleOf(::InsetsViewModel)
    singleOf(::SettingsViewModel)

    singleOf(::MapsViewModel)
    singleOf(::MapsEditViewModel)
    singleOf(::MapsMergeViewModel)

    singleOf(::WallpapersViewModel)
    singleOf(::ScreenshotsViewModel)
}