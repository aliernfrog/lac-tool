package com.aliernfrog.lactool.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.enum.MapAction
import com.aliernfrog.lactool.enum.MapsListSegment
import com.aliernfrog.lactool.enum.MapsListSortingType
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState

class MapsListViewModel(
    val topToastState: TopToastState,
    val prefs: PreferenceManager,
    private val mapsViewModel: MapsViewModel
) : ViewModel() {

    var searchQuery by mutableStateOf("")
    var chosenSegment by mutableStateOf(MapsListSegment.IMPORTED)
    var sorting by mutableStateOf(MapsListSortingType.ALPHABETICAL)
    var reverseList by mutableStateOf(false)
    var selectedMaps = mutableStateListOf<MapFile>()

    val selectedMapsActions: List<MapAction>
        get() = MapAction.entries.filter { action ->
            action.availableForMultiSelection && !selectedMaps.any { map ->
                !action.availableFor(map)
            }
        }

    /**
     * Map list with filters and sorting options applied.
     */
    val mapsToShow: List<MapFile>
        get() {
            val list = chosenSegment.getMaps(mapsViewModel)
                .filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }
                .sortedWith(sorting.comparator)
            return if (reverseList) list.reversed() else list
        }

    fun isMapSelected(map: MapFile): Boolean {
        return selectedMaps.any {
            it.path == map.path
        }
    }
}