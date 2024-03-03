package com.aliernfrog.lactool.util.staticutil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import com.aliernfrog.lactool.data.Language
import com.aliernfrog.lactool.di.appModules
import com.aliernfrog.lactool.ui.activity.MainActivity
import com.aliernfrog.lactool.util.extension.toPath
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import java.util.Locale

@Suppress("DEPRECATION")
class GeneralUtil {
    companion object {
        fun getAppVersionName(context: Context): String {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        }

        fun getAppVersionCode(context: Context): Int {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
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
            return Language(
                languageCode = languageCode,
                countryCode = countryCode,
                fullCode = code,
                localizedName = locale.getDisplayName(locale)
            )
        }

        fun getLocale(language: String, country: String? = null): Locale {
            return if (country != null) Locale(language, country)
            else Locale(language)
        }

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
                internalPath = Uri.parse(wallpapersPath).toPath() ?: wallpapersPath
            } catch (_: Exception) {}
            return "file://$internalPath/$fileName"
        }
    }
}