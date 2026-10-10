package com.fexed.playground.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Theme tokens centralize color choices so Material components can share consistent light styling.
private val LightColors = lightColorScheme(
    primary = Color(0xFF315C4A),
    onPrimary = Color.White,
    secondary = Color(0xFF52645A),
    background = Color(0xFFF7F8F5),
    surface = Color(0xFFF7F8F5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3C5AA),
    secondary = Color(0xFFB8CCBD),
)

/**
 * Applies the app's Material 3 design tokens to a subtree.
 *
 * System appearance is the default, but callers can override it (for example, in previews). Theme
 * choice is kept in presentation rather than mixed into feature or persistence state.
 */
// Allows this function to emit MaterialTheme into the Compose UI tree.
@Composable
fun PlaygroundTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // The content parameter is itself a Compose UI lambda and can only be invoked by composition.
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
