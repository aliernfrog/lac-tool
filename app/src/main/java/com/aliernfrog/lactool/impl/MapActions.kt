package com.aliernfrog.lactool.impl

import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditLocationAlt
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.aliernfrog.pftool_shared.data.MapAction
import io.github.aliernfrog.pftool_shared.enum.MapImportedState
import io.github.aliernfrog.shared.util.SharedString
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.util.SubDestination
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.staticutil.FileUtil
import io.github.aliernfrog.pftool_shared.data.MapActionResult
import io.github.aliernfrog.pftool_shared.impl.Progress

@Suppress("UNCHECKED_CAST")
val mapActions = listOf(
    MapAction(
        id = MapAction.RENAME_ID,
        shortLabel = SharedString.fromResId(R.string.maps_rename),
        icon = Icons.Rounded.Edit,
        availableForMultiSelection = false,
        availableFor = { it.importedState != MapImportedState.NONE },
        execute = { context, maps, args ->
            val first = maps.first() as MapFile
            val newName = args.resolveMapName(fallback = first.name)
            first.progressState.currentProgress = Progress(
                description = context.getString(R.string.maps_renaming)
                    .replace("{NAME}", first.name)
                    .replace("{NEW_NAME}", newName)
            )
            first.runInIOThreadSafe {
                val result = first.rename(newName = newName)
                if (!result.successful) return@runInIOThreadSafe first.topToastState.showErrorToast(
                    text = result.message ?: R.string.warning_error
                )
                result.newFile?.let {
                    first.mapsState.viewMapDetails(it)
                }
                first.topToastState.showToast(
                    text = context.getString(result.message ?: R.string.maps_renamed)
                        .replace("{NAME}", newName),
                    icon = Icons.Rounded.Edit
                )
            }
            first.progressState.currentProgress = null
            first.mapsState.loadMaps(context)
        }
    ),

    MapAction(
        id = MapAction.DUPLICATE_ID,
        shortLabel = SharedString.fromResId(R.string.maps_duplicate),
        icon = Icons.Rounded.FileCopy,
        availableForMultiSelection = false,
        availableFor = { it.importedState != MapImportedState.NONE },
        execute = { context, maps, args ->
            val first = maps.first() as MapFile
            val newName = args.resolveMapName(fallback = first.name)
            first.progressState.currentProgress = Progress(
                description = context.getString(R.string.maps_duplicating)
                    .replace("{NAME}", first.name)
                    .replace("{NEW_NAME}", newName)
            )
            first.runInIOThreadSafe {
                val result = first.duplicate(context, newName = newName)
                if (!result.successful) return@runInIOThreadSafe first.topToastState.showErrorToast(
                    text = result.message ?: R.string.warning_error
                )
                result.newFile?.let {
                    first.mapsState.viewMapDetails(it)
                }
                first.topToastState.showToast(
                    text = context.getString(result.message ?: R.string.maps_duplicated)
                        .replace("{NAME}", newName),
                    icon = Icons.Rounded.FileCopy
                )
            }
            first.progressState.currentProgress = null
            first.mapsState.loadMaps(context)
        }
    ),

    MapAction(
        id = "import",
        shortLabel = SharedString.fromResId(R.string.maps_import_short),
        longLabel = SharedString.fromResId(R.string.maps_import),
        icon = Icons.Rounded.Download,
        availableForMultiSelection = true,
        availableFor = { it.importedState != MapImportedState.IMPORTED },
        execute = { context, maps, args ->
            runIOAction(
                maps = maps as List<MapFile>,
                context = context,
                singleSuccessMessageId = R.string.maps_imported_single,
                multipleSuccessMessageId = R.string.maps_imported_multiple,
                singleProcessingMessageId = R.string.maps_importing_single,
                multipleProcessingMessageId = R.string.maps_importing_multiple,
                successIcon = Icons.Rounded.Download,
                newName = args.resolveMapName(fallback = maps.first().name)
            ) { map ->
                map.import(
                    context = context,
                    withName = args.resolveMapName(fallback = map.name)
                )
            }
        }
    ),

    MapAction(
        id = "export",
        shortLabel = SharedString.fromResId(R.string.maps_export_short),
        longLabel = SharedString.fromResId(R.string.maps_export),
        icon = Icons.Rounded.Upload,
        availableForMultiSelection = true,
        availableFor = { it.importedState == MapImportedState.IMPORTED },
        execute = { context, maps, args ->
            runIOAction(
                maps = maps as List<MapFile>,
                context = context,
                singleSuccessMessageId = R.string.maps_exported_single,
                multipleSuccessMessageId = R.string.maps_exported_multiple,
                singleProcessingMessageId = R.string.maps_exporting_single,
                multipleProcessingMessageId = R.string.maps_exporting_multiple,
                successIcon = Icons.Rounded.Upload,
                newName = args.resolveMapName(fallback = maps.first().name)
            ) { map ->
                map.export(
                    context = context,
                    withName = args.resolveMapName(fallback = map.name)
                )
            }
        }
    ),

    MapAction(
        id = "exportCustomTarget",
        shortLabel = SharedString.fromResId(R.string.maps_exportCustomTarget),
        icon = Icons.AutoMirrored.Filled.AddToHomeScreen,
        availableForMultiSelection = false,
        availableFor = { true },
        execute = { context, maps, args ->
            val first = maps.first() as MapFile
            val withName = args.resolveMapName(fallback = maps.first().name)
            first.progressState.currentProgress = Progress(
                description = context.getString(R.string.maps_exportCustomTarget_exporting)
                    .replace("{NAME}", first.name)
            )
            first.runInIOThreadSafe {
                val result = first.exportToCustomLocation(context, withName)
                if (result.successful) first.topToastState.showToast(
                    text = context.getString(R.string.maps_exportCustomTarget_exported)
                        .replace("{NAME}", first.name),
                    icon = Icons.AutoMirrored.Filled.AddToHomeScreen
                )
                else first.topToastState.showErrorToast(result.message ?: R.string.warning_error)
                result.newFile?.let { first.mapsState.viewMapDetails(it) }
            }
            first.progressState.currentProgress = null
        }
    ),

    MapAction(
        id = "share",
        shortLabel = SharedString.fromResId(R.string.maps_share_short),
        longLabel = SharedString.fromResId(R.string.maps_share),
        icon = Icons.Rounded.Share,
        availableForMultiSelection = true,
        availableFor = { true },
        execute = { context, maps, _ ->
            val first = maps.first() as MapFile
            val files = maps.map { it.file }
            first.progressState.currentProgress = Progress(
                description = context.getString(R.string.info_sharing)
            )
            first.runInIOThreadSafe {
                FileUtil.shareFiles(*files.toTypedArray(), context = context)
            }
            first.progressState.currentProgress = null
        }
    ),

    MapAction(
        id = "edit",
        shortLabel = SharedString.fromResId(R.string.maps_edit),
        description = SharedString.fromResId(R.string.maps_edit_description),
        icon = Icons.Rounded.EditLocationAlt,
        availableForMultiSelection = false,
        availableFor = { true },
        execute = { _, maps, _ ->
            val first = maps.first() as MapFile
            Log.d(TAG, first.name)
            first.appState.navigationBackStack.add(
                SubDestination.MapsEdit.Root(map = first)
            )
        }
    ),

    MapAction(
        id = "merge",
        shortLabel = SharedString.fromResId(R.string.maps_merge_short),
        longLabel = SharedString.fromResId(R.string.maps_merge),
        icon = Icons.Rounded.AddLocationAlt,
        availableForMultiSelection = true,
        availableFor = { true },
        execute = { _, maps, _ ->
            maps as List<MapFile>
            maps.first().appState.navigationBackStack.add(
                SubDestination.MapsMerge(maps = maps)
            )
        }
    ),

    MapAction(
        id = "delete",
        shortLabel = SharedString.fromResId(R.string.maps_delete_short),
        longLabel = SharedString.fromResId(R.string.maps_delete),
        icon = Icons.Rounded.Delete,
        destructive = true,
        availableForMultiSelection = true,
        availableFor = { it.importedState != MapImportedState.NONE },
        execute = { _, maps, _ ->
            maps as List<MapFile>
            maps.first().mapsState.mapsPendingDelete = maps.toList()
        }
    )
)

private suspend fun runIOAction(
    maps: List<MapFile>,
    context: Context,
    singleSuccessMessageId: Int,
    multipleSuccessMessageId: Int,
    singleProcessingMessageId: Int,
    multipleProcessingMessageId: Int,
    successIcon: ImageVector,
    newName: String,
    result: (MapFile) -> MapActionResult
) {
    val first = maps.first()
    val total = maps.size
    val isSingle = total == 1

    var passedProgress = 0
    fun getProgress(): Progress {
        return Progress(
            description = if (isSingle) context.getString(singleProcessingMessageId)
                .replace("{NAME}", first.name)
                .replace("{NEW_NAME}", newName)
            else context.getString(multipleProcessingMessageId)
                .replace("{DONE}", passedProgress.toString())
                .replace("{TOTAL}", total.toString()),
            totalProgress = total.toLong(),
            passedProgress = passedProgress.toLong()
        )
    }

    first.progressState.currentProgress = getProgress()
    first.runInIOThreadSafe {
        val results = maps.map {
            val executionResult = result(it)
            passedProgress++
            first.progressState.currentProgress = getProgress()
            it.name to executionResult
        }
        if (isSingle) results.first().let { (mapName, result) ->
            if (result.successful) first.topToastState.showToast(
                text = context.getString(singleSuccessMessageId).replace("{NAME}", mapName),
                icon = successIcon
            ) else first.topToastState.showErrorToast(
                text = context.getString(result.message ?: R.string.warning_error)
            )
            result.newFile?.let { first.mapsState.viewMapDetails(it) }
        } else {
            val successes = results.filter { it.second.successful }
            val fails = results.filter { !it.second.successful }
            if (fails.isEmpty()) first.topToastState.showToast(
                text = context.getString(multipleSuccessMessageId).replace("{COUNT}", successes.size.toString()),
                icon = successIcon
            ) else first.mapsState.showActionFailedDialog(
                successes = successes,
                fails = fails
            )
        }
    }
    first.mapsState.loadMaps(context)
    first.progressState.currentProgress = null
}