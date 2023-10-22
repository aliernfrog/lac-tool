package com.aliernfrog.lactool.ui.screen.maps

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.MapsListSegment
import com.aliernfrog.lactool.enum.MapsListSortingType
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.SegmentedButtons
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.component.maps.MapButton
import com.aliernfrog.lactool.ui.viewmodel.MapsListViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MapsListScreen(
    mapsListViewModel: MapsListViewModel = getViewModel(),
    mapsViewModel: MapsViewModel = getViewModel(),
    title: String = stringResource(R.string.mapsList_pickMap),
    onBackClick: (() -> Unit)?,
    onMapPick: (Any) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapsToShow = mapsListViewModel.mapsToShow

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data?.data != null) scope.launch {
            withContext(Dispatchers.IO) {
                val cachedFile = UriUtil.cacheFile(
                    uri = it.data?.data!!,
                    parentName = "maps",
                    context = context
                )
                if (cachedFile != null) onMapPick(cachedFile)
                else mapsListViewModel.topToastState.showToast(
                    text = R.string.mapsList_pickMap_failed,
                    icon = Icons.Rounded.PriorityHigh,
                    iconTintColor = TopToastColor.ERROR
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        mapsViewModel.loadMaps(context)
    }

    onBackClick?.let {
        BackHandler(onBack = it)
    }

    AppScaffold(
        title = title,
        topAppBarState = rememberTopAppBarState(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).setType("application/zip")
                    launcher.launch(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderZip,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.mapsList_storage))
            }
        },
        topBarActions = {
            Crossfade(mapsViewModel.isLoadingMaps) { showLoading ->
                if (showLoading) CircularProgressIndicator(
                    modifier = Modifier.size(48.dp).padding(8.dp)
                )
                else IconButton(
                    onClick = { scope.launch {
                        mapsViewModel.loadMaps(context)
                    } }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.mapsList_reload)
                    )
                }
            }
        },
        onBackClick = onBackClick
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Search(
                    searchQuery = mapsListViewModel.searchQuery,
                    onSearchQueryChange = { mapsListViewModel.searchQuery = it },
                    sorting = mapsListViewModel.sorting,
                    onSortingChange = { mapsListViewModel.sorting = it },
                    reversed = mapsListViewModel.reverseList,
                    onReversedChange = { mapsListViewModel.reverseList = it }
                )
                Filter(
                    selectedSegment = mapsListViewModel.chosenSegment,
                    onSelectedSegmentChange = {
                        mapsListViewModel.chosenSegment = it
                    }
                )
            }

            item {
                if (mapsToShow.isEmpty()) {
                    if (mapsViewModel.isLoadingMaps) Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }

                    else ErrorWithIcon(
                        error = stringResource(
                            if (mapsListViewModel.searchQuery.isNotEmpty()) R.string.mapsList_searchNoMatches
                            else mapsListViewModel.chosenSegment.noMapsTextId
                        ),
                        painter = rememberVectorPainter(Icons.Rounded.LocationOff)
                    )
                } else Text(
                    text = stringResource(R.string.mapsList_count)
                        .replace("{COUNT}", mapsToShow.size.toString()),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            items(mapsToShow) { map ->
                MapButton(
                    map = map,
                    showMapThumbnail = mapsListViewModel.prefs.showMapThumbnailsInList,
                    modifier = Modifier.animateItemPlacement()
                ) {
                    onMapPick(map)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Search(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sorting: MapsListSortingType,
    onSortingChange: (MapsListSortingType) -> Unit,
    reversed: Boolean,
    onReversedChange: (Boolean) -> Unit
) {
    SearchBar(
        query = searchQuery,
        onQueryChange = onSearchQueryChange,
        onSearch = {},
        active = false,
        onActiveChange = {},
        leadingIcon = {
            if (searchQuery.isNotEmpty()) IconButton(
                onClick = { onSearchQueryChange("") }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.mapsList_search_clear)
                )
            } else Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            var sortingOptionsShown by rememberSaveable { mutableStateOf(false) }
            IconButton(
                onClick = { sortingOptionsShown = true }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = stringResource(R.string.mapsList_sorting)
                )
            }
            DropdownMenu(
                expanded = sortingOptionsShown,
                onDismissRequest = { sortingOptionsShown = false }
            ) {
                Text(
                    text = stringResource(R.string.mapsList_sorting),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                DividerRow(Modifier.padding(vertical = 4.dp))
                MapsListSortingType.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(option.labelId)) },
                        leadingIcon = {
                            Icon(
                                imageVector = option.iconVector,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            RadioButton(
                                selected = option == sorting,
                                onClick = { onSortingChange(option) }
                            )
                        },
                        onClick = { onSortingChange(option) }
                    )
                }
                DividerRow(Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.mapsList_sorting_reverse)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Checkbox(
                            checked = reversed,
                            onCheckedChange = { onReversedChange(it) }
                        )
                    },
                    onClick = { onReversedChange(!reversed) }
                )
            }
        },
        placeholder = {
            Text(stringResource(R.string.mapsList_search))
        },
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-12).dp)
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
        content = {}
    )
}

@Composable
private fun Filter(
    selectedSegment: MapsListSegment,
    onSelectedSegmentChange: (MapsListSegment) -> Unit
) {
    SegmentedButtons(
        options = MapsListSegment.values().map {
            stringResource(it.labelId)
        },
        selectedIndex = selectedSegment.ordinal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        onSelectedSegmentChange(MapsListSegment.values()[it])
    }
}