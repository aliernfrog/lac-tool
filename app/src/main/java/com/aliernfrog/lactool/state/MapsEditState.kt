package com.aliernfrog.lactool.state

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.laclib.data.LACMapObjectFilter
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.map.LACMapEditor
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class MapsEditState(_topToastState: TopToastState) {
    private val topToastState = _topToastState
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)
    val rolesTopAppBarState = TopAppBarState(0F, 0F, 0F)
    val rolesLazyListState = LazyListState()
    val materialsTopAppBarState = TopAppBarState(0F, 0F, 0F)
    val materialsLazyListState = LazyListState()

    val roleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
    val addRoleSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
    val materialSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden, isSkipHalfExpanded = true)
    var saveWarningShown = mutableStateOf(false)

    private var mapFile: File? = null
    private var mapDocumentFile: DocumentFileCompat? = null
    var mapEditor by mutableStateOf<LACMapEditor?>(null, neverEqualPolicy())
    var objectFilter by mutableStateOf(LACMapObjectFilter(), neverEqualPolicy())
    val failedMaterials = mutableStateListOf<LACMapDownloadableMaterial>()
    val roleSheetChosenRole = mutableStateOf("")
    val materialSheetChosenMaterial = mutableStateOf<LACMapDownloadableMaterial?>(null)
    val materialSheetMaterialFailed = mutableStateOf(false)

    @SuppressLint("Recycle")
    suspend fun loadMap(file: File?, documentFile: DocumentFileCompat?, context: Context) {
        if (file == null && documentFile == null) throw IllegalArgumentException()
        mapFile = file
        mapDocumentFile = documentFile
        withContext(Dispatchers.IO) {
            val inputStream = file?.inputStream() ?: context.contentResolver.openInputStream(documentFile!!.uri)
            val content = inputStream?.bufferedReader()?.readText() ?: return@withContext
            mapEditor = LACMapEditor(content)
            inputStream.close()
        }
    }

    fun setServerName(serverName: String) {
        mapEditor?.serverName = serverName
        updateMapEditorState()
    }

    fun setMapType(mapType: LACMapType) {
        mapEditor?.mapType = mapType
        updateMapEditorState()
    }

    fun replaceOldObjects(context: Context) {
        val replacedObjects = mapEditor?.replaceOldObjects() ?: 0
        updateMapEditorState()
        topToastState.showToast(
            text = context.getString(R.string.mapsEdit_replacedOldObjects).replace("%n", replacedObjects.toString()),
            icon = Icons.Rounded.Done
        )
    }

    fun getObjectFilterMatches(): List<String> {
        return mapEditor?.getObjectsMatchingFilter(objectFilter) ?: listOf()
    }

    fun removeObjectFilterMatches(context: Context) {
        val removedObjects = mapEditor?.removeObjectsMatchingFilter(objectFilter)
        objectFilter = LACMapObjectFilter()
        topToastState.showToast(
            text = context.getString(R.string.mapsEdit_filterObjects_removedMatches).replace("%n", removedObjects.toString()),
            icon = Icons.Rounded.Delete
        )
    }

    fun deleteRole(role: String, context: Context) {
        mapEditor?.deleteRole(role)
        updateMapEditorState()
        topToastState.showToast(context.getString(R.string.mapsRoles_deletedRole).replace("%ROLE%", role.removeHtml()), Icons.Rounded.Delete)
    }

    fun addRole(role: String, context: Context) {
        mapEditor?.addRole(
            role = role,
            onIllegalChar = {
                topToastState.showToast(
                    text = context.getString(R.string.mapsRoles_illegalChars).replace("%CHAR%", it),
                    icon = Icons.Rounded.PriorityHigh,
                    iconTintColor = TopToastColor.ERROR
                )
            }
        ) {
            updateMapEditorState()
            topToastState.showToast(context.getString(R.string.mapsRoles_addedRole).replace("%ROLE%", role.removeHtml()), Icons.Rounded.Check)
        }
    }

    fun deleteDownloadableMaterial(material: LACMapDownloadableMaterial, context: Context) {
        val removedObjects = mapEditor?.removeDownloadableMaterial(material.url) ?: 0
        failedMaterials.remove(material)
        updateMapEditorState()
        topToastState.showToast(
            text = context.getString(R.string.mapsMaterials_deleted)
                .replace("%MATERIAL%", material.name)
                .replace("%OBJECTS%", removedObjects.toString()),
            icon = Icons.Rounded.Delete
        )
    }

    suspend fun onDownloadableMaterialError(material: LACMapDownloadableMaterial) {
        if (!failedMaterials.contains(material)) {
            failedMaterials.add(material)
            materialsLazyListState.scrollToItem(0)
        }
    }

    fun updateMapEditorState() {
        mapEditor = mapEditor
    }

    @SuppressLint("Recycle")
    suspend fun saveAndFinishEditing(navController: NavController, context: Context) {
        withContext(Dispatchers.IO) {
            val newContent = mapEditor?.applyChanges() ?: return@withContext
            val outputStreamWriter = (if (mapFile != null) mapFile!!.outputStream() else context.contentResolver.openOutputStream(mapDocumentFile!!.uri)!!).writer(Charsets.UTF_8)
            outputStreamWriter.write(newContent)
            outputStreamWriter.flush()
            outputStreamWriter.close()
            topToastState.showToast(R.string.maps_edit_saved, Icons.Rounded.Save)
        }
        finishEditingWithoutSaving(navController)
    }

    suspend fun finishEditingWithoutSaving(navController: NavController) {
        navController.popBackStack()
        mapEditor = null
        objectFilter = LACMapObjectFilter()
        mapFile = null
        mapDocumentFile = null
        failedMaterials.clear()
        scrollState.scrollTo(0)
    }

    suspend fun showRoleSheet(role: String) {
        roleSheetChosenRole.value = role
        roleSheetState.show()
    }

    suspend fun showMaterialSheet(material: LACMapDownloadableMaterial) {
        if (materialSheetChosenMaterial.value != material) {
            materialSheetMaterialFailed.value = false
            materialSheetChosenMaterial.value = material
        }
        materialSheetState.show()
    }

    suspend fun onNavigationBack(navController: NavController) {
        if (mapEditor == null) finishEditingWithoutSaving(navController)
        else saveWarningShown.value = true
    }
}