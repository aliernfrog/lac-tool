package com.aliernfrog.lactool.ui.screen.maps

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.component.maps.GridMapItem
import com.aliernfrog.lactool.ui.component.maps.ListMapItem
import com.aliernfrog.lactool.ui.component.util.LazyGridScrollAccessibilityListener
import com.aliernfrog.lactool.ui.component.util.LazyListScrollAccessibilityListener
import com.aliernfrog.lactool.ui.sheet.ListViewOptionsSheet
import com.aliernfrog.lactool.ui.viewmodel.MapsListViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.staticutil.UriUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import io.github.aliernfrog.pftool_shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.pftool_shared.ui.component.FloatingActionButton
import io.github.aliernfrog.pftool_shared.ui.component.LazyAdaptiveVerticalGrid
import io.github.aliernfrog.pftool_shared.ui.component.SEGMENTOR_DEFAULT_ROUNDNESS
import io.github.aliernfrog.pftool_shared.ui.component.SEGMENTOR_SMALL_ROUNDNESS
import io.github.aliernfrog.pftool_shared.ui.component.SingleChoiceConnectedButtonGroup
import io.github.aliernfrog.pftool_shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.pftool_shared.ui.theme.AppFABPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3WindowSizeClassApi::class)
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
    val listStylePref = mapsListViewModel.prefs.mapsListOptions.styleGroup.getCurrent()
    val gridMaxLineSpanPref = mapsListViewModel.prefs.mapsListOptions.gridMaxLineSpanGroup.getCurrent()
    val listStyle = ListStyle.entries[listStylePref.value]
    val showMapThumbnails = mapsListViewModel.prefs.showMapThumbnailsInList.value
    var multiSelectionDropdownShown by remember { mutableStateOf(false) }
    var areAllShownMapsSelected by remember { mutableStateOf(false) }
    var showFABLabel by remember { mutableStateOf(true) }

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
        withContext(Dispatchers.IO) {
            mapsViewModel.loadMaps(context)
        }
    }

    if (isMultiSelecting) LaunchedEffect(
        mapsListViewModel.pagerState.currentPage,
        mapsListViewModel.selectedMaps.size
    ) {
        val shownMaps = mapsListViewModel.getCurrentlyShownMaps()
        areAllShownMapsSelected = mapsListViewModel.selectedMaps.containsAll(shownMaps)
    }

    BackHandler(
        enabled = isMultiSelecting || onBackClick != null
    ) {
        if (isMultiSelecting) mapsListViewModel.selectedMaps.clear()
        else onBackClick?.invoke()
    }

    ListViewOptionsSheet(
        sheetState = mapsListViewModel.listViewOptionsSheetState,
        listViewOptionsPreference = mapsListViewModel.prefs.mapsListOptions
    )

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
                        if (multiSelecting && showMultiSelectionOptions) {
                            IconButton(
                                shapes = IconButtonDefaults.shapes(),
                                onClick = {
                                    val shownMaps = mapsListViewModel.getCurrentlyShownMaps()
                                    if (areAllShownMapsSelected) mapsListViewModel.selectedMaps.removeAll(shownMaps)
                                    else mapsListViewModel.selectedMaps.addAll(
                                        shownMaps.filter { !mapsListViewModel.selectedMaps.contains(it) }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = if (areAllShownMapsSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = stringResource(
                                        if (areAllShownMapsSelected) R.string.action_select_deselectAll else R.string.action_select_selectAll
                                    )
                                )
                            }
                            Box {
                                IconButton(
                                    shapes = IconButtonDefaults.shapes(),
                                    onClick = { multiSelectionDropdownShown = true }
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
                            }
                        } else {
                            Crossfade(mapsViewModel.isLoadingMaps) { showLoading ->
                                if (showLoading) CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp).padding(8.dp)
                                )
                                else IconButton(
                                    shapes = IconButtonDefaults.shapes(),
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
                        showText = showFABLabel,
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
        fun MapItem(map: MapFile, isGrid: Boolean, modifier: Modifier = Modifier) {
            val selected = if (isMultiSelecting) mapsListViewModel.isMapSelected(map) else null
            fun toggleSelection() {
                mapsListViewModel.selectedMaps.run {
                    if (selected == true) remove(map) else add(map)
                }
            }

            val scale by animateFloatAsState(
                if (selected == true) 0.95f else 1f
            )
            val roundness by animateDpAsState(
                if (selected == true) SEGMENTOR_DEFAULT_ROUNDNESS else SEGMENTOR_SMALL_ROUNDNESS
            )

            if (isGrid) GridMapItem(
                map = map,
                selected = selected,
                showMapThumbnail = showMapThumbnails,
                onSelectedChange = { toggleSelection() },
                onLongClick = { toggleSelection() },
                modifier = modifier.scale(scale).clip(RoundedCornerShape(roundness))
            ) {
                if (isMultiSelecting) toggleSelection()
                else onMapPick(map)
            }
            else ListMapItem(
                map = map,
                selected = selected,
                showMapThumbnail = showMapThumbnails,
                onSelectedChange = { toggleSelection() },
                onLongClick = { toggleSelection() },
                modifier = modifier.scale(scale).clip(RoundedCornerShape(roundness))
            ) {
                if (isMultiSelecting) toggleSelection()
                else onMapPick(map)
            }
        }

        HorizontalPager(
            state = mapsListViewModel.pagerState,
            beyondViewportPageCount = 1
        ) { page ->
            val lazyListState = rememberLazyListState()
            val lazyGridState = rememberLazyGridState()

            val segment = mapsListViewModel.availableSegments[page]
            val mapsToShow = mapsListViewModel.getFilteredMaps(segment)

            LazyListScrollAccessibilityListener(
                lazyListState = lazyListState,
                onShowLabelsStateChange = { showFABLabel = it }
            )

            LazyGridScrollAccessibilityListener(
                lazyGridState = lazyGridState,
                onShowLabelsStateChange = { showFABLabel = it }
            )

            AnimatedContent(targetState = listStyle) { style ->
                when (style) {
                    ListStyle.LIST -> LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Header(segment, page, mapsToShow, Modifier.padding(horizontal = 12.dp))
                        }

                        itemsIndexed(mapsToShow) { index, map ->
                            MapItem(
                                map, isGrid = false,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .verticalSegmentedShape(
                                        index = index,
                                        totalSize = mapsToShow.size,
                                        spacing = 4.dp,
                                        containerColor = Color.Transparent
                                    )
                            )
                        }

                        item {
                            Footer()
                        }
                    }
                    ListStyle.GRID -> LazyAdaptiveVerticalGrid(
                        state = lazyGridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        maxLineSpan = gridMaxLineSpanPref.value
                    ) { maxLineSpan: Int ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Header(segment, page, mapsToShow, Modifier.padding(horizontal = 2.dp))
                        }

                        items(mapsToShow) { map ->
                            MapItem(
                                map = map,
                                isGrid = true,
                                modifier = Modifier.padding(2.dp)
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Footer()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(
    segment: MapsListSegment,
    segmentIndex: Int,
    mapsToShow: List<MapFile>,
    modifier: Modifier = Modifier,
    mapsViewModel: MapsViewModel = koinViewModel(),
    mapsListViewModel: MapsListViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    Column(modifier) {
        Search(
            searchQuery = mapsListViewModel.searchQuery,
            onSearchQueryChange = { mapsListViewModel.searchQuery = it }
        )
        SingleChoiceConnectedButtonGroup(
            choices = mapsListViewModel.availableSegments.map {
                stringResource(it.labelId)
            },
            selectedIndex = segmentIndex,
            modifier = Modifier.fillMaxWidth()
        ) { scope.launch {
            mapsListViewModel.pagerState.animateScrollToPage(it, animationSpec = tween(300))
        } }
        if (mapsToShow.isEmpty()) {
            if (mapsViewModel.isLoadingMaps) Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingIndicator()
            }
            else AnimatedContent(mapsListViewModel.searchQuery.isNotEmpty()) { searching ->
                ErrorWithIcon(
                    error = stringResource(
                        if (searching) R.string.mapsList_searchNoMatches else segment.noMapsTextId
                    ),
                    painter = rememberVectorPainter(
                        if (searching) Icons.Rounded.SearchOff else Icons.Rounded.LocationOff
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else Text(
            text = stringResource(R.string.mapsList_count)
                .replace("{COUNT}", mapsToShow.size.toString()),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .padding(bottom = 4.dp)
        )
    }
}

@Composable
private fun Footer() {
    Spacer(Modifier.navigationBarsPadding().height(AppFABPadding))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Search(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    mapsListViewModel: MapsListViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
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
                        onClick = { onSearchQueryChange("") },
                        shapes = IconButtonDefaults.shapes()
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
                    IconButton(
                        onClick = { scope.launch {
                            mapsListViewModel.listViewOptionsSheetState.show()
                        } },
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.list_options)
                        )
                    }
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