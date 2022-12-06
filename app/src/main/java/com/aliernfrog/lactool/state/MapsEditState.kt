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
import com.aliernfrog.lactool.util.staticutil.LACUtil
import com.aliernfrog.lactool.util.extension.removeHtml
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
    private val roleNameIllegalChars = listOf(",",":")

    @OptIn(ExperimentalMaterialApi::class)
    val roleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
    @OptIn(ExperimentalMaterialApi::class)
    val addRoleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
    var saveWarningShown = mutableStateOf(false)

    private var mapFile: File? = null
    private var mapDocumentFile: DocumentFileCompat? = null
    val mapData: MutableState<LACMapData?> = mutableStateOf(null)
    val roleSheetChosenRole = mutableStateOf("")

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) return
        mapFile = file
        mapDocumentFile = documentFile
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
                        mapData.value!!.serverName.value = type.getValue(line)
                        mapData.value!!.serverNameLine = index
                    }
                    LACLineType.MAP_TYPE -> {
                        mapData.value!!.mapType.value = type.getValue(line).toInt()
                        mapData.value!!.mapTypeLine = index
                    }
                    LACLineType.ROLES_LIST -> {
                        mapData.value!!.mapRoles = type.getValue(line).removeSuffix(",").split(",").toMutableStateList()
                        mapData.value!!.mapRolesLine = index
                    }
                    LACLineType.OPTION_NUMBER -> mapData.value!!.mapOptions?.add(LacMapOption(LACMapOptionType.NUMBER, type.getLabel(line)!!, mutableStateOf(type.getValue(line)), index))
                    LACLineType.OPTION_BOOLEAN -> mapData.value!!.mapOptions?.add(LacMapOption(LACMapOptionType.BOOLEAN, type.getLabel(line)!!, mutableStateOf(type.getValue(line)), index))
                    LACLineType.OPTION_SWITCH -> mapData.value!!.mapOptions?.add(LacMapOption(LACMapOptionType.SWITCH, type.getLabel(line)!!, mutableStateOf(type.getValue(line)), index))
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("Recycle")
    suspend fun saveAndFinishEditing(navController: NavController, context: Context) {
        withContext(Dispatchers.IO) {
            if (mapData.value?.serverName != null)
                mapData.value!!.mapLines!![mapData.value!!.serverNameLine!!] = LACLineType.SERVER_NAME.setValue(mapData.value!!.serverName.value)
            if (mapData.value?.mapType != null)
                mapData.value!!.mapLines!![mapData.value!!.mapTypeLine!!] = LACLineType.MAP_TYPE.setValue(mapData.value!!.mapType.value.toString())
            if (mapData.value?.mapRoles != null)
                mapData.value!!.mapLines!![mapData.value!!.mapRolesLine!!] = LACLineType.ROLES_LIST.setValue(mapData.value!!.mapRoles!!.joinToString(",").plus(","))
            mapData.value?.mapOptions?.forEach { option ->
                mapData.value!!.mapLines!![option.line] = LACLineType.OPTION_GENERAL.setValue(option.value.value, option.label)
            }
            val outputStreamWriter = (if (mapFile != null) mapFile!!.outputStream() else context.contentResolver.openOutputStream(mapDocumentFile!!.uri)!!).writer(Charsets.UTF_8)
            outputStreamWriter.write(mapData.value!!.mapLines!!.joinToString("\n"))
            outputStreamWriter.flush()
            outputStreamWriter.close()
            topToastManager.showToast(context.getString(R.string.info_mapEditsSaved), iconDrawableId = R.drawable.check, iconTintColorType = TopToastColorType.PRIMARY)
        }
        finishEditingWithoutSaving(navController)
    }

    suspend fun finishEditingWithoutSaving(navController: NavController) {
        navController.popBackStack()
        mapData.value = null
        mapFile = null
        mapDocumentFile = null
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
        if (roleNameIllegalChars.find { role.contains(it) } != null)
            return topToastManager.showToast(context.getString(R.string.mapsRoles_illegalChars).replace("%CHARS%", roleNameIllegalChars.joinToString(", ") { "\"$it\"" }), iconDrawableId = R.drawable.exclamation, iconTintColorType = TopToastColorType.ERROR)
        mapData.value?.mapRoles?.add(role)
        topToastManager.showToast(context.getString(R.string.mapsRoles_addedRole).replace("%ROLE%", role.removeHtml()), iconDrawableId = R.drawable.check, iconTintColorType = TopToastColorType.PRIMARY)
    }

    suspend fun onNavigationBack(navController: NavController) {
        if (mapData.value == null) finishEditingWithoutSaving(navController)
        else saveWarningShown.value = true
    }
}