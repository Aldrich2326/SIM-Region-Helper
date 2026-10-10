// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Shared semantic tokens keep main, permission and attribution screens consistent.
private val LightColors = lightColorScheme(
    primary = Color(0xFF006A63), onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F2E9), onPrimaryContainer = Color(0xFF004C46),
    secondary = Color(0xFF49665F), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1EDE7), onSecondaryContainer = Color(0xFF2C4942),
    tertiary = Color(0xFF556581), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE6FA), onTertiaryContainer = Color(0xFF34445E),
    background = Color(0xFFF3F6F3), onBackground = Color(0xFF182C28),
    surface = Color(0xFFFCFEFB), onSurface = Color(0xFF182C28),
    surfaceVariant = Color(0xFFE4ECE6), onSurfaceVariant = Color(0xFF4C6058),
    surfaceContainer = Color(0xFFEBF0EA), surfaceContainerLow = Color(0xFFF7FAF5),
    outline = Color(0xFF72847C), outlineVariant = Color(0xFFC4D2C9),
    error = Color(0xFFB3261E), onError = Color.White,
    errorContainer = Color(0xFFFCE3DF), onErrorContainer = Color(0xFF7A241E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD7C8), onPrimary = Color(0xFF003A33),
    primaryContainer = Color(0xFF164F46), onPrimaryContainer = Color(0xFFB5EDDE),
    secondary = Color(0xFFB4CCC0), onSecondary = Color(0xFF213B30),
    secondaryContainer = Color(0xFF2D493E), onSecondaryContainer = Color(0xFFD0E6D9),
    tertiary = Color(0xFFB9C9E8), onTertiary = Color(0xFF263650),
    tertiaryContainer = Color(0xFF3C4C68), onTertiaryContainer = Color(0xFFDCE6FA),
    background = Color(0xFF101815), onBackground = Color(0xFFE0EAE3),
    surface = Color(0xFF19241F), onSurface = Color(0xFFE0EAE3),
    surfaceVariant = Color(0xFF2A3931), onSurfaceVariant = Color(0xFFBECEC3),
    surfaceContainer = Color(0xFF202D25), surfaceContainerLow = Color(0xFF17211C),
    outline = Color(0xFF8C9F92), outlineVariant = Color(0xFF405348),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF65332F), onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun NrfrTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
