package com.aliernfrog.lactool.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.laclib.data.LACMapObjectFilter
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.map.LACMapEditor
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class MapsEditViewModel(
    val topToastState: TopToastState,
    private val progressState: ProgressState,
    private val mainViewModel: MainViewModel,
    context: Context
) : ViewModel() {
    private val navController: NavController
        get() = mainViewModel.navController
    
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)
    val rolesTopAppBarState = TopAppBarState(0F, 0F, 0F)
    val rolesLazyListState = LazyListState()
    val materialsTopAppBarState = TopAppBarState(0F, 0F, 0F)
    val materialsLazyListState = LazyListState()

    val addRoleSheetState = SheetState(skipPartiallyExpanded = true, Density(context))
    val materialSheetState = SheetState(skipPartiallyExpanded = false, Density(context))
    var mapTypesExpanded by mutableStateOf(false)
    var objectFilterExpanded by mutableStateOf(false)
    var pendingRoleDelete by mutableStateOf<String?>(null)
    var pendingMaterialDelete by mutableStateOf<LACMapDownloadableMaterial?>(null)
    var saveWarningShown by mutableStateOf(false)

    private var mapFile: FileWrapper? = null

    var mapEditor by mutableStateOf<LACMapEditor?>(null, neverEqualPolicy())
    var objectFilter by mutableStateOf(LACMapObjectFilter(), neverEqualPolicy())
    var rolesExpandedRoleIndex by mutableIntStateOf(-1)
    var failedMaterials = mutableStateListOf<LACMapDownloadableMaterial>()
    var materialSheetChosenMaterial by mutableStateOf<LACMapDownloadableMaterial?>(null)
    var materialSheetMaterialFailed by mutableStateOf(false)

    @SuppressLint("Recycle")
    suspend fun openMap(map: MapFile, context: Context) {
        mapFile = map.file
        withContext(Dispatchers.IO) {
            val inputStream = mapFile?.inputStream(context)
            val content = inputStream?.bufferedReader()?.readText() ?: return@withContext inputStream?.close()
            mapEditor = LACMapEditor(content)
            inputStream.close()
        }
        navController.navigate(Destination.MAPS_EDIT.route)
    }

    fun updateMapEditorState() {
        mapEditor = mapEditor
    }

    fun setServerName(serverName: String) {
        mapEditor?.serverName = serverName
        updateMapEditorState()
    }

    fun setMapType(mapType: LACMapType) {
        mapEditor?.mapType = mapType
        updateMapEditorState()
    }

    fun deleteRole(role: String, context: Context) {
        mapEditor?.deleteRole(role)
        updateMapEditorState()
        topToastState.showToast(context.getString(R.string.mapsRoles_deletedRole).replace("{ROLE}", role.removeHtml()), Icons.Rounded.Delete)
    }

    fun addRole(role: String, context: Context) {
        mapEditor?.addRole(
            role = role,
            onIllegalChar = {
                topToastState.showToast(
                    text = context.getString(R.string.mapsRoles_illegalChars).replace("{CHAR}", it),
                    icon = Icons.Rounded.PriorityHigh,
                    iconTintColor = TopToastColor.ERROR
                )
            }
        ) {
            updateMapEditorState()
            topToastState.showToast(context.getString(R.string.mapsRoles_addedRole).replace("{ROLE}", role.removeHtml()), Icons.Rounded.Check)
        }
    }

    fun deleteDownloadableMaterial(material: LACMapDownloadableMaterial, context: Context) {
        val removedObjects = mapEditor?.removeDownloadableMaterial(material.url) ?: 0
        failedMaterials.remove(material)
        updateMapEditorState()
        topToastState.showToast(
            text = context.getString(R.string.mapsMaterials_deleted)
                .replace("{MATERIAL}", material.name)
                .replace("{REPLACEDCOUNT}", removedObjects.toString()),
            icon = Icons.Rounded.Delete
        )
    }

    suspend fun onDownloadableMaterialError(material: LACMapDownloadableMaterial) {
        if (!failedMaterials.contains(material)) {
            failedMaterials.add(material)
            materialsLazyListState.animateScrollToItem(0)
        }
    }

    suspend fun showMaterialSheet(material: LACMapDownloadableMaterial) {
        if (materialSheetChosenMaterial != material) {
            materialSheetMaterialFailed = false
            materialSheetChosenMaterial = material
        }
        materialSheetState.show()
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

    @SuppressLint("Recycle")
    suspend fun saveAndFinishEditing(onNavigateBackRequest: () -> Unit, context: Context) {
        val mapName = mapFile?.nameWithoutExtension
        progressState.currentProgress = Progress(
            context.getString(R.string.maps_edit_saving).replace("{NAME}", mapName.toString())
        )
        try { withContext(Dispatchers.IO) {
            val newContent = mapEditor?.applyChanges() ?: return@withContext
            mapFile!!.writeFile(newContent, context)
            topToastState.showToast(
                text = context.getString(R.string.maps_edit_saved).replace("{NAME}", mapName.toString()),
                icon = Icons.Rounded.Save
            )
        } } catch (e: Exception) {
            topToastState.showErrorToast()
            Log.e(TAG, "saveAndFinishEditing: ", e)
        }
        finishEditingWithoutSaving(onNavigateBackRequest)
        progressState.currentProgress = null
    }

    suspend fun finishEditingWithoutSaving(onNavigateBackRequest: () -> Unit) {
        onNavigateBackRequest()
        mapEditor = null
        objectFilter = LACMapObjectFilter()
        mapFile = null
        failedMaterials.clear()
        scrollState.scrollTo(0)
    }

    suspend fun onNavigationBack(onNavigateBackRequest: () -> Unit) {
        if (mapEditor == null) finishEditingWithoutSaving(onNavigateBackRequest)
        else saveWarningShown = true
    }
}