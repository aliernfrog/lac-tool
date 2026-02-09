package com.aliernfrog.lactool.ui.viewmodel

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.laclib.data.LACMapObjectFilter
import com.aliernfrog.laclib.enum.LACMapType
import com.aliernfrog.laclib.map.LACMapEditor
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.domain.AppState
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.impl.laclib.MapEditorState
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.lactool.util.extension.showReportableErrorToast
import com.aliernfrog.lactool.util.extension.writeFile
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.impl.ProgressState
import io.github.aliernfrog.shared.data.MediaOverlayData
import io.github.aliernfrog.shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.shared.ui.dialog.DeleteConfirmationDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
class MapsEditViewModel(
    val map: MapFile,
    val onNavigateBackRequest: () -> Unit,
    val prefs: PreferenceManager,
    private val appState: AppState,
    private val progressState: ProgressState,
    val topToastState: TopToastState,
    context: Context
) : ViewModel() {
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)
    var mapTypesExpanded by mutableStateOf(false)
    var objectFilterExpanded by mutableStateOf(false)
    var saveWarningShown by mutableStateOf(false)

    var mapEditor by mutableStateOf<MapEditorState?>(null)

    var objectFilter by mutableStateOf(LACMapObjectFilter(), neverEqualPolicy())
    var failedMaterials = mutableStateListOf<LACMapDownloadableMaterial>()
        private set

    private val materialsProgressText = context.getString(R.string.mapsMaterials_loading)
    var materialsLoadProgress by mutableStateOf(Progress(
        description = materialsProgressText,
        totalProgress = 0,
        passedProgress = 0
    ))
        private set

    val materialsLoaded: Boolean
        get() = materialsLoadProgress.float?.let { it >= 1f } ?: false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = map.file.inputStream(context)!!
                val content = inputStream.bufferedReader().readText()
                mapEditor = MapEditorState(LACMapEditor(
                    content = content,
                    onDebugLog = { msg ->
                        if (prefs.debug.value) Log.d(TAG, "[laclib] $msg")
                    }
                ))
                inputStream.close()
                val materialsCount = mapEditor!!.downloadableMaterials.size
                materialsLoadProgress = Progress(
                    description = materialsProgressText
                        .replace("{DONE}", "0")
                        .replace("{TOTAL}", materialsCount.toString()),
                    totalProgress = materialsCount.toLong(),
                    passedProgress = 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "MapsEditViewModel/init: Failed to open map" , e)
                topToastState.showReportableErrorToast(e)
            }
        }
    }

    fun setServerName(serverName: String) {
        mapEditor!!.serverName = serverName
    }

    fun setMapType(mapType: LACMapType) {
        mapEditor!!.mapType = mapType
    }

    fun deleteRole(role: String, context: Context) {
        mapEditor!!.deleteRole(role)
        topToastState.showToast(context.getString(R.string.mapsRoles_deletedRole).replace("{ROLE}", role.removeHtml()), Icons.Rounded.Delete)
    }

    fun addRole(role: String, context: Context) {
        mapEditor!!.addRole(
            role = role,
            onIllegalChar = {
                topToastState.showToast(
                    text = context.getString(R.string.mapsRoles_illegalChars).replace("{CHAR}", it),
                    icon = Icons.Rounded.PriorityHigh,
                    iconTintColor = TopToastColor.ERROR
                )
            }
        ) {
            topToastState.showToast(context.getString(R.string.mapsRoles_addedRole).replace("{ROLE}", role.removeHtml()), Icons.Rounded.Check)
        }
    }

    fun loadDownloadableMaterials(context: Context) = viewModelScope.launch {
        if (materialsLoaded) return@launch
        val materials = mapEditor!!.downloadableMaterials
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
        val removedObjects = mapEditor!!.removeDownloadableMaterial(material.url) ?: 0
        failedMaterials.remove(material)
        topToastState.showToast(
            text = context.getString(R.string.mapsMaterials_deleted)
                .replace("{MATERIAL}", material.name)
                .replace("{REPLACEDCOUNT}", removedObjects.toString()),
            icon = Icons.Rounded.Delete
        )
    }

    fun openDownloadableMaterialOptions(material: LACMapDownloadableMaterial) {
        val failed = failedMaterials.contains(material)
        appState.mediaOverlayData = MediaOverlayData(
            model = material.url,
            title = material.name,
            zoomEnabled = !failed,
            errorContent = {
                ErrorWithIcon(
                    description = stringResource(R.string.mapsMaterials_failed),
                    icon = rememberVectorPainter(Icons.Rounded.Error),
                    contentColor = Color.Red
                )
            },
            optionsSheetContent = {
                val context = LocalContext.current
                val clipboard = LocalClipboard.current
                val scope = rememberCoroutineScope()

                val unused = material.usedBy.isEmpty()
                var showDeleteDialog by remember { mutableStateOf(false) }

                VerticalSegmentor(
                    {
                        ExpressiveButtonRow(
                            title = stringResource(R.string.mapsMaterials_material_copyUrl),
                            icon = {
                                ExpressiveRowIcon(
                                    painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
                                )
                            }
                        ) { scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(
                                null, material.url
                            )))
                            topToastState.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
                        } }
                    },
                    {
                        ExpressiveButtonRow(
                            title = stringResource(R.string.mapsMaterials_material_delete),
                            description = if (unused) stringResource(R.string.mapsMaterials_unused)
                            else stringResource(R.string.mapsMaterials_material_delete_description)
                                .replace("%n", material.usedBy.size.toString()),
                            contentColor = if (unused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            icon = {
                                ExpressiveRowIcon(
                                    painter = rememberVectorPainter(Icons.Rounded.Delete),
                                    containerColor = if (unused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        ) {
                            showDeleteDialog = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                if (showDeleteDialog) DeleteConfirmationDialog(
                    name = material.name,
                    onDismissRequest = { showDeleteDialog = false }
                ) {
                    deleteDownloadableMaterial(material, context)
                    showDeleteDialog = false
                    appState.mediaOverlayData = null
                }
            }
        )
    }

    fun replaceOldObjects(context: Context) {
        val replacedObjects = mapEditor!!.replaceOldObjects()
        topToastState.showToast(
            text = context.getString(R.string.mapsEdit_replacedOldObjects).replace("%n", replacedObjects.toString()),
            icon = Icons.Rounded.Done
        )
    }

    fun getObjectFilterMatches(): List<String> {
        return mapEditor!!.editor.getObjectsMatchingFilter(objectFilter)
    }

    fun removeObjectFilterMatches(context: Context) {
        val removedObjects = mapEditor!!.editor.removeObjectsMatchingFilter(objectFilter)
        objectFilter = LACMapObjectFilter()
        topToastState.showToast(
            text = context.getString(R.string.mapsEdit_filterObjects_removedMatches).replace("%n", removedObjects.toString()),
            icon = Icons.Rounded.Delete
        )
    }

    @SuppressLint("Recycle")
    suspend fun saveAndFinishEditing(context: Context) {
        val mapName = map.file.nameWithoutExtension
        progressState.currentProgress = Progress(
            context.getString(R.string.maps_edit_saving).replace("{NAME}", mapName)
        )
        withContext(Dispatchers.IO) {
            try {
                val newContent = mapEditor!!.editor.applyChanges()
                map.file.writeFile(newContent, context)
                topToastState.showToast(
                    text = context.getString(R.string.maps_edit_saved).replace("{NAME}", mapName),
                    icon = Icons.Rounded.Save
                )
            } catch (e: Exception) {
                topToastState.showReportableErrorToast(e)
                Log.e(TAG, "saveAndFinishEditing: ", e)
            }
        }
        onNavigateBackRequest()
        progressState.currentProgress = null
    }

    fun finishEditingWithoutSaving() {
        onNavigateBackRequest()
    }

    fun showSaveWarning() {
        saveWarningShown = true
    }
}