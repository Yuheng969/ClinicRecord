package com.example.clinicrecord.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ColorDarkSurface = Color(0xFF2E3440)
private val ColorDarkLayer = Color(0xFF363D4A)
private val ColorDarkLayerHigh = Color(0xFF414958)

enum class AppColorStyle(
    val title: String,
    val description: String,
    val seed: Color
) {
    Sage("雾绿", "清润、安静", Color(0xFF8BAFA5)),
    Apricot("暖杏", "温和、亲近", Color(0xFFD9A481)),
    Arctic("雪蓝", "清冷、克制", Color(0xFF8498B8)),
    Lavender("薰衣草", "柔和、轻盈", Color(0xFF9C8BBC)),
    Rose("胭粉", "温柔、细腻", Color(0xFFD78A96))
}

private fun clinicLightColorScheme(style: AppColorStyle) = when (style) {
    AppColorStyle.Sage -> clinicColorScheme(
        primary = Color(0xFF4F7F73),
        primaryContainer = Color(0xFFD6E8E1),
        background = Color(0xFFEAF2EE),
        surface = Color(0xFFFFFCF7),
        surfaceLayer = Color(0xFFF4EFE6),
        surfaceLayerHigh = Color(0xFFE1EEE8)
    )
    AppColorStyle.Apricot -> clinicColorScheme(
        primary = Color(0xFF8D5C42),
        primaryContainer = Color(0xFFF4D8C7),
        background = Color(0xFFF5DFCF),
        surface = Color(0xFFFFF6EC),
        surfaceLayer = Color(0xFFF3E3D7),
        surfaceLayerHigh = Color(0xFFE7C2AA)
    )
    AppColorStyle.Arctic -> clinicColorScheme(
        primary = Color(0xFF3F567A),
        primaryContainer = Color(0xFFDCE6F6),
        background = Color(0xFFE7EDF7),
        surface = Color(0xFFF8FAFE),
        surfaceLayer = Color(0xFFE9EEF7),
        surfaceLayerHigh = Color(0xFFD4DEEC)
    )
    AppColorStyle.Lavender -> clinicColorScheme(
        primary = Color(0xFF68568D),
        primaryContainer = Color(0xFFE4D9F2),
        background = Color(0xFFEDE5F4),
        surface = Color(0xFFFFFAF5),
        surfaceLayer = Color(0xFFF1E9F4),
        surfaceLayerHigh = Color(0xFFD9CCE8)
    )
    AppColorStyle.Rose -> clinicColorScheme(
        primary = Color(0xFF7F3B45),
        primaryContainer = Color(0xFFF2D0D5),
        background = Color(0xFFF4D8DD),
        surface = Color(0xFFFFF6F3),
        surfaceLayer = Color(0xFFF4E2E1),
        surfaceLayerHigh = Color(0xFFEAC0C6)
    )
}

private fun clinicColorScheme(
    primary: Color,
    primaryContainer: Color,
    background: Color,
    surface: Color,
    surfaceLayer: Color,
    surfaceLayerHigh: Color
) = lightColorScheme(
    primary = primary,
    onPrimary = WarmWhite,
    primaryContainer = primaryContainer,
    onPrimaryContainer = Ink900,
    secondary = primary,
    onSecondary = WarmWhite,
    secondaryContainer = surfaceLayerHigh,
    onSecondaryContainer = Ink900,
    tertiary = Ink700,
    onTertiary = WarmWhite,
    background = background,
    onBackground = Ink900,
    surface = surface,
    onSurface = Ink900,
    surfaceVariant = surfaceLayer,
    onSurfaceVariant = Ink700,
    surfaceContainerLowest = WarmWhite,
    surfaceContainerLow = surface,
    surfaceContainer = surfaceLayer,
    surfaceContainerHigh = surfaceLayerHigh,
    surfaceContainerHighest = surfaceLayerHigh,
    outline = Ink500,
    outlineVariant = surfaceLayerHigh,
    error = SoftError,
    onError = WarmWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = PostalBlue300,
    onPrimary = Ink900,
    primaryContainer = PostalBlue700,
    onPrimaryContainer = WarmWhite,
    secondary = PostalBlue300,
    onSecondary = Ink900,
    secondaryContainer = Ink700,
    onSecondaryContainer = WarmWhite,
    background = Ink900,
    onBackground = Porcelain100,
    surface = ColorDarkSurface,
    onSurface = Porcelain100,
    surfaceVariant = Ink700,
    onSurfaceVariant = Porcelain300,
    surfaceContainerLowest = Ink900,
    surfaceContainerLow = ColorDarkSurface,
    surfaceContainer = ColorDarkLayer,
    surfaceContainerHigh = ColorDarkLayerHigh,
    surfaceContainerHighest = ColorDarkLayerHigh,
    outline = Porcelain300,
    outlineVariant = Ink700,
    error = SoftError,
    onError = WarmWhite
)

private val ClinicShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun ClinicRecordTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    colorStyle: AppColorStyle = AppColorStyle.Sage,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else clinicLightColorScheme(colorStyle)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ClinicShapes,
        content = content
    )
}
