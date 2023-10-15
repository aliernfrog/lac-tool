package com.aliernfrog.lactool.util.staticutil

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import com.aliernfrog.lactool.di.appModules
import com.aliernfrog.lactool.ui.activity.MainActivity
import com.aliernfrog.lactool.util.extension.resolvePath
import com.lazygeniouz.dfc.file.DocumentFileCompat
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

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
                internalPath = Uri.parse(wallpapersPath).resolvePath() ?: wallpapersPath
            } catch (_: Exception) {}
            return "file://$internalPath/$fileName"
        }

        fun getDocumentFileFromPath(path: String, context: Context): DocumentFileCompat {
            val treeId = path.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
            val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId)
            return DocumentFileCompat.fromTreeUri(context, treeUri)!!
        }
    }
}