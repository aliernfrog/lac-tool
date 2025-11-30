package com.aliernfrog.lactool.ui.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager
import io.github.aliernfrog.pftool_shared.enum.ListSorting
import io.github.aliernfrog.pftool_shared.enum.ListStyle
import io.github.aliernfrog.pftool_shared.ui.sheet.ListViewOptionsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListViewOptionsSheet(
    sheetState: SheetState,
    listViewOptionsPreference: BasePreferenceManager.ListViewOptionsPreference
) {
    val listStylePref = listViewOptionsPreference.styleGroup.getCurrent()
    val gridMaxLineSpanPref = listViewOptionsPreference.gridMaxLineSpanGroup.getCurrent()

    ListViewOptionsSheet(
        sheetState = sheetState,
        sorting = listViewOptionsPreference.sorting.let {
            ListSorting.entries[it.value]
        },
        onSortingChange = listViewOptionsPreference.sorting.let { pref -> {
            pref.value = it.ordinal
        } },
        sortingReversed = listViewOptionsPreference.sortingReversed.value,
        onSortingReversedChange = listViewOptionsPreference.sortingReversed.let { pref ->{
            pref.value = it
        } },
        style = ListStyle.entries[listStylePref.value],
        onStyleChange = { listStylePref.value = it.ordinal },
        gridMaxLineSpan = gridMaxLineSpanPref.value,
        onGridMaxLineSpanChange = { gridMaxLineSpanPref.value = it }
    )
}