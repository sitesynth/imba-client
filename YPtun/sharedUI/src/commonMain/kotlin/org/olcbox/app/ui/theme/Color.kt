package org.olcbox.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// IMBA cabinet palette (src/app/globals.css of the site): cream canvas, white cards, ink text and borders,
// yellow / blue / green / violet / orange accents.
internal val ImbaCream = Color(0xFFF3F0E9)
internal val ImbaInk = Color(0xFF111111)
internal val ImbaPaper = Color(0xFFFFFFFF)
internal val ImbaBlue = Color(0xFF2E7DF6)
internal val ImbaBlue100 = Color(0xFFDCEEFF)
internal val ImbaViolet = Color(0xFFC9A4FF)
internal val ImbaViolet100 = Color(0xFFE9CCFF)
internal val ImbaGreen = Color(0xFF55DB9C)
internal val ImbaGreen100 = Color(0xFFCFF6E4)
internal val ImbaYellow = Color(0xFFFFD731)
internal val ImbaYellow100 = Color(0xFFFFF0B8)
internal val ImbaOrange = Color(0xFFFF7A3D)

internal val OlcboxLightColorScheme = lightColorScheme(
    primary = ImbaInk,
    onPrimary = ImbaPaper,
    primaryContainer = ImbaYellow,
    onPrimaryContainer = ImbaInk,
    inversePrimary = ImbaYellow,
    secondary = ImbaBlue,
    onSecondary = ImbaPaper,
    secondaryContainer = ImbaBlue100,
    onSecondaryContainer = ImbaInk,
    tertiary = ImbaGreen,
    onTertiary = ImbaInk,
    tertiaryContainer = ImbaGreen100,
    onTertiaryContainer = ImbaInk,
    background = ImbaCream,
    onBackground = ImbaInk,
    surface = ImbaCream,
    onSurface = ImbaInk,
    surfaceVariant = Color(0xFFEBE6DA),
    onSurfaceVariant = Color(0xFF5B5B5B),
    surfaceContainerLowest = ImbaPaper,
    surfaceContainerLow = ImbaPaper,
    surfaceContainer = ImbaPaper,
    surfaceContainerHigh = ImbaCream,
    surfaceContainerHighest = Color(0xFFEBE6DA),
    inverseSurface = ImbaInk,
    inverseOnSurface = ImbaPaper,
    outline = ImbaInk,
    outlineVariant = Color(0xFFDADADA),
    error = Color(0xFFE5484D),
    onError = ImbaPaper,
    errorContainer = Color(0xFFFFD9D2),
    onErrorContainer = Color(0xFF410002)
)

// Dark variant: ink canvas, paper text, yellow accent (same neo-brutalist shapes).
internal val OlcboxDarkColorScheme = darkColorScheme(
    primary = ImbaYellow,
    onPrimary = ImbaInk,
    primaryContainer = Color(0xFF3A3210),
    onPrimaryContainer = ImbaYellow100,
    inversePrimary = ImbaInk,
    secondary = Color(0xFF7FB0FF),
    onSecondary = ImbaInk,
    secondaryContainer = Color(0xFF1B3358),
    onSecondaryContainer = ImbaBlue100,
    tertiary = ImbaGreen,
    onTertiary = ImbaInk,
    tertiaryContainer = Color(0xFF16402F),
    onTertiaryContainer = ImbaGreen100,
    background = ImbaInk,
    onBackground = ImbaCream,
    surface = ImbaInk,
    onSurface = ImbaCream,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB5B1A6),
    surfaceContainerLowest = Color(0xFF0B0B0B),
    surfaceContainerLow = Color(0xFF161616),
    surfaceContainer = Color(0xFF1C1C1C),
    surfaceContainerHigh = Color(0xFF262626),
    surfaceContainerHighest = Color(0xFF303030),
    inverseSurface = ImbaCream,
    inverseOnSurface = ImbaInk,
    outline = ImbaCream,
    outlineVariant = Color(0xFF3C3C3C),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF410002),
    errorContainer = Color(0xFF5A1A17),
    onErrorContainer = Color(0xFFFFDAD6)
)
