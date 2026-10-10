package app.polaroam.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Raw palette. Screens should use [PolaroamColors] roles, not these directly. */
object PolaroamPalette {
    val InkNavy = Color(0xFF1D1A2F)
    val NightBlue = Color(0xFF2B3A67)
    val CorkBrown = Color(0xFFA8743F)
    val DarkBark = Color(0xFF5B3A29)
    val Amber = Color(0xFFE8A33D)
    val Gold = Color(0xFFF2C94C)
    val Cream = Color(0xFFF4E6C3)
    val Parchment = Color(0xFFD9C295)
    val BrickRed = Color(0xFFC4452F)
    val MossGreen = Color(0xFF6B9A3F)
    val PineGreen = Color(0xFF2F5D3A)
    val SkyTeal = Color(0xFF4AA3A8)
    val WarmWhite = Color(0xFFFFF8E7)
}

@Immutable
data class PolaroamColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val paper: Color,
    val parchment: Color,
    val onPaper: Color,
    val border: Color,
    val shadow: Color,
    val accent: Color,
    val onAccent: Color,
    val highlight: Color,
    val success: Color,
    val successDeep: Color,
    val danger: Color,
    val onDanger: Color,
    val info: Color,
    val cork: Color,
    val photoPaper: Color,
) {
    companion object {
        val Default = PolaroamColors(
            background = PolaroamPalette.InkNavy,
            onBackground = PolaroamPalette.Cream,
            surface = PolaroamPalette.NightBlue,
            onSurface = PolaroamPalette.Cream,
            paper = PolaroamPalette.Cream,
            parchment = PolaroamPalette.Parchment,
            onPaper = PolaroamPalette.InkNavy,
            border = PolaroamPalette.DarkBark,
            shadow = PolaroamPalette.InkNavy.copy(alpha = 0.6f),
            accent = PolaroamPalette.Amber,
            onAccent = PolaroamPalette.InkNavy,
            highlight = PolaroamPalette.Gold,
            success = PolaroamPalette.MossGreen,
            successDeep = PolaroamPalette.PineGreen,
            danger = PolaroamPalette.BrickRed,
            onDanger = PolaroamPalette.WarmWhite,
            info = PolaroamPalette.SkyTeal,
            cork = PolaroamPalette.CorkBrown,
            photoPaper = PolaroamPalette.WarmWhite,
        )
    }
}
