package com.aliernfrog.lactool.state

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.aliernfrog.lactool.enum.LACLineType
import com.aliernfrog.lactool.util.LACUtil
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MapsEditState {
    val scrollState = ScrollState(0)

    var mapLines: MutableList<String>? = null
    val serverName: MutableState<String?> = mutableStateOf(null)
    val mapType: MutableState<Int?> = mutableStateOf(null)

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) return
        withContext(Dispatchers.IO) {
            val inputStream = file?.inputStream() ?: context.contentResolver.openInputStream(documentFile!!.uri)
            mapLines = inputStream?.bufferedReader()?.readText()?.split("\n")?.toMutableList()
            inputStream?.close()
            readMapLines()
        }
    }

    private fun readMapLines() {
        mapLines?.forEach {
            try {
                when (LACUtil.getEditorLineType(it)) {
                    LACLineType.SERVER_NAME -> serverName.value = LACLineType.SERVER_NAME.getValue(it)
                    LACLineType.MAP_TYPE -> mapType.value = LACLineType.MAP_TYPE.getValue(it).toInt()
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun finishEditing(navController: NavController) {
        navController.popBackStack()
        mapLines = null
        serverName.value = null
        mapType.value = null
        scrollState.scrollTo(0)
    }
}