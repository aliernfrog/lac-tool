package com.aliernfrog.lactool.state

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMapData
import com.aliernfrog.lactool.data.LacMapOption
import com.aliernfrog.lactool.enum.LACLineType
import com.aliernfrog.lactool.enum.LACMapOptionType
import com.aliernfrog.lactool.util.LACUtil
import com.aliernfrog.lactool.util.extensions.removeHtml
import com.aliernfrog.toptoast.TopToastColorType
import com.aliernfrog.toptoast.TopToastManager
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MapsEditState(_topToastManager: TopToastManager) {
    private val topToastManager = _topToastManager
    val scrollState = ScrollState(0)
    val rolesLazyListState = LazyListState()

    @OptIn(ExperimentalMaterialApi::class)
    val roleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
    @OptIn(ExperimentalMaterialApi::class)
    val addRoleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)

    val mapData: MutableState<LACMapData?> = mutableStateOf(null)
    val roleSheetChosenRole = mutableStateOf("")

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) return
        withContext(Dispatchers.IO) {
            val inputStream = file?.inputStream() ?: context.contentResolver.openInputStream(documentFile!!.uri)
            mapData.value = LACMapData()
            mapData.value?.mapLines = inputStream?.bufferedReader()?.readText()?.split("\n")?.toMutableList()
            mapData.value?.mapOptions = mutableStateListOf()
            inputStream?.close()
            readMapLines()
        }
    }

    private fun readMapLines() {
        mapData.value!!.mapLines?.forEachIndexed { index, line ->
            try {
                when (val type = LACUtil.getEditorLineType(line)) {
                    LACLineType.SERVER_NAME -> {
                        mapData.value!!.serverName = type.getValue(line)
                        mapData.value!!.serverNameLine = index
                    }
                    LACLineType.MAP_TYPE -> {
                        mapData.value!!.mapType = type.getValue(line).toInt()
                        mapData.value!!.mapTypeLine = index
                    }
                    LACLineType.ROLES_LIST -> {
                        mapData.value!!.mapRoles = type.getValue(line).removeSuffix(",").split(",").toMutableStateList()
                        mapData.value!!.mapRolesLine = index
                    }
                    LACLineType.OPTION_NUMBER -> mapData.value!!.mapOptions?.add(LacMapOption(LACMapOptionType.NUMBER, type.getLabel(line)!!, type.getValue(line)))
                    LACLineType.OPTION_BOOLEAN -> mapData.value!!.mapOptions?.add(LacMapOption(LACMapOptionType.BOOLEAN, type.getLabel(line)!!, type.getValue(line)))
                    LACLineType.OPTION_SWITCH -> mapData.value!!.mapOptions?.add(LacMapOption(LACMapOptionType.SWITCH, type.getLabel(line)!!, type.getValue(line)))
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun finishEditing(navController: NavController) {
        navController.popBackStack()
        mapData.value = null
        scrollState.scrollTo(0)
    }

    @OptIn(ExperimentalMaterialApi::class)
    suspend fun showRoleSheet(role: String) {
        roleSheetChosenRole.value = role
        roleSheetState.show()
    }

    fun deleteRole(role: String, context: Context) {
        mapData.value?.mapRoles?.remove(role)
        topToastManager.showToast(context.getString(R.string.mapsRoles_deletedRole).replace("%ROLE%", role.removeHtml()), iconDrawableId = R.drawable.trash, iconTintColorType = TopToastColorType.PRIMARY)
    }

    fun addRole(role: String, context: Context) {
        mapData.value?.mapRoles?.add(role)
        topToastManager.showToast(context.getString(R.string.mapsRoles_addedRole).replace("%ROLE%", role.removeHtml()), iconDrawableId = R.drawable.check, iconTintColorType = TopToastColorType.PRIMARY)
    }
}