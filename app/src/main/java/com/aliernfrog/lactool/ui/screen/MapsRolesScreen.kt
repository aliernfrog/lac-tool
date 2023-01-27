package com.aliernfrog.lactool.ui.screen

import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.FloatingActionButton
import com.aliernfrog.lactool.ui.component.MapRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MapsRolesScreen(mapsEditState: MapsEditState, navController: NavController) {
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.mapsRoles),
        topAppBarState = mapsEditState.rolesTopAppBarState,
        floatingActionButton = {
            FloatingActionButton(
                icon = Icons.Rounded.Add,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                scope.launch { mapsEditState.addRoleSheetState.show() }
            }
        },
        onBackClick = {
            navController.popBackStack()
        }
    ) { paddingValues ->
        val roles = mapsEditState.mapEditor?.mapRoles ?: mutableListOf()
        LazyColumn(
            state = mapsEditState.rolesLazyListState,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            item {
                Text(
                    text = stringResource(R.string.mapsRoles_showingCount).replace("%COUNT%", roles.size.toString()),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            items(roles) {
                MapRole(it) { scope.launch { mapsEditState.showRoleSheet(it) } }
            }
            item {
                Spacer(Modifier.systemBarsPadding().height(70.dp))
            }
        }
    }
}