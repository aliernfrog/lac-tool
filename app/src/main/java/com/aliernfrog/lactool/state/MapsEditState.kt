package com.aliernfrog.lactool.state

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.aliernfrog.lactool.data.LacMapOption
import com.aliernfrog.lactool.enum.LACLineType
import com.aliernfrog.lactool.enum.LACMapOptionType
import com.aliernfrog.lactool.util.LACUtil
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MapsEditState {
    val scrollState = ScrollState(0)
    val rolesLazyListState = LazyListState()

    @OptIn(ExperimentalMaterialApi::class)
    val roleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)

    var mapLines: MutableList<String>? = null
    val serverName: MutableState<String?> = mutableStateOf(null)
    val mapType: MutableState<Int?> = mutableStateOf(null)
    var mapOptions: MutableList<LacMapOption>? = null
    var mapRoles: MutableList<String>? = null
    val roleSheetChosenRole = mutableStateOf("")

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) return
        withContext(Dispatchers.IO) {
            val inputStream = file?.inputStream() ?: context.contentResolver.openInputStream(documentFile!!.uri)
            mapLines = inputStream?.bufferedReader()?.readText()?.split("\n")?.toMutableList()
            mapOptions = mutableListOf()
            mapRoles = mutableListOf()
            inputStream?.close()
            readMapLines()
        }
    }

    private fun readMapLines() {
        mapLines?.forEach { line ->
            try {
                when (val type = LACUtil.getEditorLineType(line)) {
                    LACLineType.SERVER_NAME -> serverName.value = type.getValue(line)
                    LACLineType.MAP_TYPE -> mapType.value = type.getValue(line).toInt()
                    LACLineType.ROLES_LIST -> mapRoles = type.getValue(line).removeSuffix(",").split(",").toMutableList()
                    LACLineType.OPTION_NUMBER -> mapOptions?.add(LacMapOption(LACMapOptionType.NUMBER, type.getLabel(line)!!, type.getValue(line)))
                    LACLineType.OPTION_BOOLEAN -> mapOptions?.add(LacMapOption(LACMapOptionType.BOOLEAN, type.getLabel(line)!!, type.getValue(line)))
                    LACLineType.OPTION_SWITCH -> mapOptions?.add(LacMapOption(LACMapOptionType.SWITCH, type.getLabel(line)!!, type.getValue(line)))
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
        mapOptions = null
        mapRoles = null
        scrollState.scrollTo(0)
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun showRoleSheet(role: String) {
        roleSheetChosenRole.value = role
        roleSheetState.show()
    }
}