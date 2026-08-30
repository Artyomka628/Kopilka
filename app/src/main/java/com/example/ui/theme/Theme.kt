package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantLavender,
    secondary = ElegantTextSecondary,
    tertiary = ElegantLavender,
    background = ElegantDarkBg,
    surface = ElegantCardBg,
    onBackground = ElegantTextPrimary,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantHeaderBg
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ElegantLavender,
    secondary = ElegantTextSecondary,
    tertiary = ElegantLavender,
    background = ElegantDarkBg, // Force elegant dark background
    surface = ElegantCardBg,
    onBackground = ElegantTextPrimary,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantHeaderBg
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for dark blue branding
  // Dynamic color is disabled by default to preserve custom branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
