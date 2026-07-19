package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandWhite,
    onPrimary = BrandBlack,
    secondary = BrandLightGrey,
    onSecondary = BrandBlack,
    tertiary = BrandAccentRed,
    onTertiary = BrandWhite,
    background = DarkGray90,
    onBackground = BrandWhite,
    surface = DarkGray80,
    onSurface = BrandWhite,
    outline = DarkGray80
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlack,
    onPrimary = BrandWhite,
    secondary = BrandDarkNavy,
    onSecondary = BrandWhite,
    tertiary = BrandAccentRed,
    onTertiary = BrandWhite,
    background = BrandLightGrey,
    onBackground = BrandBlack,
    surface = BrandWhite,
    onSurface = BrandBlack,
    outline = BrandBorderGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
