package app.polaroam.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import app.polaroam.ui.theme.PolaroamTheme
import coil3.compose.AsyncImage
import kotlin.math.roundToInt

/** Every image in the app goes through these so nearest-neighbor sampling is never forgotten. */
@Composable
fun PixelImage(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        filterQuality = FilterQuality.None,
    )
}

@Composable
fun PixelImage(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    PixelImage(ImageBitmap.imageResource(id), contentDescription, modifier, contentScale)
}

/**
 * Sizes itself from the art so every art pixel is the same whole number of dp: [pixelSize]
 * when that fits, otherwise the largest whole dp that does (never below 1dp).
 */
@Composable
fun PixelArt(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    pixelSize: Dp = PolaroamTheme.dimens.pixel,
) {
    val bitmap = ImageBitmap.imageResource(id)
    val density = LocalDensity.current
    Layout(
        content = {
            PixelImage(
                bitmap = bitmap,
                contentDescription = contentDescription,
            )
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val pixelSizePx = with(density) { pixelSize.toPx() }
        val wanted = pixelSizePx.roundToInt().coerceAtLeast(1)
        val fitsWidth = if (constraints.hasBoundedWidth) constraints.maxWidth / bitmap.width else wanted
        val fitsHeight = if (constraints.hasBoundedHeight) constraints.maxHeight / bitmap.height else wanted
        val scale = minOf(wanted, fitsWidth, fitsHeight).coerceAtLeast(1)
        val targetWidth = bitmap.width * scale
        val targetHeight = bitmap.height * scale
        val placeable = measurables.first().measure(Constraints.fixed(targetWidth, targetHeight))
        val layoutWidth = targetWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
        val layoutHeight = targetHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(layoutWidth, layoutHeight) {
            val x = (layoutWidth - targetWidth) / 2
            val y = (layoutHeight - targetHeight) / 2
            placeable.placeRelative(x, y)
        }
    }
}

/** A 64px illustration centred at the top of a panel. */
@Composable
fun ColumnScope.PanelIllustration(@DrawableRes id: Int) {
    PixelArt(
        id = id,
        contentDescription = null,
        pixelSize = PolaroamTheme.dimens.smallPixel,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )
}

@Composable
fun PixelAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        filterQuality = FilterQuality.None,
    )
}
