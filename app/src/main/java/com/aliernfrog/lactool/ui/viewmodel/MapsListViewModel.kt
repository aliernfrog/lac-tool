package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.enum.MapAction
import com.aliernfrog.lactool.enum.MapsListSegment
import com.aliernfrog.lactool.enum.ListSorting
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState

class MapsListViewModel(
    val topToastState: TopToastState,
    val prefs: PreferenceManager,
    private val mapsViewModel: MapsViewModel
) : ViewModel() {
    var searchQuery by mutableStateOf("")
    var selectedMaps = mutableStateListOf<MapFile>()

    val availableSegments: List<MapsListSegment>
        get() = MapsListSegment.entries.filter {
            !(mapsViewModel.sharedMaps.isEmpty() && it == MapsListSegment.SHARED)
        }

    val pagerState = PagerState { availableSegments.size }

    val selectedMapsActions: List<MapAction>
        get() = MapAction.entries.filter { action ->
            action.availableForMultiSelection && !selectedMaps.any { map ->
                !action.availableFor(map)
            }
        }

    fun getFilteredMaps(segment: MapsListSegment) = segment.getMaps(mapsViewModel)
        .filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
        .sortedWith { m1, m2 ->
            ListSorting.entries[prefs.mapsListSorting.value].comparator.compare(m1.file, m2.file)
        }
        .let {
            if (prefs.mapsListSortingReversed.value) it.reversed() else it
        }

    fun isMapSelected(map: MapFile): Boolean {
        return selectedMaps.any {
            it.path == map.path
        }
    }
}