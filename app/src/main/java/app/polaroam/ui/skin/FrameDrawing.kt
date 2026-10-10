package app.polaroam.ui.skin

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/** Draws [skin] behind the content and insets the content by the frame's own thickness. */
fun Modifier.pixelFrame(skin: FrameSkin): Modifier =
    this
        .drawBehind { drawFrame(skin) }
        .padding(skin.contentInset)

fun DrawScope.drawFrame(skin: FrameSkin) {
    when (skin) {
        is FrameSkin.Flat -> drawFlatFrame(skin)
        is FrameSkin.NinePatch -> drawNinePatch(skin.image, skin.insets, skin.pixelSize.toPx())
    }
}

private fun DrawScope.drawFlatFrame(skin: FrameSkin.Flat) {
    val offset = skin.shadowOffset.toPx()
    val border = skin.borderWidth.toPx()
    val body = Size((size.width - offset).coerceAtLeast(0f), (size.height - offset).coerceAtLeast(0f))
    val bodyTopLeft = if (skin.sunken) Offset(offset, offset) else Offset.Zero

    if (!skin.sunken && skin.shadow != null && offset > 0f) {
        drawRect(color = skin.shadow, topLeft = Offset(offset, offset), size = body)
    }
    drawRect(color = skin.border, topLeft = bodyTopLeft, size = body)

    val inner = Size((body.width - 2 * border).coerceAtLeast(0f), (body.height - 2 * border).coerceAtLeast(0f))
    val innerTopLeft = bodyTopLeft + Offset(border, border)
    drawRect(color = skin.fill, topLeft = innerTopLeft, size = inner)

    if (skin.highlight != null && !skin.sunken) {
        drawRect(color = skin.highlight, topLeft = innerTopLeft, size = Size(inner.width, border.coerceAtMost(inner.height)))
    }
}

/** Nine-slice draw with nearest-neighbor sampling so pixel art stays sharp at any scale. */
fun DrawScope.drawNinePatch(image: ImageBitmap, insets: NinePatchInsets, pixelSize: Float) {
    val srcX = intArrayOf(0, insets.left, image.width - insets.right, image.width)
    val srcY = intArrayOf(0, insets.top, image.height - insets.bottom, image.height)
    val width = size.width.roundToInt()
    val height = size.height.roundToInt()
    val dstX = intArrayOf(
        0,
        (insets.left * pixelSize).roundToInt(),
        width - (insets.right * pixelSize).roundToInt(),
        width,
    )
    val dstY = intArrayOf(
        0,
        (insets.top * pixelSize).roundToInt(),
        height - (insets.bottom * pixelSize).roundToInt(),
        height,
    )

    for (row in 0..2) {
        for (col in 0..2) {
            val srcW = srcX[col + 1] - srcX[col]
            val srcH = srcY[row + 1] - srcY[row]
            val dstW = dstX[col + 1] - dstX[col]
            val dstH = dstY[row + 1] - dstY[row]
            if (srcW <= 0 || srcH <= 0 || dstW <= 0 || dstH <= 0) continue
            drawImage(
                image = image,
                srcOffset = IntOffset(srcX[col], srcY[row]),
                srcSize = IntSize(srcW, srcH),
                dstOffset = IntOffset(dstX[col], dstY[row]),
                dstSize = IntSize(dstW, dstH),
                filterQuality = FilterQuality.None,
            )
        }
    }
}
