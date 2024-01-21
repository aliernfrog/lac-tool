package com.aliernfrog.lactool.ui.screen.maps

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.maps.MapRoleRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.ui.sheet.AddRoleSheet
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.util.extension.removeHtml
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsRolesScreen(
    mapsEditViewModel: MapsEditViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.mapsRoles),
        topAppBarState = mapsEditViewModel.rolesTopAppBarState,
        floatingActionButton = {
            FloatingActionButton(
                icon = Icons.Rounded.Add,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                scope.launch { mapsEditViewModel.addRoleSheetState.show() }
            }
        },
        onBackClick = {
            onNavigateBackRequest()
        }
    ) {
        val roles = mapsEditViewModel.mapEditor?.mapRoles ?: mutableListOf()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = mapsEditViewModel.rolesLazyListState
        ) {
            item {
                Crossfade(
                    targetState = roles.isNotEmpty(),
                    modifier = Modifier.animateContentSize()
                ) { hasRoles ->
                    if (hasRoles) Text(
                        text = stringResource(R.string.mapsRoles_showingCount).replace("{COUNT}", roles.size.toString()),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) else ErrorWithIcon(
                        error = stringResource(R.string.mapsRoles_noRoles),
                        painter = rememberVectorPainter(Icons.Rounded.Style)
                    )
                }
            }
            itemsIndexed(roles) { index, it ->
                val expanded = mapsEditViewModel.rolesExpandedRoleIndex == index
                MapRoleRow(
                    role = it,
                    expanded = expanded,
                    showTopDivider = index != 0,
                    topToastState = mapsEditViewModel.topToastState,
                    onRoleDelete = { role ->
                        mapsEditViewModel.pendingRoleDelete = role
                    },
                    onClick = {
                        mapsEditViewModel.rolesExpandedRoleIndex = if (expanded) -1 else index
                    }
                )
            }
            item {
                Spacer(Modifier.systemBarsPadding().height(70.dp))
            }
        }
    }

    mapsEditViewModel.pendingRoleDelete?.let {
        DeleteConfirmationDialog(
            name = it.removeHtml(),
            onDismissRequest = { mapsEditViewModel.pendingRoleDelete = null }
        ) {
            mapsEditViewModel.rolesExpandedRoleIndex = -1
            mapsEditViewModel.pendingRoleDelete = null
            mapsEditViewModel.deleteRole(it, context)
        }
    }

    AddRoleSheet(
        state = mapsEditViewModel.addRoleSheetState,
        onRoleAdd = { mapsEditViewModel.addRole(it, context) }
    )
}