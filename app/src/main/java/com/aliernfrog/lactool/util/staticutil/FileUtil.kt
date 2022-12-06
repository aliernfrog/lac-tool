package com.aliernfrog.lactool.util.staticutil

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.DocumentsContract
import android.text.format.DateUtils
import androidx.core.content.FileProvider
import com.aliernfrog.lactool.R
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import java.io.File

class FileUtil {
    companion object {
        fun removeExtension(path: String): String {
            val extensionIndex = path.lastIndexOf(".")
            if (extensionIndex == -1) return path
            return path.substring(0, extensionIndex)
        }

        fun lastModifiedFromLong(lastModified: Long, context: Context): String {
            return DateUtils.getRelativeDateTimeString(context, lastModified, DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0).toString()
        }

        fun copyFile(source: String, destination: DocumentFileCompat, context: Context) {
            val input = File(source).inputStream()
            val output = context.contentResolver.openOutputStream(destination.uri)
            if (output != null) input.copyTo(output)
            input.close()
            output?.close()
        }

        fun copyFile(source: DocumentFileCompat, destination: String, context: Context) {
            val input = context.contentResolver.openInputStream(source.uri)
            val output = File(destination).outputStream()
            input?.copyTo(output)
            input?.close()
            output.close()
        }

        fun shareFile(filePath: String, type: String, context: Context, title: String = context.getString(R.string.action_share)) {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).setType(type).putExtra(Intent.EXTRA_STREAM, uri)
            context.startActivity(Intent.createChooser(intent, title))
        }

        fun checkUriPermission(path: String, context: Context): Boolean {
            val treeId = path.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
            val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId)
            return context.checkUriPermission(treeUri, android.os.Process.myPid(), android.os.Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
    }
}