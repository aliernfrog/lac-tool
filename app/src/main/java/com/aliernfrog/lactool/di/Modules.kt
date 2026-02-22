package com.aliernfrog.lactool.di

import com.aliernfrog.lactool.BuildConfig
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.extension.enable
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.pfToolSharedString
import com.aliernfrog.lactool.util.sharedString
import io.github.aliernfrog.pftool_shared.di.getPFToolSharedModule
import io.github.aliernfrog.pftool_shared.repository.MapFileFinder
import io.github.aliernfrog.shared.di.getKoinInstance
import io.github.aliernfrog.shared.di.getSharedModule

val appModules = listOf(
    appModule,
    viewModelModule,

    getSharedModule(
        sharedString = sharedString
    ),

    getPFToolSharedModule(
        applicationId = BuildConfig.APPLICATION_ID,
        isDebugBuild = BuildConfig.DEBUG,
        languageCodes = BuildConfig.LANGUAGES,
        translationProgresses = BuildConfig.TRANSLATION_PROGRESSES,
        baseLanguageCode = "en-US",
        sharedString = pfToolSharedString,
        importedMapsFinder = MapFileFinder(
            pathPref = { getKoinInstance<PreferenceManager>().lacMapsDir },
            isMapFile = { it.isFile && it.name.endsWith(".txt", ignoreCase = true) }
        ),
        exportedMapsFinder = MapFileFinder(
            pathPref = { getKoinInstance<PreferenceManager>().exportedMapsDir },
            isMapFile = { it.isFile && it.name.endsWith(".txt", ignoreCase = true) }
        ),
        getFileAsMapFile = { MapFile(it) },
        languagePref = {
            getKoinInstance<PreferenceManager>().language
        },
        shizukuNeverLoadPref = {
            getKoinInstance<PreferenceManager>().shizukuNeverLoad
        },
        storageAccessTypePref = {
            getKoinInstance<PreferenceManager>().storageAccessType
        },
        ignoreDocumentsUIRestrictionsPref = {
            getKoinInstance<PreferenceManager>().ignoreDocumentsUIRestrictions
        },
        onSetStorageAccessType = {
            it.enable()
        }
    )
)