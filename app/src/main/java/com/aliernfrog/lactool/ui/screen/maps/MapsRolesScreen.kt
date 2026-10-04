package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.maps.MapRoleRow
import com.aliernfrog.lactool.ui.sheet.AddRoleSheet
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.toptoast.state.TopToastState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.aliernfrog.shared.ui.component.AppScaffoldNoContentPadding
import io.github.aliernfrog.shared.ui.component.AppTopBar
import io.github.aliernfrog.shared.ui.component.ErrorWithIcon
import io.github.aliernfrog.shared.ui.component.FloatingActionButton
import io.github.aliernfrog.shared.ui.component.buildBottomSheetEnabledValues
import io.github.aliernfrog.shared.ui.component.util.BottomSpacer
import io.github.aliernfrog.shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.shared.ui.dialog.DeleteConfirmationDialog
import io.github.aliernfrog.shared.ui.theme.AppFABPadding
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsRolesScreen(
    topToastState: TopToastState,
    roles: List<String>,
    onAddRoleRequest: (String) -> Unit,
    onDeleteRoleRequest: (String) -> Unit,
    onNavigateBackRequest: () -> Unit
) {
    val hazeState = rememberHazeState()
    val addRoleSheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = buildBottomSheetEnabledValues(skipPartiallyExpanded = true)
    )
    val scope = rememberCoroutineScope()

    var expandedRoleIndex by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var pendingRoleDelete by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    AppScaffoldNoContentPadding(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.mapsRoles),
                hazeState = hazeState,
                scrollBehavior = scrollBehavior,
                onNavigationClick = {
                    onNavigateBackRequest()
                }
            )
        },
        topAppBarState = rememberTopAppBarState(),
        floatingActionButton = {
            FloatingActionButton(
                icon = Icons.Rounded.Add,
                modifier = Modifier.navigationBarsPadding()
            ) {
                scope.launch { addRoleSheetState.show() }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            item {
                Spacer(Modifier.height(paddingValues.calculateTopPadding()))
            }

            item {
                Crossfade(
                    targetState = roles.isNotEmpty(),
                    modifier = Modifier.animateContentSize()
                ) { hasRoles ->
                    if (hasRoles) Text(
                        text = stringResource(R.string.mapsRoles_showingCount).replace("{COUNT}", roles.size.toString()),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) else ErrorWithIcon(
                        description = stringResource(R.string.mapsRoles_noRoles),
                        icon = rememberVectorPainter(Icons.Rounded.Style),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            itemsIndexed(roles) { index, it ->
                val expanded = expandedRoleIndex == index
                MapRoleRow(
                    role = it,
                    expanded = expanded,
                    topToastState = topToastState,
                    onRoleDelete = { role ->
                        pendingRoleDelete = role
                    },
                    onClick = {
                        expandedRoleIndex = if (expanded) -1 else index
                    },
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .verticalSegmentedShape(
                            index = index,
                            totalSize = roles.size
                        )
                )
            }

            item {
                BottomSpacer(
                    Modifier
                        .padding(bottom = AppFABPadding + paddingValues.calculateBottomPadding())
                )
            }
        }
    }

    pendingRoleDelete?.let { role ->
        DeleteConfirmationDialog(
            name = role.removeHtml(),
            onDismissRequest = { pendingRoleDelete = null }
        ) {
            expandedRoleIndex = null
            pendingRoleDelete = null
            onDeleteRoleRequest(role)
        }
    }

    AddRoleSheet(
        state = addRoleSheetState,
        onRoleAdd = { role ->
            onAddRoleRequest(role)
        }
    )
}