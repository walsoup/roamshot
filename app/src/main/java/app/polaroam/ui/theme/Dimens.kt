package app.polaroam.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 4dp spacing grid. */
@Immutable
data class PolaroamSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

/**
 * [pixel] is one art pixel; borders and offsets are whole multiples of it.
 * Icons inline with text and illustrations inside panels use [smallPixel] so they stay
 * close to the text size while every art pixel is still a whole number of dp.
 */
@Immutable
data class PolaroamDimens(
    val pixel: Dp = 2.dp,
    val smallPixel: Dp = 1.dp,
    val border: Dp = 4.dp,
    val shadow: Dp = 4.dp,
    val minTouchTarget: Dp = 48.dp,
)
