package com.aliernfrog.lactool.util.staticutil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import com.aliernfrog.lactool.BuildConfig
import com.aliernfrog.lactool.di.appModules
import com.aliernfrog.lactool.hasAndroidDataRestrictions
import com.aliernfrog.lactool.ui.activity.MainActivity
import com.aliernfrog.lactool.util.extension.toPath
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import java.util.Locale
import androidx.core.net.toUri
import io.github.aliernfrog.pftool_shared.data.Language

class GeneralUtil {
    companion object {
        private const val DOCUMENTS_UI_PACKAGE = "com.android.documentsui"
        private const val GOOGLE_DOCUMENTS_UI_PACKAGE = "com.google.android.documentsui"

        fun documentsUIRestrictsAndroidData(context: Context): Boolean {
            if (!hasAndroidDataRestrictions) return false
            val documentsUIPackage = getDocumentsUIPackage(context)
            return documentsUIPackage?.let {
                it.longVersionCode >= when (it.packageName) {
                    DOCUMENTS_UI_PACKAGE -> 14
                    GOOGLE_DOCUMENTS_UI_PACKAGE -> 340916000
                    else -> Long.MAX_VALUE
                }
            } ?: false
        }

        fun getDocumentsUIPackage(context: Context) = try {
            context.packageManager.getPackageInfo(GOOGLE_DOCUMENTS_UI_PACKAGE, 0)
        } catch (_: Exception) {
            try {
                context.packageManager.getPackageInfo(DOCUMENTS_UI_PACKAGE, 0)
            } catch (_: Exception) {
                null
            }
        }

        fun getAppVersionName(context: Context): String {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName.toString()
        }

        fun getAppVersionCode(context: Context): Long {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        }

        /**
         * Gets [Language] from given language code.
         * [code] must either be a language code, or language and country code splitted by a "-" (e.g.: en-US, en)
         *
         * @return [Language] if [code] is valid, null if it is invalid
         */
        fun getLanguageFromCode(code: String): Language? {
            val split = code.split("-")
            val languageCode = split.getOrNull(0) ?: return null
            val countryCode = split.getOrNull(1)
            val locale = getLocale(languageCode, countryCode)
            var translationProgress = 0f
            try {
                val index = BuildConfig.LANGUAGES.indexOf(code)
                if (index != -1) {
                    translationProgress = BuildConfig.TRANSLATION_PROGRESSES[index]
                }
            } catch (_: Exception) {}
            return Language(
                languageCode = languageCode,
                countryCode = countryCode,
                fullCode = code,
                localizedName = locale.getDisplayName(locale),
                translationProgress = translationProgress
            )
        }

        fun getLocale(language: String, country: String? = null): Locale {
            val builder = Locale.Builder().setLanguage(language)
            if (country != null) builder.setRegion(country)
            return builder.build()
        }

        @Suppress("DEPRECATION")
        fun isConnectedToInternet(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }

        fun restartApp(context: Context, withModules: Boolean = true) {
            val intent = Intent(context, MainActivity::class.java)
            (context as Activity).finish()
            if (withModules) {
                unloadKoinModules(appModules)
                loadKoinModules(appModules)
            }
            context.startActivity(intent)
        }

        fun generateWallpaperImportUrl(fileName: String, wallpapersPath: String): String {
            var internalPath = wallpapersPath
            try {
                internalPath = wallpapersPath.toUri().toPath()
            } catch (_: Exception) {}
            return "file://$internalPath/$fileName"
        }
    }
}