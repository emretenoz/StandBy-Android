package com.example.standby.ui.standby

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.standby.data.settings.BackgroundStyle
import com.example.standby.data.settings.ClockFace
import com.example.standby.data.settings.StandBySettings
import com.example.standby.data.settings.StandByThemeId
import com.example.standby.data.settings.TypographyStyle

data class StandByThemeTokens(
    val id: StandByThemeId,
    val background: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val clockPrimary: Color,
    val clockSecondary: Color,
    val analogMarkers: Color,
    val analogSecondHand: Color,
    val separator: Color,
    val icon: Color,
    val isLight: Boolean = false,
)

data class StandByTypography(
    val style: TypographyStyle,
    val family: FontFamily,
    val weight: FontWeight,
    val letterSpacing: TextUnit,
    val horizontalScale: Float = 1f,
)

val LocalStandByTheme = staticCompositionLocalOf { classicTokens() }

fun resolveStandByTheme(
    settings: StandBySettings,
    dayThemeOverride: StandByThemeId? = null,
    nightThemeOverride: StandByThemeId? = null,
): StandByThemeTokens {
    val requested = if (settings.nightMode) {
        nightThemeOverride ?: settings.nightTheme
    } else {
        dayThemeOverride ?: settings.dayTheme
    }
    val safeTheme = if (settings.nightMode && requested == StandByThemeId.MONO_LIGHT) {
        StandByThemeId.CRIMSON_NIGHT
    } else {
        requested
    }
    val base = when (safeTheme) {
        StandByThemeId.CLASSIC -> classicTokens()
        StandByThemeId.MIDNIGHT -> StandByThemeTokens(
            safeTheme, Color(0xFF02050B), Color(0xFFEAF5FF), Color(0xFF7894AE),
            Color(0xFF5AC8FA), Color(0xFFE6F4FF), Color(0xFF82D8FF),
            Color(0xFF6B879F), Color(0xFF5AC8FA), Color(0xFF172536), Color(0xFF8FDCF9),
        )
        StandByThemeId.CRIMSON_NIGHT -> StandByThemeTokens(
            safeTheme, Color.Black, Color(0xFF9A2428), Color(0xFF5C2428),
            Color(0xFFC43C42), Color(0xFF8E1F24), Color(0xFF651C20),
            Color(0xFF5D2527), Color(0xFFD04A50), Color(0xFF251012), Color(0xFFA82E34),
        )
        StandByThemeId.SUNSET -> StandByThemeTokens(
            safeTheme, Color(0xFF100906), Color(0xFFFFD7A3), Color(0xFFAA7050),
            Color(0xFFFF9F43), Color(0xFFFFC477), Color(0xFFD97843),
            Color(0xFFB7774D), Color(0xFFE86645), Color(0xFF2B1710), Color(0xFFFFA64D),
        )
        StandByThemeId.FOREST -> StandByThemeTokens(
            safeTheme, Color(0xFF000503), Color(0xFFE8E4D8), Color(0xFF819C88),
            Color(0xFF3BA66B), Color(0xFFE8E4D8), Color(0xFFA1B7A3),
            Color(0xFF78977F), Color(0xFF40BD78), Color(0xFF122219), Color(0xFF84B993),
        )
        StandByThemeId.NEON -> StandByThemeTokens(
            safeTheme, Color.Black, Color(0xFFEAFBFF), Color(0xFF8C78AF),
            Color(0xFF4DDBE8), Color(0xFFBDF8FF), Color(0xFF9C78D2),
            Color(0xFF507A82), Color(0xFFB06CFF), Color(0xFF141728), Color(0xFF53DCE8),
        )
        StandByThemeId.MONO_LIGHT -> StandByThemeTokens(
            safeTheme, Color(0xFFF0F0EC), Color(0xFF111111), Color(0xFF656561),
            Color(0xFF343434), Color(0xFF0A0A0A), Color(0xFF4A4A47),
            Color(0xFF5B5B57), Color(0xFF111111), Color(0xFFD2D2CE), Color(0xFF242424), true,
        )
        StandByThemeId.CUSTOM -> StandByThemeTokens(
            safeTheme,
            Color(settings.customBackground),
            Color(settings.customPrimary),
            Color(settings.customSecondary),
            Color(settings.customAccent),
            Color(settings.customPrimary),
            Color(settings.customSecondary),
            Color(settings.customSecondary),
            Color(settings.customAccent),
            Color(settings.customSecondary).copy(alpha = 0.22f),
            Color(settings.customAccent),
        )
    }
    return if (settings.clockColor.argb != 0L) {
        base.copy(clockPrimary = Color(settings.clockColor.argb))
    } else {
        base
    }
}

private fun classicTokens() = StandByThemeTokens(
    id = StandByThemeId.CLASSIC,
    background = Color.Black,
    primary = Color(0xFFF1EDE3),
    secondary = Color(0xFF9D9990),
    accent = Color(0xFFF1EDE3),
    clockPrimary = Color(0xFFF1EDE3),
    clockSecondary = Color(0xFFAAA69D),
    analogMarkers = Color(0xFF8B877F),
    analogSecondHand = Color(0xFFFF9F0A),
    separator = Color(0xFF1A1917),
    icon = Color(0xFFEAE6DC),
)

fun resolveTypography(settings: StandBySettings, face: ClockFace): StandByTypography {
    val themeId = resolveStandByTheme(settings).id
    val selected = if (settings.typographyStyle != TypographyStyle.AUTO) {
        settings.typographyStyle
    } else {
        when {
            face == ClockFace.WORLD -> TypographyStyle.MONO
            face == ClockFace.DIGITAL_SPLIT && themeId == StandByThemeId.SUNSET -> TypographyStyle.ROUNDED
            face == ClockFace.DIGITAL_SPLIT -> TypographyStyle.CONDENSED
            face == ClockFace.DIGITAL && themeId == StandByThemeId.CLASSIC -> TypographyStyle.EDITORIAL
            themeId == StandByThemeId.NEON -> TypographyStyle.MONO
            else -> TypographyStyle.MODERN
        }
    }
    return when (selected) {
        TypographyStyle.AUTO, TypographyStyle.MODERN -> StandByTypography(
            selected, FontFamily.SansSerif, FontWeight.Thin, (-1).sp,
        )
        TypographyStyle.EDITORIAL -> StandByTypography(
            selected, FontFamily.Serif, FontWeight.Light, (-1.5).sp,
        )
        TypographyStyle.ROUNDED -> StandByTypography(
            selected, FontFamily.SansSerif, FontWeight.Normal, 0.sp, 0.98f,
        )
        TypographyStyle.CONDENSED -> StandByTypography(
            selected, FontFamily.SansSerif, FontWeight.Light, (-2).sp, 0.84f,
        )
        TypographyStyle.MONO -> StandByTypography(
            selected, FontFamily.Monospace, FontWeight.Light, (-2).sp, 0.94f,
        )
    }
}

@Composable
fun StandByThemeProvider(
    settings: StandBySettings,
    dayThemeOverride: StandByThemeId? = null,
    nightThemeOverride: StandByThemeId? = null,
    content: @Composable () -> Unit,
) {
    val tokens = resolveStandByTheme(settings, dayThemeOverride, nightThemeOverride)
    CompositionLocalProvider(LocalStandByTheme provides tokens) {
        content()
    }
}

@Composable
fun ThemedBackground(
    settings: StandBySettings,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val theme = LocalStandByTheme.current
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(theme.background)
            when (settings.backgroundStyle) {
                BackgroundStyle.SOLID -> Unit
                BackgroundStyle.RADIAL -> drawRect(
                    Brush.radialGradient(
                        listOf(theme.primary.copy(alpha = 0.07f), Color.Transparent),
                        center = center,
                        radius = size.maxDimension * 0.7f,
                    )
                )
                BackgroundStyle.VIGNETTE -> drawRect(
                    Brush.radialGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)),
                        center = center,
                        radius = size.maxDimension * 0.66f,
                    )
                )
                BackgroundStyle.AURA -> drawRect(
                    Brush.radialGradient(
                        listOf(theme.accent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.28f, size.height * 0.3f),
                        radius = size.maxDimension * 0.62f,
                    )
                )
                BackgroundStyle.GRAIN -> drawGrain(theme)
            }
        }
        content()
    }
}

private fun DrawScope.drawGrain(theme: StandByThemeTokens) {
    val step = 18f
    var y = 5f
    var row = 0
    while (y < size.height) {
        var x = if (row % 2 == 0) 7f else 13f
        while (x < size.width) {
            drawCircle(theme.primary.copy(alpha = 0.022f), 0.7f, Offset(x, y))
            x += step
        }
        y += step
        row++
    }
}
