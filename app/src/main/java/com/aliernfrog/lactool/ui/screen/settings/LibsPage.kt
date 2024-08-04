package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppSmallTopBar
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import com.mikepenz.aboutlibraries.entity.Library
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibsPage(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val librarySheetState = rememberModalBottomSheetState()
    var selectedLibrary by remember { mutableStateOf<Library?>(null) }

    AppScaffold(
        topBar = { scrollBehavior ->
            AppSmallTopBar(
                title = stringResource(R.string.settings_about_libs),
                scrollBehavior = scrollBehavior,
                onNavigationClick = onNavigateBackRequest
            )
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(settingsViewModel.libraries) { index, lib ->
                if (index != 0) DividerRow()
                ListItem(
                    headlineContent = { Text(lib.name) },
                    supportingContent = lib.description?.let { { Text(it) } },
                    overlineContent = {
                        Text(lib.developers.joinToString(", ") { it.name.toString() })
                    },
                    modifier = Modifier.clickable { scope.launch {
                        selectedLibrary = lib
                        librarySheetState.show()
                    } }
                )
            }

            item {
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }

    AppModalBottomSheet(sheetState = librarySheetState) {
        selectedLibrary?.let { lib ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = lib.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
                lib.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 8.dp)
                    )
                }

                val buttonsScrollState = rememberScrollState()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .horizontalFadingEdge(
                            scrollState = buttonsScrollState,
                            edgeColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
                        )
                        .horizontalScroll(buttonsScrollState)
                        .padding(horizontal = 8.dp)
                ) {
                    lib.website?.let {
                        if (it.contains("://")) Button(
                            onClick = { uriHandler.openUri(it) }
                        ) {
                            ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                            Text(stringResource(R.string.settings_about_libs_website))
                        }
                    }
                    lib.organization?.url?.let {
                        if (it.contains("://")) FilledTonalButton(
                            onClick = {
                                uriHandler.openUri(it)
                            }
                        ) {
                            ButtonIcon(rememberVectorPainter(Icons.Default.CorporateFare))
                            Text(stringResource(R.string.settings_about_libs_organization))
                        }
                    }
                    lib.developers.forEach { dev ->
                        OutlinedButton(
                            onClick = {
                                dev.organisationUrl?.let {
                                    if (it.contains("://")) uriHandler.openUri(it)
                                }
                            }
                        ) {
                            ButtonIcon(rememberVectorPainter(Icons.Default.Engineering))
                            Text(dev.name ?: "Developer")
                        }
                    }
                }
            }

            lib.licenses.forEach { license ->
                FormSection(
                    title = license.name,
                    topDivider = true,
                    bottomDivider = false
                ) {
                    Text(
                        text = license.licenseContent ?: "",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}