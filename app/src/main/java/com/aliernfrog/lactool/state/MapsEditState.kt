package com.aliernfrog.lactool.state

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.navigation.NavController
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MapsEditState() {
    val scrollState = ScrollState(0)
    var mapLines: MutableList<String>? = null

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) return
        withContext(Dispatchers.IO) {
            val inputStream = file?.inputStream() ?: context.contentResolver.openInputStream(documentFile!!.uri)
            mapLines = inputStream?.bufferedReader()?.readText()?.split("\n")?.toMutableList()
            inputStream?.close()
        }
    }

    suspend fun finishEditing(navController: NavController) {
        navController.popBackStack()
        mapLines = null
        scrollState.scrollTo(0)
    }
}