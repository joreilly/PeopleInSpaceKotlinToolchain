package dev.johnoreilly.peopleinspace.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Light default theme color scheme - Space themed */
val LightDefaultColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Purple40,
    onSecondary = Color.White,
    secondaryContainer = Purple90,
    onSecondaryContainer = Purple10,
    tertiary = Orange40,
    onTertiary = Color.White,
    tertiaryContainer = Orange90,
    onTertiaryContainer = Orange10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = DarkPurpleGray99,
    onBackground = DarkPurpleGray10,
    surface = DarkPurpleGray99,
    onSurface = DarkPurpleGray10,
    surfaceVariant = PurpleGray90,
    onSurfaceVariant = PurpleGray30,
    outline = PurpleGray50,
    surfaceTint = Blue40,
)

/** Dark default theme color scheme - Space themed */
val DarkDefaultColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Purple80,
    onSecondary = Purple20,
    secondaryContainer = Purple30,
    onSecondaryContainer = Purple90,
    tertiary = Orange80,
    onTertiary = Orange20,
    tertiaryContainer = Orange30,
    onTertiaryContainer = Orange90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = DarkPurpleGray10,
    onBackground = DarkPurpleGray90,
    surface = DarkPurpleGray10,
    onSurface = DarkPurpleGray90,
    surfaceVariant = PurpleGray30,
    onSurfaceVariant = PurpleGray80,
    outline = PurpleGray60,
    surfaceTint = Blue80,
)

/** Android 12+ dynamic (wallpaper) colours, as in the original app; null on other platforms. */
@Composable
internal expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?

@Composable
fun PeopleInSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = dynamicColorScheme(darkTheme)
        ?: if (darkTheme) DarkDefaultColorScheme else LightDefaultColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PeopleInSpaceTypography,
        content = content,
    )
}
