package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LanzarusPrimaryTeal,
    secondary = LanzarusSecondaryCyan,
    tertiary = LanzarusAccentGold,
    background = LanzarusDarkBg,
    surface = LanzarusCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LanzarusTextPrimary,
    onSurface = LanzarusTextPrimary,
    error = LanzarusErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = LanzarusPrimaryTeal,
    secondary = LanzarusSecondaryCyan,
    tertiary = LanzarusAccentGold,
    background = LanzarusDarkBg,
    surface = LanzarusCardBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LanzarusTextPrimary,
    onSurface = LanzarusTextPrimary,
    error = LanzarusErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set default to false to render the high-end light professional look by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
