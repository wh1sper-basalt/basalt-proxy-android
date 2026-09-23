package com.basalt.proxy.ui

import android.os.Build
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.staticCompositionLocalOf


val AppFontFamily = FontFamily.Default
val LocalIsBasaltTheme = staticCompositionLocalOf { false }

val BasaltProxyTypography = Typography(
    displayLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

private val EspressoLightColorScheme = lightColorScheme(
    primary = Color(0xFF6D4C41), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7CCC8), onPrimaryContainer = Color(0xFF3E2723),
    secondary = Color(0xFF8D6E63), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFEBE9), onSecondaryContainer = Color(0xFF4E342E),
    tertiary = Color(0xFF795548), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCAAA4), onTertiaryContainer = Color(0xFF3E2723),
    background = Color(0xFFFFFBF7), onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFF5F0EB), onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFEFEBE9), onSurfaceVariant = Color(0xFF5D4037),
    outline = Color(0xFFBCAAA4), outlineVariant = Color(0xFFD7CCC8),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
)
private val EspressoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD7CCC8), onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFF5D4037), onPrimaryContainer = Color(0xFFEFEBE9),
    secondary = Color(0xFFBCAAA4), onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF4E342E), onSecondaryContainer = Color(0xFFEFEBE9),
    tertiary = Color(0xFFA1887F), onTertiary = Color(0xFF3E2723),
    tertiaryContainer = Color(0xFF5D4037), onTertiaryContainer = Color(0xFFEFEBE9),
    background = Color(0xFF1A1614), onBackground = Color(0xFFEDE0D4),
    surface = Color(0xFF211D1B), onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF2C2624), onSurfaceVariant = Color(0xFFD7CCC8),
    outline = Color(0xFF8D6E63), outlineVariant = Color(0xFF4E342E),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

private val IndigoLightColorScheme = lightColorScheme(
    primary = Color(0xFF5B588D), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE2DFFF), onPrimaryContainer = Color(0xFF1A1744),
    secondary = Color(0xFF5B588D), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2DFFF), onSecondaryContainer = Color(0xFF1A1744),
    background = Color(0xFFFBF8FF), onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFF6F3FA), onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE4E1EC), onSurfaceVariant = Color(0xFF47464F),
    outline = Color(0xFF787680), outlineVariant = Color(0xFFC8C5D0),
)
private val IndigoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC4C0FF), onPrimary = Color(0xFF2D2A5B),
    primaryContainer = Color(0xFF434073), onPrimaryContainer = Color(0xFFE2DFFF),
    secondary = Color(0xFFC4C0FF), onSecondary = Color(0xFF2D2A5B),
    secondaryContainer = Color(0xFF434073), onSecondaryContainer = Color(0xFFE2DFFF),
    background = Color(0xFF131316), onBackground = Color(0xFFE4E1E6),
    surface = Color(0xFF1B1B1F), onSurface = Color(0xFFC8C5D0),
    surfaceVariant = Color(0xFF47464F), onSurfaceVariant = Color(0xFFC8C5D0),
    outline = Color(0xFF918F9A), outlineVariant = Color(0xFF47464F),
)

private val ForestLightColorScheme = lightColorScheme(
    primary = Color(0xFF5F5D68), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E0F0), onPrimaryContainer = Color(0xFF1C1A23),
    secondary = Color(0xFF5F5D68), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E0F0), onSecondaryContainer = Color(0xFF1C1A23),
    background = Color(0xFFFCF8FF), onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFF7F2FA), onSurface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFFE6E0E9), onSurfaceVariant = Color(0xFF48454E),
    outline = Color(0xFF79747E), outlineVariant = Color(0xFFCAC4D0),
)
private val ForestDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC8C4D3), onPrimary = Color(0xFF312F38),
    primaryContainer = Color(0xFF474550), onPrimaryContainer = Color(0xFFE5E0F0),
    secondary = Color(0xFFC8C4D3), onSecondary = Color(0xFF312F38),
    secondaryContainer = Color(0xFF474550), onSecondaryContainer = Color(0xFFE5E0F0),
    background = Color(0xFF141318), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B20), onSurface = Color(0xFFCAC4D0),
    surfaceVariant = Color(0xFF48454E), onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99), outlineVariant = Color(0xFF48454E),
)

private val PinkLightColorScheme = lightColorScheme(
    primary = Color(0xFFB96A85), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E3), onPrimaryContainer = Color(0xFF3E0021),
    secondary = Color(0xFFB96A85), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E3), onSecondaryContainer = Color(0xFF3E0021),
    background = Color(0xFFFFF7F9), onBackground = Color(0xFF201A1C),
    surface = Color(0xFFFDF2F5), onSurface = Color(0xFF201A1C),
    surfaceVariant = Color(0xFFF3DDE3), onSurfaceVariant = Color(0xFF524347),
    outline = Color(0xFF857377), outlineVariant = Color(0xFFD7C1C6),
)
private val PinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE8B7C8), onPrimary = Color(0xFF66183A),
    primaryContainer = Color(0xFF8E3354), onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFE8B7C8), onSecondary = Color(0xFF66183A),
    secondaryContainer = Color(0xFF8E3354), onSecondaryContainer = Color(0xFFFFD9E3),
    background = Color(0xFF1A1214), onBackground = Color(0xFFEEDFE2),
    surface = Color(0xFF211719), onSurface = Color(0xFFEEDFE2),
    surfaceVariant = Color(0xFF524347), onSurfaceVariant = Color(0xFFD7C1C6),
    outline = Color(0xFFA08C91), outlineVariant = Color(0xFF524347),
)

private val MonoLightColorScheme = lightColorScheme(
    primary = Color(0xFF1A1A1A), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5E5), onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF454545), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEAEAEA), onSecondaryContainer = Color(0xFF1A1A1A),
    background = Color(0xFFFAFAFA), onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFF2F2F2), onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEAEAEA), onSurfaceVariant = Color(0xFF3D3D3D),
    outline = Color(0xFF7A7A7A), outlineVariant = Color(0xFFCFCFCF),
)
private val MonoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE5E5E5), onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF3D3D3D), onPrimaryContainer = Color(0xFFE5E5E5),
    secondary = Color(0xFFBFBFBF), onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF2E2E2E), onSecondaryContainer = Color(0xFFE5E5E5),
    background = Color(0xFF0E0E0E), onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF1A1A1A), onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF2E2E2E), onSurfaceVariant = Color(0xFFCFCFCF),
    outline = Color(0xFF7A7A7A), outlineVariant = Color(0xFF3D3D3D),
)

internal fun getAppColorScheme(palette: String, isDark: Boolean): ColorScheme {
    return when (palette) {
        "espresso" -> if (isDark) EspressoDarkColorScheme else EspressoLightColorScheme
        "forest"   -> if (isDark) ForestDarkColorScheme else ForestLightColorScheme
        "pink"     -> if (isDark) PinkDarkColorScheme else PinkLightColorScheme
        "mono"     -> if (isDark) MonoDarkColorScheme else MonoLightColorScheme
        else       -> if (isDark) IndigoDarkColorScheme else IndigoLightColorScheme
    }
}

object AppColors {
    val connected = Color(0xFF4CAF50)
    val connectedDark = Color(0xFF81C784)
    val warning = Color(0xFFFFA726)
    val terminalText = Color(0xFFE0E0E0)
    val terminalGreen = Color(0xFF4CAF50)
    val terminalBlue = Color(0xFF42A5F5)
    val terminalRed = Color(0xFFEF5350)
    val terminalOrange = Color(0xFFFF7043)
    val terminalYellow = Color(0xFFFFC107)
    val terminalCounter = Color(0xFF1E88E5)

    fun terminalBg(scheme: ColorScheme): Color {
        return lerp(scheme.surface, scheme.primary, 0.35f)
            .let { lerp(it, Color.Black, 0.45f) }
    }

    val github = Color(0xFF24292E)
    val githubDark = Color(0xFF333C47)
}

@Composable
fun BasaltProxyTheme(
    themeMode: String = "system",
    dynamicColor: Boolean = true,
    themePalette: String = "indigo",
    isBasaltTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme: ColorScheme = when {
        isBasaltTheme -> getAppColorScheme("mono", darkTheme)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getAppColorScheme(themePalette, darkTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val navigationBarColor = if (darkTheme) Color.Transparent
            else lerp(colorScheme.background, colorScheme.surface, 0.55f)
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = navigationBarColor.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                window.isNavigationBarContrastEnforced = false
                @Suppress("DEPRECATION")
                window.isStatusBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalIsBasaltTheme provides isBasaltTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BasaltProxyTypography,
            content = content
        )
    }
}