package com.aliernfrog.lactool.ui.screen.maps

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.ListStyle
import com.aliernfrog.lactool.enum.MapAction
import com.aliernfrog.lactool.enum.MapsListSegment
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.LazyAdaptiveVerticalGrid
import com.aliernfrog.lactool.ui.component.ListViewOptionsDropdown
import com.aliernfrog.lactool.ui.component.SegmentedButtons
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.maps.GridMapItem
import com.aliernfrog.lactool.ui.component.maps.ListMapItem
import com.aliernfrog.lactool.ui.viewmodel.MapsListViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsListScreen(
    title: String = stringResource(R.string.mapsList_pickMap),
    mapsListViewModel: MapsListViewModel = koinViewModel(),
    mapsViewModel: MapsViewModel = koinViewModel(),
    showMultiSelectionOptions: Boolean = true,
    multiSelectFloatingActionButton: @Composable (selectedMaps: List<MapFile>, clearSelection: () -> Unit) -> Unit = { _, _ -> },
    onNavigateSettingsRequest: (() -> Unit)? = null,
    onBackClick: (() -> Unit)?,
    onMapPick: (MapFile) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isMultiSelecting = mapsListViewModel.selectedMaps.isNotEmpty()
    val listStyle = ListStyle.entries[mapsListViewModel.prefs.mapsListStyle.value]
    val showMapThumbnails = mapsListViewModel.prefs.showMapThumbnailsInList.value
    var multiSelectionDropdownShown by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.data?.data != null) scope.launch {
            withContext(Dispatchers.IO) {
                val cachedFile = UriUtil.cacheFile(
                    uri = it.data?.data!!,
                    parentName = "maps",
                    context = context
                )
                if (cachedFile != null) onMapPick(MapFile(FileWrapper(cachedFile)))
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

    BackHandler(
        enabled = isMultiSelecting || onBackClick != null
    ) {
        if (isMultiSelecting) mapsListViewModel.selectedMaps.clear()
        else onBackClick?.invoke()
    }

    AppScaffold(
        topBar = { scrollBehavior ->
            AnimatedContent(targetState = isMultiSelecting) { multiSelecting ->
                AppTopBar(
                    title = if (!multiSelecting) title
                    else stringResource(R.string.mapsList_multiSelection)
                        .replace("{COUNT}", mapsListViewModel.selectedMaps.size.toString()),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = if (multiSelecting) Icons.Default.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                    onNavigationClick = if (multiSelecting) { {
                        mapsListViewModel.selectedMaps.clear()
                    } } else onBackClick,
                    actions = {
                        if (multiSelecting && showMultiSelectionOptions) Box {
                            IconButton(
                                onClick = {
                                    multiSelectionDropdownShown = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.action_more)
                                )
                            }
                            MultiSelectionDropdown(
                                expanded = multiSelectionDropdownShown,
                                maps = mapsListViewModel.selectedMaps,
                                actions = mapsListViewModel.selectedMapsActions,
                                onDismissRequest = { clearSelection ->
                                    multiSelectionDropdownShown = false
                                    if (clearSelection) mapsListViewModel.selectedMaps.clear()
                                }
                            )
                        } else {
                            Crossfade(mapsViewModel.isLoadingMaps) { showLoading ->
                                if (showLoading) CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(8.dp)
                                )
                                else IconButton(
                                    onClick = {
                                        scope.launch {
                                            mapsViewModel.loadMaps(context)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.mapsList_reload)
                                    )
                                }
                            }
                            onNavigateSettingsRequest?.let {
                                SettingsButton(onClick = it)
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = !isMultiSelecting,
                modifier = Modifier
                    .navigationBarsPadding()
                    // since we are adding 16.dp padding below, add offset to bring it back to where its supposed to be
                    .offset(x = 16.dp, y = 16.dp)
            ) { showStorage ->
                Box(
                    // padding so that FAB shadow doesnt get cropped
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (showStorage) FloatingActionButton(
                        icon = Icons.Outlined.SdCard,
                        text = stringResource(R.string.mapsList_storage),
                        onClick = {
                            val intent = Intent(Intent.ACTION_GET_CONTENT).setType("text/plain")
                            launcher.launch(intent)
                        }
                    ) else multiSelectFloatingActionButton(mapsListViewModel.selectedMaps) {
                        mapsListViewModel.selectedMaps.clear()
                    }
                }
            }
        }
    ) {
        @Composable
        fun MapItem(map: MapFile, isGrid: Boolean) {
            val selected = if (isMultiSelecting) mapsListViewModel.isMapSelected(map) else null
            fun toggleSelection() {
                mapsListViewModel.selectedMaps.run {
                    if (selected == true) remove(map) else add(map)
                }
            }

            if (isGrid) GridMapItem(
                map = map,
                selected = selected,
                showMapThumbnail = showMapThumbnails,
                onSelectedChange = { toggleSelection() },
                onLongClick = { toggleSelection() }
            ) {
                if (isMultiSelecting) toggleSelection()
                else onMapPick(map)
            }
            else ListMapItem(
                map = map,
                selected = selected,
                showMapThumbnail = showMapThumbnails,
                onSelectedChange = { toggleSelection() },
                onLongClick = { toggleSelection() }
            ) {
                if (isMultiSelecting) toggleSelection()
                else onMapPick(map)
            }
        }

        BoxWithConstraints {
            val viewportHeight = maxHeight
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Header()

                HorizontalPager(
                    state = mapsListViewModel.pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier
                        .height(viewportHeight)
                        .nestedScroll(remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    return if (available.y > 0) Offset.Zero else Offset(
                                        x = 0f,
                                        y = -scrollState.dispatchRawDelta(-available.y)
                                    )
                                }
                            }
                        })
                ) { page ->
                    val segment = mapsListViewModel.availableSegments[page]
                    val mapsToShow = mapsListViewModel.getFilteredMaps(segment)

                    AnimatedContent(targetState = listStyle) { style ->
                        when (style) {
                            ListStyle.LIST -> LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    SegmentSummary(segment, mapsToShow)
                                }

                                items(mapsToShow) {
                                    MapItem(it, isGrid = false)
                                }

                                item {
                                    Footer()
                                }
                            }
                            ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    SegmentSummary(segment, mapsToShow)
                                }

                                items(mapsToShow) {
                                    MapItem(it, isGrid = true)
                                }

                                item { Footer() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    mapsListViewModel: MapsListViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    Column {
        Search(
            searchQuery = mapsListViewModel.searchQuery,
            onSearchQueryChange = { mapsListViewModel.searchQuery = it }
        )
        SegmentedButtons(
            options = mapsListViewModel.availableSegments.map {
                stringResource(it.labelId)
            },
            selectedIndex = mapsListViewModel.pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) { scope.launch {
            mapsListViewModel.pagerState.animateScrollToPage(it, animationSpec = tween(300))
        } }
    }
}

@Composable
private fun SegmentSummary(
    segment: MapsListSegment,
    mapsToShow: List<MapFile>,
    mapsListViewModel: MapsListViewModel = koinViewModel(),
    mapsViewModel: MapsViewModel = koinViewModel()
) {
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
                else segment.noMapsTextId
            ),
            painter = rememberVectorPainter(Icons.Rounded.LocationOff),
            modifier = Modifier.fillMaxWidth()
        )
    } else Text(
        text = stringResource(R.string.mapsList_count)
            .replace("{COUNT}", mapsToShow.size.toString()),
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun Footer() {
    Spacer(Modifier.navigationBarsPadding().padding(bottom = 70.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Search(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val mapsListViewModel = koinViewModel<MapsListViewModel>()
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
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
                            contentDescription = stringResource(R.string.list_sorting)
                        )
                    }
                    ListViewOptionsDropdown(
                        expanded = sortingOptionsShown,
                        onDismissRequest = { sortingOptionsShown = false },
                        sortingPref = mapsListViewModel.prefs.mapsListSorting,
                        sortingReversedPref = mapsListViewModel.prefs.mapsListSortingReversed,
                        stylePref = mapsListViewModel.prefs.mapsListStyle
                    )
                },
                placeholder = {
                    Text(stringResource(R.string.mapsList_search))
                }
            )
        },
        expanded = false,
        onExpandedChange = {},
        content = {},
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-12).dp)
            .padding(
                start = 8.dp,
                end = 8.dp
            )
    )
}

@Composable
private fun MultiSelectionDropdown(
    expanded: Boolean,
    maps: List<MapFile>,
    actions: List<MapAction>,
    onDismissRequest: (clearSelection: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (maps.isNotEmpty()) DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest(false) }
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                text = { Text(stringResource(action.shortLabel)) },
                leadingIcon = {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null
                    )
                },
                colors = if (action.destructive) MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error,
                    leadingIconColor = MaterialTheme.colorScheme.error
                ) else MenuDefaults.itemColors(),
                onClick = { scope.launch {
                    onDismissRequest(false)
                    action.execute(context = context, *maps.toTypedArray())
                    onDismissRequest(true) // clear selection
                } }
            )
        }
    }
}