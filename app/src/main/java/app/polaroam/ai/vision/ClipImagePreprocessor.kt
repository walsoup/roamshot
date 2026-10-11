package app.polaroam.ai.vision

import android.graphics.Bitmap

/** Turns a photo into the 224×224 normalized NCHW tensor CLIP ViT-B/32 expects. */
object ClipImagePreprocessor {
    const val SIZE = 224

    private val MEAN = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)
    private val STD = floatArrayOf(0.26862954f, 0.26130258f, 0.27577711f)

    fun tensorData(bitmap: Bitmap): FloatArray {
        val square = centerSquare(bitmap)
        val scaled = Bitmap.createScaledBitmap(square, SIZE, SIZE, true)
        val pixels = IntArray(SIZE * SIZE)
        scaled.getPixels(pixels, 0, SIZE, 0, 0, SIZE, SIZE)
        if (scaled !== square && scaled !== bitmap) scaled.recycle()
        if (square !== bitmap) square.recycle()
        return toChw(pixels, SIZE)
    }

    /** ARGB pixels of a [size]×[size] image, row by row, to channel-first normalized floats. */
    fun toChw(pixels: IntArray, size: Int): FloatArray {
        require(pixels.size == size * size) { "Expected ${size * size} pixels, got ${pixels.size}" }
        val plane = size * size
        val out = FloatArray(3 * plane)
        for (i in 0 until plane) {
            val argb = pixels[i]
            out[i] = (((argb shr 16) and 0xFF) / 255f - MEAN[0]) / STD[0]
            out[plane + i] = (((argb shr 8) and 0xFF) / 255f - MEAN[1]) / STD[1]
            out[2 * plane + i] = ((argb and 0xFF) / 255f - MEAN[2]) / STD[2]
        }
        return out
    }

    private fun centerSquare(bitmap: Bitmap): Bitmap {
        val side = minOf(bitmap.width, bitmap.height)
        if (bitmap.width == bitmap.height) return bitmap
        return Bitmap.createBitmap(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side)
    }
}
