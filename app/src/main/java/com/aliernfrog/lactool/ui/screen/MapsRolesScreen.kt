package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.MapRole
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MapsRolesScreen(
    mapsEditViewModel: MapsEditViewModel = getViewModel(),
    onNavigateBackRequest: () -> Unit
) {
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
                Text(
                    text = stringResource(R.string.mapsRoles_showingCount)
                        .replace("{COUNT}", roles.size.toString()),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            items(roles) {
                MapRole(it) { scope.launch { mapsEditViewModel.showRoleSheet(it) } }
            }
            item {
                Spacer(Modifier.systemBarsPadding().height(70.dp))
            }
        }
    }
}