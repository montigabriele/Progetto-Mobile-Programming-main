package com.mastermind.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = AccentCyan,
    onTertiary = Color(0xFF061B26),
    background = AppBackgroundDark,
    onBackground = AppTextPrimaryDark,
    surface = AppSurfaceDark,
    onSurface = AppTextPrimaryDark,
    surfaceVariant = AppSurfaceVariantDark,
    onSurfaceVariant = AppTextSecondaryDark,
    outline = AppOutlineDark,
    outlineVariant = AppOutlineVariantDark,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF330000),
    errorContainer = Color(0xFF5F1B1B),
    onErrorContainer = Color(0xFFFFDADA)
)

val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = AccentCyan,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = AccentTeal,
    onTertiary = Color(0xFF042F2E),
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppTextSecondary,
    outline = AppOutline,
    outlineVariant = AppOutlineVariant,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun MastermindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
