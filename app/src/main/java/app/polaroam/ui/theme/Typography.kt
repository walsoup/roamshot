package app.polaroam.ui.theme

import android.content.res.AssetManager
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.sp

object PixelFonts {
    /** Checked in order; drop any of these into app/src/main/assets/fonts/. */
    val candidates = listOf(
        "fonts/PixelifySans.ttf",
        "fonts/PixelifySans-Regular.ttf",
        "fonts/Silkscreen-Regular.ttf",
    )

    fun load(assets: AssetManager): FontFamily {
        for (path in candidates) {
            val typeface = runCatching { android.graphics.Typeface.createFromAsset(assets, path) }.getOrNull()
            if (typeface != null) return FontFamily(Typeface(typeface))
        }
        return FontFamily.Monospace
    }
}

/** Sizes are multiples of 8sp so pixel fonts stay crisp. */
@Immutable
data class PolaroamTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val button: TextStyle,
    val caption: TextStyle,
) {
    fun toMaterial(): Typography = Typography(
        displayLarge = display,
        displayMedium = display,
        headlineLarge = title,
        headlineMedium = title,
        titleLarge = title,
        titleMedium = body,
        bodyLarge = body,
        bodyMedium = body,
        bodySmall = caption,
        labelLarge = button,
        labelMedium = label,
        labelSmall = caption,
    )

    companion object {
        fun from(family: FontFamily) = PolaroamTypography(
            display = TextStyle(fontFamily = family, fontSize = 32.sp, lineHeight = 40.sp),
            title = TextStyle(fontFamily = family, fontSize = 24.sp, lineHeight = 32.sp),
            body = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 24.sp),
            label = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 20.sp),
            button = TextStyle(fontFamily = family, fontSize = 16.sp, lineHeight = 20.sp),
            caption = TextStyle(fontFamily = family, fontSize = 12.sp, lineHeight = 16.sp),
        )
    }
}
