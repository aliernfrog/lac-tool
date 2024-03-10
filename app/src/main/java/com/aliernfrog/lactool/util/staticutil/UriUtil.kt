package com.aliernfrog.lactool.util.staticutil

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.URLUtil
import java.io.File
import java.io.InputStream
import java.net.URL

class UriUtil {
    companion object {
        /**
         * Caches file from [uri] and returns cached [File].
         * @param uri [Uri] to cache.
         * @param parentName Name of the parent folder in cache dir.
         */
        fun cacheFile(
            uri: Uri,
            parentName: String?,
            context: Context
        ): File? {
            return try {
                val isHTTP = uri.scheme == "http" || uri.scheme == "https"
                val inputStream = (
                        if (isHTTP) URL(uri.toString()).openStream()
                        else context.contentResolver.openInputStream(uri)
                        ) ?: return null
                val fileName = (
                        if (isHTTP) URLUtil.guessFileName(uri.toString(), null, null)
                        else getFileName(uri, context)
                        ) ?: "unknown"
                val file = writeToCache(
                    fileName = fileName,
                    inputStream = inputStream,
                    parentName = parentName,
                    context = context
                )
                inputStream.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun writeToCache(
            fileName: String,
            inputStream: InputStream,
            parentName: String?,
            context: Context
        ): File {
            val outputFile = File("${context.externalCacheDir}${
                if (parentName != null) "/$parentName" else ""
            }/$fileName")
            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) outputFile.delete()
            val output = outputFile.outputStream()
            inputStream.copyTo(output)
            output.close()
            return outputFile
        }

        @SuppressLint("Range")
        private fun getFileName(uri: Uri, context: Context): String {
            var fileName: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor?.moveToFirst() == true) fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                cursor?.close()
            }
            if (fileName == null) {
                fileName = uri.path
                val cut = fileName!!.lastIndexOf("/")
                if (cut != -1) fileName = fileName.substring(cut + 1)
            }
            return fileName
        }
    }
}