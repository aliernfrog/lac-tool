package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.ui.theme.supportsMaterialYou
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import io.github.aliernfrog.pftool_shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveSwitchRow
import io.github.aliernfrog.pftool_shared.ui.component.expressive.toRowFriendlyColor
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearancePage(
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    SettingsPageContainer(
        title = stringResource(R.string.settings_appearance),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        ExpressiveSection(
            title = stringResource(R.string.settings_appearance_theme)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                Theme.entries.forEachIndexed { index, theme ->
                    val isLightThemeItem = theme == Theme.LIGHT
                    val selected = settingsViewModel.prefs.theme.value == theme.ordinal
                    val onSelect = { settingsViewModel.prefs.theme.value = theme.ordinal }
                    val weight by animateFloatAsState(if (selected) 1.1f else 1f)

                    val iconRotation = remember { Animatable(
                        if (isLightThemeItem && selected) 90f else 0f
                    ) }

                    LaunchedEffect(selected) {
                        if (isLightThemeItem) iconRotation.animateTo(
                            targetValue = if (selected) 90f else 0f,
                            animationSpec = tween(durationMillis = 800, easing = EaseInOut)
                        )
                    }

                    ToggleButton(
                        checked = selected,
                        onCheckedChange = { onSelect() },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            Theme.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        modifier = Modifier
                            .weight(weight)
                            .animateContentSize()
                            .semantics { Role.RadioButton }
                    ) {
                        Box {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp)
                            ) {
                                @Composable
                                fun ThemeIcon(useFilled: Boolean) {
                                    Icon(
                                        imageVector = if (useFilled) theme.filledIcon else theme.outlinedIcon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .graphicsLayer {
                                                rotationZ = iconRotation.value
                                            }
                                    )
                                }

                                if (isLightThemeItem) ThemeIcon(useFilled = selected)
                                else AnimatedContent(
                                    targetState = selected
                                ) { useFilled ->
                                    ThemeIcon(useFilled = useFilled)
                                }

                                Text(
                                    text = stringResource(theme.label),
                                    style = MaterialTheme.typography.labelLarge
                                )

                                RadioButton(
                                    selected = selected,
                                    onClick = { onSelect() },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = LocalContentColor.current,
                                        unselectedColor = LocalContentColor.current
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        ExpressiveSection(
            title = stringResource(R.string.settings_appearance_colors)
        ) {
            VerticalSegmentor(
                {
                    ExpressiveSwitchRow(
                        title = stringResource(R.string.settings_appearance_materialYou),
                        description = stringResource(
                            if (supportsMaterialYou) R.string.settings_appearance_materialYou_description
                            else R.string.settings_appearance_materialYou_unavailable
                        ),
                        icon = {
                            ExpressiveRowIcon(
                                painter = rememberVectorPainter(Icons.Rounded.Brush),
                                containerColor = Color.Yellow.toRowFriendlyColor
                            )
                        },
                        checked = settingsViewModel.prefs.materialYou.value,
                        enabled = supportsMaterialYou
                    ) {
                        settingsViewModel.prefs.materialYou.value = it
                    }
                },
                {
                    ExpressiveSwitchRow(
                        title = stringResource(R.string.settings_appearance_pitchBlack),
                        description = stringResource(R.string.settings_appearance_pitchBlack_description),
                        icon = {
                            ExpressiveRowIcon(
                                painter = rememberVectorPainter(Icons.Rounded.Contrast),
                                containerColor = Color.Black.toRowFriendlyColor
                            )
                        },
                        checked = settingsViewModel.prefs.pitchBlack.value
                    ) {
                        settingsViewModel.prefs.pitchBlack.value = it
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}