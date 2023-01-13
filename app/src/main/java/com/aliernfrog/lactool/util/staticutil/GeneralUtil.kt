package com.aliernfrog.lactool.util.staticutil

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.aliernfrog.lactool.data.XYZ

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

        fun checkStoragePermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager()
            else ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        fun generateWallpaperImportUrl(fileName: String, wallpapersPath: String): String {
            return "file://$wallpapersPath/$fileName"
        }

        /**
         * Parses the [string] as [XYZ]
         * @return [XYZ] if it can be parsed, null otherwise
         */
        fun parseAsXYZ(string: String): XYZ? {
            val split = string
                .split(",")
                .map { it.replace(" ","") }
                .filter {
                    it.toDoubleOrNull() != null
                }
            return if (split.size != 3) null
            else XYZ(
                x = split[0].toDouble(),
                y = split[1].toDouble(),
                z = split[2].toDouble()
            )
        }
    }
}