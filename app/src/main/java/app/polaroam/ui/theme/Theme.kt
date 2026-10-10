package app.polaroam.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.polaroam.ui.skin.PolaroamSkin
import app.polaroam.ui.skin.PolaroamSkins

private val DefaultSpacing = PolaroamSpacing()
private val DefaultDimens = PolaroamDimens()

private val LocalPolaroamColors = staticCompositionLocalOf { PolaroamColors.Default }
private val LocalPolaroamTypography = staticCompositionLocalOf { PolaroamTypography.from(FontFamily.Monospace) }
private val LocalPolaroamSpacing = staticCompositionLocalOf { DefaultSpacing }
private val LocalPolaroamDimens = staticCompositionLocalOf { DefaultDimens }
private val LocalPolaroamSkin = staticCompositionLocalOf {
    PolaroamSkins.flat(PolaroamColors.Default, DefaultDimens, DefaultSpacing)
}

object PolaroamTheme {
    val colors: PolaroamColors
        @Composable @ReadOnlyComposable get() = LocalPolaroamColors.current
    val typography: PolaroamTypography
        @Composable @ReadOnlyComposable get() = LocalPolaroamTypography.current
    val spacing: PolaroamSpacing
        @Composable @ReadOnlyComposable get() = LocalPolaroamSpacing.current
    val dimens: PolaroamDimens
        @Composable @ReadOnlyComposable get() = LocalPolaroamDimens.current
    val skin: PolaroamSkin
        @Composable @ReadOnlyComposable get() = LocalPolaroamSkin.current
}

/**
 * @param skin pass a PNG-backed skin here once the pixel art lands; defaults to flat colors.
 */
@Composable
fun PolaroamTheme(
    skin: PolaroamSkin? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = PolaroamColors.Default
    val fontFamily = remember(context) { PixelFonts.load(context.assets) }
    val typography = remember(fontFamily) { PolaroamTypography.from(fontFamily) }
    val resolvedSkin = skin ?: remember(colors) { PolaroamSkins.flat(colors, DefaultDimens, DefaultSpacing) }

    val square = RoundedCornerShape(0.dp)
    val colorScheme = darkColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        secondary = colors.info,
        onSecondary = colors.onPaper,
        tertiary = colors.success,
        background = colors.background,
        onBackground = colors.onBackground,
        surface = colors.surface,
        onSurface = colors.onSurface,
        surfaceVariant = colors.parchment,
        onSurfaceVariant = colors.onPaper,
        error = colors.danger,
        onError = colors.onDanger,
        outline = colors.border,
    )

    CompositionLocalProvider(
        LocalPolaroamColors provides colors,
        LocalPolaroamTypography provides typography,
        LocalPolaroamSpacing provides DefaultSpacing,
        LocalPolaroamDimens provides DefaultDimens,
        LocalPolaroamSkin provides resolvedSkin,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography.toMaterial(),
            shapes = Shapes(
                extraSmall = square,
                small = square,
                medium = square,
                large = square,
                extraLarge = square,
            ),
            content = content,
        )
    }
}
