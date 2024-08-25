package com.aliernfrog.lactool.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import coil.imageLoader
import coil.request.ImageRequest
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.laclib.data.LACMapObjectFilter
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.map.LACMapEditor
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.data.MediaViewData
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.impl.ProgressState
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
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

    val addRoleSheetState = SheetState(skipPartiallyExpanded = true, Density(context))
    var mapTypesExpanded by mutableStateOf(false)
    var objectFilterExpanded by mutableStateOf(false)
    var pendingRoleDelete by mutableStateOf<String?>(null)
    var saveWarningShown by mutableStateOf(false)

    private var mapFile: FileWrapper? = null

    var mapEditor by mutableStateOf<LACMapEditor?>(null, neverEqualPolicy())
    var objectFilter by mutableStateOf(LACMapObjectFilter(), neverEqualPolicy())
    var rolesExpandedRoleIndex by mutableIntStateOf(-1)
    var failedMaterials = mutableStateListOf<LACMapDownloadableMaterial>()
        private set
    var materialsLoadProgress by mutableStateOf(Progress(
        description = "",
        totalProgress = 0,
        passedProgress = 0
    ))
        private set

    private val materialsProgressText = context.getString(R.string.mapsMaterials_loading)
    val materialsLoaded: Boolean
        get() = materialsLoadProgress.float?.let { it >= 1f } ?: false

    @SuppressLint("Recycle")
    suspend fun openMap(map: MapFile, context: Context) {
        mapFile = map.file
        withContext(Dispatchers.IO) {
            val inputStream = mapFile?.inputStream(context)
            val content = inputStream?.bufferedReader()?.readText() ?: return@withContext inputStream?.close()
            mapEditor = LACMapEditor(content)
            val materialsCount = (mapEditor?.downloadableMaterials?.size ?: 0).toLong()
            materialsLoadProgress = Progress(
                description = materialsProgressText
                    .replace("{DONE}", "0")
                    .replace("{TOTAL}", materialsCount.toString()),
                totalProgress = (mapEditor?.downloadableMaterials?.size ?: 0).toLong(),
                passedProgress = 0
            )
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

    suspend fun loadDownloadableMaterials(context: Context) {
        if (materialsLoaded) return
        val materials = mapEditor?.downloadableMaterials ?: return
        val totalCount = materials.size
        var passedCount = 0
        materials.forEach { material ->
            val request = ImageRequest.Builder(context)
                .data(material.url)
                .listener(
                    onError = { _, _ ->
                        failedMaterials.add(material)
                    }
                )
                .build()
            context.imageLoader.execute(request)
            passedCount++
            materialsLoadProgress = Progress(
                description = materialsProgressText
                    .replace("{DONE}", passedCount.toString())
                    .replace("{TOTAL}", totalCount.toString()),
                totalProgress = totalCount.toLong(),
                passedProgress = passedCount.toLong()
            )
        }
    }

    private fun deleteDownloadableMaterial(material: LACMapDownloadableMaterial, context: Context) {
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

    fun openDownloadableMaterialOptions(material: LACMapDownloadableMaterial) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        val failed = failedMaterials.contains(material)
        mainViewModel.showMediaView(MediaViewData(
            model = material.url,
            title = material.name,
            zoomEnabled = !failed,
            errorContent = {
                ErrorWithIcon(
                    error = stringResource(R.string.mapsMaterials_material_failed),
                    painter = rememberVectorPainter(Icons.Rounded.Error)
                )
            },
            options = {
                val context = LocalContext.current
                val clipboardManager = LocalClipboardManager.current
                var showDeleteDialog by remember { mutableStateOf(false) }

                ButtonRow(
                    title = stringResource(R.string.mapsMaterials_material_copyUrl),
                    painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
                ) {
                    clipboardManager.setText(AnnotatedString(material.url))
                    topToastState.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
                }
                ButtonRow(
                    title = stringResource(R.string.mapsMaterials_material_delete),
                    description = stringResource(R.string.mapsMaterials_material_delete_description)
                        .replace("%n", material.usedBy.size.toString()),
                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    showDeleteDialog = true
                }

                if (showDeleteDialog) {
                    DeleteConfirmationDialog(
                        name = material.name,
                        onDismissRequest = { showDeleteDialog = false }
                    ) {
                        deleteDownloadableMaterial(material, context)
                        showDeleteDialog = false
                        mainViewModel.dismissMediaView()
                    }
                }
            }
        ))
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