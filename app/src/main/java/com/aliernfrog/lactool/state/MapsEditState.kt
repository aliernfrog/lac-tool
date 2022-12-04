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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
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

    var mapLines: MutableList<String>? = null
    val serverName: MutableState<String?> = mutableStateOf(null)
    val mapType: MutableState<Int?> = mutableStateOf(null)
    var mapOptions: MutableList<LacMapOption>? = null
    var mapRoles: SnapshotStateList<String>? = null
    val roleSheetChosenRole = mutableStateOf("")

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) return
        withContext(Dispatchers.IO) {
            val inputStream = file?.inputStream() ?: context.contentResolver.openInputStream(documentFile!!.uri)
            mapLines = inputStream?.bufferedReader()?.readText()?.split("\n")?.toMutableList()
            mapOptions = mutableListOf()
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
                    LACLineType.ROLES_LIST -> mapRoles = type.getValue(line).removeSuffix(",").split(",").toMutableStateList()
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

    fun deleteRole(role: String, context: Context) {
        mapRoles?.remove(role)
        topToastManager.showToast(context.getString(R.string.mapsRoles_deletedRole).replace("%ROLE%", role.removeHtml()), iconDrawableId = R.drawable.trash, iconTintColorType = TopToastColorType.PRIMARY)
    }

    fun addRole(role: String, context: Context) {
        mapRoles?.add(role)
        topToastManager.showToast(context.getString(R.string.mapsRoles_addedRole).replace("%ROLE%", role.removeHtml()), iconDrawableId = R.drawable.check, iconTintColorType = TopToastColorType.PRIMARY)
    }
}