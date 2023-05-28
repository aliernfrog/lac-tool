package com.aliernfrog.lactool.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val supportsMaterialYou = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
fun LACToolTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColors: Boolean = true, content: @Composable () -> Unit) {
    val useDynamicColors = supportsMaterialYou && dynamicColors
    val colors = when {
        useDynamicColors && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        useDynamicColors && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val view = LocalView.current
    val systemUiController = rememberSystemUiController()
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        systemUiController.systemBarsDarkContentEnabled = !darkTheme
        systemUiController.isNavigationBarContrastEnforced = false
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

enum class Theme(val int: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2)
}