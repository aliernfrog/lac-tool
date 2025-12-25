package com.aliernfrog.lactool.util.staticutil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.aliernfrog.lactool.BuildConfig
import com.aliernfrog.lactool.di.appModules
import com.aliernfrog.lactool.ui.activity.MainActivity
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import androidx.core.net.toUri
import io.github.aliernfrog.pftool_shared.data.Language
import io.github.aliernfrog.pftool_shared.util.extension.toPath
import io.github.aliernfrog.pftool_shared.util.staticutil.PFToolSharedUtil

class GeneralUtil {
    companion object {
        /**
         * Gets [Language] from given language code.
         * [code] must either be a language code, or language and country code splitted by a "-" (e.g.: en-US, en)
         *
         * @return [Language] if [code] is valid, null if it is invalid
         */
        fun getLanguageFromCode(code: String): Language? =
            PFToolSharedUtil.getLanguageFromCode(
                code = code,
                languages = BuildConfig.LANGUAGES,
                translationProgresses = BuildConfig.TRANSLATION_PROGRESSES
            )

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