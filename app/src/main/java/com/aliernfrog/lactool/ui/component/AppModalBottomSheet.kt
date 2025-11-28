package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.aliernfrog.lactool.ui.viewmodel.InsetsViewModel
import io.github.aliernfrog.pftool_shared.ui.component.AppModalBottomSheet
import io.github.aliernfrog.pftool_shared.ui.component.BaseModalBottomSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    title: String? = null,
    sheetState: SheetState,
    sheetScrollState: ScrollState = rememberScrollState(),
    insetsViewModel: InsetsViewModel = koinViewModel(),
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetContent: @Composable ColumnScope.() -> Unit
) {
    AppModalBottomSheet(
        title = title,
        sheetState = sheetState,
        topPadding = insetsViewModel.topPadding,
        bottomPadding = insetsViewModel.bottomPadding,
        sheetScrollState = sheetScrollState,
        dragHandle = dragHandle,
        sheetContent = sheetContent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseModalBottomSheet(
    sheetState: SheetState,
    insetsViewModel: InsetsViewModel = koinViewModel(),
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.(bottomPadding: Dp) -> Unit
) {
    BaseModalBottomSheet(
        sheetState = sheetState,
        topPadding = insetsViewModel.topPadding,
        bottomPadding = insetsViewModel.bottomPadding,
        dragHandle = dragHandle,
        content = content
    )
}