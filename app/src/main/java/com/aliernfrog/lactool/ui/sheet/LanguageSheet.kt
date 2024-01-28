package com.aliernfrog.lactool.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.crowdinURL
import com.aliernfrog.lactool.data.Language
import com.aliernfrog.lactool.languages
import com.aliernfrog.lactool.ui.component.BaseModalBottomSheet
import com.aliernfrog.lactool.ui.component.SmallDragHandle
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.extension.getAvailableLanguage
import com.aliernfrog.lactool.util.extension.getNameIn
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSheet(
    mainViewModel: MainViewModel = koinViewModel(),
    sheetState: SheetState
) {
    val currentLanguage = mainViewModel.prefs.language
    val availableDeviceLanguage = mainViewModel.deviceLanguage.getAvailableLanguage()

    @Composable
    fun LanguageButton(
        language: Language? = null,
        title: String = language?.localizedName.toString(),
        description: String = language?.fullCode.toString(),
        painter: Painter = rememberVectorPainter(Icons.Default.Translate),
        selected: Boolean = language?.fullCode == currentLanguage,
        onClick: () -> Unit = {
            mainViewModel.appLanguage = language
        }
    ) {
        ButtonRow(
            title = title,
            description = description,
            painter = painter,
            trailingComponent = if (selected) { {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.CheckCircle),
                    contentDescription = stringResource(R.string.settings_general_language_selected)
                )
            } } else null,
            onClick = onClick
        )
    }

    BaseModalBottomSheet(
        sheetState = sheetState,
        dragHandle = { SmallDragHandle() }
    ) { bottomPadding ->
        Text(
            text = stringResource(R.string.settings_general_language_select),
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
        )
        DividerRow(
            alpha = 0.3f
        )
        LazyColumn {
            item {
                TranslationHelp(isDeviceLanguageAvailable = availableDeviceLanguage != null)
            }

            item {
                LanguageButton(
                    title = stringResource(R.string.settings_general_language_system),
                    description = availableDeviceLanguage?.localizedName ?: stringResource(R.string.settings_general_language_system_notAvailable)
                        .replace("{SYSTEM_LANGUAGE}", mainViewModel.appLanguage?.let {
                            mainViewModel.deviceLanguage.getNameIn(it.languageCode, it.countryCode)
                        } ?: ""),
                    painter = rememberVectorPainter(Icons.Default.PhoneAndroid),
                    selected = currentLanguage.isBlank(),
                    onClick = {
                        mainViewModel.appLanguage = null
                    }
                )
                DividerRow()
            }

            items(languages) {
                LanguageButton(language = it)
            }

            item {
                Spacer(Modifier.height(bottomPadding))
            }
        }
    }
}

@Composable
fun TranslationHelp(
    isDeviceLanguageAvailable: Boolean
) {
    val uriHandler = LocalUriHandler.current
    Card(
        shape = AppComponentShape,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        onClick = { uriHandler.openUri(crowdinURL) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Default.Handshake),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(
                        if (isDeviceLanguageAvailable) R.string.settings_general_language_help
                        else R.string.settings_general_language_help_deviceNotAvailable
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = stringResource(R.string.settings_general_language_help_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}