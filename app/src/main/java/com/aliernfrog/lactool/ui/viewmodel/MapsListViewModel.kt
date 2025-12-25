package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.enum.MapAction
import com.aliernfrog.lactool.enum.MapsListSegment
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.extension.comparator
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.pftool_shared.enum.ListSorting
import io.github.aliernfrog.shared.ui.component.createSheetStateWithDensity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MapsListViewModel(
    val topToastState: TopToastState,
    val prefs: PreferenceManager,
    private val mapsViewModel: MapsViewModel,
    context: Context
) : ViewModel() {
    var searchQuery by mutableStateOf("")
    val selectedMaps = mutableStateListOf<MapFile>()
    val availableSegments = mutableStateListOf<MapsListSegment>()
    val pagerState = PagerState { availableSegments.size }
    val listViewOptionsSheetState = createSheetStateWithDensity(skipPartiallyExpanded = true, Density(context))

    val selectedMapsActions: List<MapAction>
        get() = MapAction.entries.filter { action ->
            action.availableForMultiSelection && !selectedMaps.any { map ->
                !action.availableFor(map)
            }
        }

    init {
        viewModelScope.launch {
            snapshotFlow { mapsViewModel.sharedMaps.isEmpty() }
                .collect { sharedMapsIsEmpty ->
                    availableSegments.clear()
                    availableSegments.addAll(MapsListSegment.entries.filter {
                        !(sharedMapsIsEmpty && it == MapsListSegment.SHARED)
                    })
                }
        }
    }

    fun getCurrentlyShownMaps(): List<MapFile> {
        val currentSegment = availableSegments[pagerState.currentPage]
        return getFilteredMaps(currentSegment)
    }

    fun getFilteredMaps(segment: MapsListSegment) = segment.getMaps(mapsViewModel)
        .filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith { m1, m2 ->
            ListSorting.entries[prefs.mapsListOptions.sorting.value].comparator().compare(m1.file, m2.file)
        }
        .let {
            if (prefs.mapsListOptions.sortingReversed.value) it.reversed() else it
        }

    fun isMapSelected(map: MapFile): Boolean {
        return selectedMaps.any {
            it.path == map.path
        }
    }
}