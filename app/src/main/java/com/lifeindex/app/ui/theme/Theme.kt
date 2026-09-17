package com.lifeindex.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = LifeIndexPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,

    secondary = LifeIndexPrimary,
    onSecondary = androidx.compose.ui.graphics.Color.White,

    background = LifeIndexBackground,
    onBackground = LifeIndexText,

    surface = LifeIndexSurface,
    onSurface = LifeIndexText,

    surfaceVariant = LifeIndexSurfaceSoft,
    onSurfaceVariant = LifeIndexTextSecondary,

    error = LifeIndexError,
    onError = androidx.compose.ui.graphics.Color.White
)

private val DarkColors = darkColorScheme(
    primary = LifeIndexPrimaryDark,
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1D2750),

    secondary = LifeIndexPrimaryDark,
    onSecondary = androidx.compose.ui.graphics.Color(0xFF1D2750),

    background = androidx.compose.ui.graphics.Color(0xFF111216),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE8E8ED),

    surface = androidx.compose.ui.graphics.Color(0xFF191A20),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE8E8ED),

    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF292B34),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFBFC1CB),

    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    onError = androidx.compose.ui.graphics.Color(0xFF690005)
)

private val LifeIndexTypography = Typography()

@Composable
fun LifeIndexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeIndexTypography,
        content = content
    )
}