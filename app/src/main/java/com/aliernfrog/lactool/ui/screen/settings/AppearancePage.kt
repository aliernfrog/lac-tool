package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.VerticalSegmentor
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowIcon
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSection
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSwitchRow
import com.aliernfrog.lactool.ui.component.expressive.toRowFriendlyColor
import com.aliernfrog.lactool.ui.theme.AppRoundnessSize
import com.aliernfrog.lactool.ui.theme.Theme
import com.aliernfrog.lactool.ui.theme.supportsMaterialYou
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Theme.entries.forEach { theme ->
                    val selected = settingsViewModel.prefs.theme.value == theme.ordinal
                    val isLightThemeItem = theme == Theme.LIGHT
                    val containerColor by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    val contentColor by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    val roundness by animateDpAsState(
                        if (selected) AppRoundnessSize else AppRoundnessSize+16.dp
                    )

                    val rotation = remember { Animatable(
                        if (isLightThemeItem && selected) 90f else 0f
                    ) }

                    LaunchedEffect(selected) {
                        if (isLightThemeItem) rotation.animateTo(
                            targetValue = if (selected) 90f else 0f,
                            animationSpec = tween(durationMillis = 800, easing = EaseInOut)
                        )
                    }

                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        Box(
                            Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(roundness))
                                    .background(containerColor)
                                    .clickable {
                                        settingsViewModel.prefs.theme.value = theme.ordinal
                                    }
                                    .padding(vertical = 32.dp)
                            ) {
                                @Composable
                                fun ThemeIcon(useFilled: Boolean) {
                                    Icon(
                                        imageVector = if (useFilled) theme.filledIcon else theme.outlinedIcon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .graphicsLayer {
                                                rotationZ = rotation.value
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
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = selected,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonChecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
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