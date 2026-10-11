package app.polaroam.ui.skin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.polaroam.R
import app.polaroam.ui.theme.PolaroamColors
import app.polaroam.ui.theme.PolaroamDimens
import app.polaroam.ui.theme.PolaroamSpacing

/** Source-pixel insets of a nine-patch image (corners that must not stretch). */
@Immutable
data class NinePatchInsets(val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * How a rectangular frame is drawn. Screens never see this: components read it from
 * [PolaroamSkin], so switching from [Flat] to [NinePatch] art changes no screen code.
 */
@Immutable
sealed interface FrameSkin {
    /** Where content starts inside the frame, measured from its outer edge. */
    val contentInset: PaddingValues

    @Immutable
    data class Flat(
        val fill: Color,
        val border: Color,
        val borderWidth: Dp,
        val shadow: Color? = null,
        val shadowOffset: Dp = 0.dp,
        val highlight: Color? = null,
        /** Pressed look: the body drops into the shadow position. */
        val sunken: Boolean = false,
        override val contentInset: PaddingValues = flatInset(borderWidth, shadowOffset, sunken),
    ) : FrameSkin

    @Immutable
    data class NinePatch(
        val image: ImageBitmap,
        val insets: NinePatchInsets,
        /** On-screen size of one source pixel. */
        val pixelSize: Dp,
        override val contentInset: PaddingValues,
        /** Tiled over the stretched centre of the image, e.g. the specks inside Night panels. */
        val fillTexture: ImageBitmap? = null,
    ) : FrameSkin
}

private fun flatInset(borderWidth: Dp, shadowOffset: Dp, sunken: Boolean): PaddingValues =
    if (sunken) {
        PaddingValues(
            start = borderWidth + shadowOffset,
            top = borderWidth + shadowOffset,
            end = borderWidth,
            bottom = borderWidth,
        )
    } else {
        PaddingValues(
            start = borderWidth,
            top = borderWidth,
            end = borderWidth + shadowOffset,
            bottom = borderWidth + shadowOffset,
        )
    }

@Immutable
data class PanelSkin(val frame: FrameSkin, val content: Color)

@Immutable
data class ButtonSkin(
    val idle: FrameSkin,
    val pressed: FrameSkin,
    val disabled: FrameSkin,
    val content: Color,
    val disabledContent: Color,
)

@Immutable
data class TagSkin(val frame: FrameSkin, val content: Color)

@Immutable
data class PolaroidSkin(
    /** Its content inset already leaves the white border and the caption chin. */
    val frame: FrameSkin,
    val photoBackground: Color,
    val captionColor: Color,
    val captionPadding: Dp,
)

@Immutable
data class ProgressSkin(val track: FrameSkin, val fill: FrameSkin)

enum class PanelStyle { Paper, Parchment, Night, Cork }

enum class ButtonStyle { Primary, Secondary, Danger }

/** Tag colours carry meaning, so they are named by role rather than by colour. */
enum class TagStyle { Open, Done, Skipped, ModelSource, BuiltInSource, Category }

@Immutable
data class PolaroamSkin(
    val panels: Map<PanelStyle, PanelSkin>,
    val buttons: Map<ButtonStyle, ButtonSkin>,
    val chip: ButtonSkin,
    val chipSelected: ButtonSkin,
    val input: FrameSkin,
    val polaroid: PolaroidSkin,
    val tags: Map<TagStyle, TagSkin>,
    val bottomBar: FrameSkin,
    val progress: ProgressSkin,
) {
    fun panel(style: PanelStyle): PanelSkin = panels.getValue(style)
    fun button(style: ButtonStyle): ButtonSkin = buttons.getValue(style)
    fun tag(style: TagStyle): TagSkin = tags.getValue(style)
}

/** The idle, pressed and disabled art of one button or chip style. */
@Immutable
data class ButtonImages(val idle: ImageBitmap, val pressed: ImageBitmap, val disabled: ImageBitmap)

/** Every bitmap the pixel skin is built from. */
@Immutable
data class PixelSkinImages(
    val panels: Map<PanelStyle, ImageBitmap>,
    val nightFill: ImageBitmap,
    val buttons: Map<ButtonStyle, ButtonImages>,
    val chip: ButtonImages,
    val chipSelected: ButtonImages,
    val input: ImageBitmap,
    val polaroid: ImageBitmap,
    val tags: Map<TagStyle, ImageBitmap>,
    val bottomBar: ImageBitmap,
    val progressTrack: ImageBitmap,
    val progressFill: ImageBitmap,
)

/** Content padding in source pixels from `design/pixel-asset-prompts.md`, shared by both skins. */
private data class SourcePadding(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    fun toDp(pixel: Dp) = PaddingValues(start = pixel * left, top = pixel * top, end = pixel * right, bottom = pixel * bottom)

    companion object {
        /** Panels, buttons and chips: inside the 2px border, plus the 2px shadow on the right and bottom. */
        val frame = SourcePadding(4, 4, 6, 6)

        /** Pressed art drops 2px down-right into the shadow, and the content goes with it. */
        val framePressed = SourcePadding(6, 6, 4, 4)
        val input = SourcePadding(2, 2, 2, 2)
        val polaroid = SourcePadding(4, 4, 6, 16)
        val tag = SourcePadding(4, 1, 4, 1)
        val bottomBar = SourcePadding(0, 2, 0, 0)
        val progressTrack = SourcePadding(2, 2, 2, 2)
        val none = SourcePadding(0, 0, 0, 0)
    }
}

private object SourceInsets {
    val frame = NinePatchInsets(8, 8, 10, 10)
    val frameDisabled = NinePatchInsets(8, 8, 8, 8)
    val chip = NinePatchInsets(8, 6, 10, 8)
    val chipDisabled = NinePatchInsets(8, 6, 8, 6)
    val input = NinePatchInsets(6, 6, 6, 6)
    val polaroid = NinePatchInsets(8, 8, 10, 18)
    val tag = NinePatchInsets(4, 4, 4, 4)
    val bottomBar = NinePatchInsets(0, 4, 0, 0)
    val progressTrack = NinePatchInsets(4, 4, 4, 4)
    val solid = NinePatchInsets(0, 0, 0, 0)
}

object PolaroamSkins {

    fun flat(colors: PolaroamColors, dimens: PolaroamDimens, spacing: PolaroamSpacing): PolaroamSkin {
        val pixel = dimens.pixel
        val framePadding = SourcePadding.frame.toDp(pixel)

        fun frame(fill: Color, border: Color = colors.border) = FrameSkin.Flat(
            fill = fill,
            border = border,
            borderWidth = dimens.border,
            shadow = colors.shadow,
            shadowOffset = dimens.shadow,
            contentInset = framePadding,
        )

        fun solid(fill: Color, padding: SourcePadding) =
            FrameSkin.Flat(fill = fill, border = fill, borderWidth = 0.dp, contentInset = padding.toDp(pixel))

        fun button(fill: Color, content: Color) = ButtonSkin(
            idle = frame(fill).copy(highlight = colors.photoPaper.copy(alpha = 0.35f)),
            pressed = frame(fill).copy(sunken = true, contentInset = SourcePadding.framePressed.toDp(pixel)),
            disabled = frame(colors.parchment, colors.border.copy(alpha = 0.6f)).copy(shadow = null),
            content = content,
            disabledContent = colors.onPaper.copy(alpha = 0.45f),
        )

        return PolaroamSkin(
            panels = mapOf(
                PanelStyle.Paper to PanelSkin(frame(colors.paper), colors.onPaper),
                PanelStyle.Parchment to PanelSkin(frame(colors.parchment), colors.onPaper),
                PanelStyle.Night to PanelSkin(frame(colors.surface, colors.background), colors.onSurface),
                PanelStyle.Cork to PanelSkin(frame(colors.cork), colors.photoPaper),
            ),
            buttons = mapOf(
                ButtonStyle.Primary to button(colors.accent, colors.onAccent),
                ButtonStyle.Secondary to button(colors.paper, colors.onPaper),
                ButtonStyle.Danger to button(colors.danger, colors.onDanger),
            ),
            chip = button(colors.paper, colors.onPaper),
            chipSelected = button(colors.highlight, colors.onAccent),
            input = FrameSkin.Flat(
                fill = colors.photoPaper,
                border = colors.border,
                borderWidth = dimens.border,
                contentInset = SourcePadding.input.toDp(pixel),
            ),
            polaroid = PolaroidSkin(
                frame = FrameSkin.Flat(
                    fill = colors.photoPaper,
                    border = colors.parchment,
                    borderWidth = pixel,
                    shadow = colors.shadow,
                    shadowOffset = dimens.shadow,
                    contentInset = SourcePadding.polaroid.toDp(pixel),
                ),
                photoBackground = colors.background,
                captionColor = colors.onPaper,
                captionPadding = spacing.s,
            ),
            tags = tagSkins(colors) { style -> solid(tagFill(style, colors), SourcePadding.tag) },
            bottomBar = solid(colors.surface, SourcePadding.bottomBar),
            progress = ProgressSkin(
                track = FrameSkin.Flat(
                    fill = colors.background,
                    border = colors.border,
                    borderWidth = dimens.border,
                    contentInset = SourcePadding.progressTrack.toDp(pixel),
                ),
                fill = solid(colors.accent, SourcePadding.none),
            ),
        )
    }

    /** The pixel-art skin, drawn from the nine-patch images in `drawable-nodpi/`. */
    fun pixel(
        images: PixelSkinImages,
        colors: PolaroamColors = PolaroamColors.Default,
        dimens: PolaroamDimens = PolaroamDimens(),
        spacing: PolaroamSpacing = PolaroamSpacing(),
    ): PolaroamSkin {
        val pixel = dimens.pixel

        fun ninePatch(
            image: ImageBitmap,
            insets: NinePatchInsets,
            padding: SourcePadding,
            fillTexture: ImageBitmap? = null,
        ) = FrameSkin.NinePatch(image, insets, pixel, padding.toDp(pixel), fillTexture)

        fun button(
            art: ButtonImages,
            content: Color,
            insets: NinePatchInsets = SourceInsets.frame,
            disabledInsets: NinePatchInsets = SourceInsets.frameDisabled,
        ) = ButtonSkin(
            idle = ninePatch(art.idle, insets, SourcePadding.frame),
            pressed = ninePatch(art.pressed, insets, SourcePadding.framePressed),
            disabled = ninePatch(art.disabled, disabledInsets, SourcePadding.frame),
            content = content,
            disabledContent = colors.onPaper.copy(alpha = 0.45f),
        )

        fun chip(art: ButtonImages, content: Color) =
            button(art, content, SourceInsets.chip, SourceInsets.chipDisabled)

        fun panel(style: PanelStyle, content: Color) = PanelSkin(
            frame = ninePatch(
                image = images.panels.getValue(style),
                insets = SourceInsets.frame,
                padding = SourcePadding.frame,
                fillTexture = if (style == PanelStyle.Night) images.nightFill else null,
            ),
            content = content,
        )

        return PolaroamSkin(
            panels = mapOf(
                PanelStyle.Paper to panel(PanelStyle.Paper, colors.onPaper),
                PanelStyle.Parchment to panel(PanelStyle.Parchment, colors.onPaper),
                PanelStyle.Night to panel(PanelStyle.Night, colors.onSurface),
                PanelStyle.Cork to panel(PanelStyle.Cork, colors.photoPaper),
            ),
            buttons = mapOf(
                ButtonStyle.Primary to button(images.buttons.getValue(ButtonStyle.Primary), colors.onAccent),
                ButtonStyle.Secondary to button(images.buttons.getValue(ButtonStyle.Secondary), colors.onPaper),
                ButtonStyle.Danger to button(images.buttons.getValue(ButtonStyle.Danger), colors.onDanger),
            ),
            chip = chip(images.chip, colors.onPaper),
            chipSelected = chip(images.chipSelected, colors.onAccent),
            input = ninePatch(images.input, SourceInsets.input, SourcePadding.input),
            polaroid = PolaroidSkin(
                frame = ninePatch(images.polaroid, SourceInsets.polaroid, SourcePadding.polaroid),
                photoBackground = colors.background,
                captionColor = colors.onPaper,
                captionPadding = spacing.s,
            ),
            tags = tagSkins(colors) { style -> ninePatch(images.tags.getValue(style), SourceInsets.tag, SourcePadding.tag) },
            bottomBar = ninePatch(images.bottomBar, SourceInsets.bottomBar, SourcePadding.bottomBar),
            progress = ProgressSkin(
                track = ninePatch(images.progressTrack, SourceInsets.progressTrack, SourcePadding.progressTrack),
                fill = ninePatch(images.progressFill, SourceInsets.solid, SourcePadding.none),
            ),
        )
    }

    private fun tagSkins(colors: PolaroamColors, frame: (TagStyle) -> FrameSkin): Map<TagStyle, TagSkin> =
        TagStyle.entries.associateWith { style -> TagSkin(frame(style), tagContent(style, colors)) }

    private fun tagFill(style: TagStyle, colors: PolaroamColors): Color = when (style) {
        TagStyle.Open -> colors.accent
        TagStyle.Done -> colors.success
        TagStyle.Skipped -> colors.border
        TagStyle.ModelSource -> colors.info
        TagStyle.BuiltInSource -> colors.parchment
        TagStyle.Category -> colors.surface
    }

    private fun tagContent(style: TagStyle, colors: PolaroamColors): Color = when (style) {
        TagStyle.Open -> colors.onAccent
        TagStyle.Done, TagStyle.Skipped -> colors.photoPaper
        TagStyle.ModelSource, TagStyle.BuiltInSource -> colors.onPaper
        TagStyle.Category -> colors.onSurface
    }
}

/** Loads the pixel-art skin from the nine-patch drawables in `drawable-nodpi/`. */
@Composable
fun rememberPixelSkin(
    colors: PolaroamColors = PolaroamColors.Default,
    dimens: PolaroamDimens = PolaroamDimens(),
    spacing: PolaroamSpacing = PolaroamSpacing(),
): PolaroamSkin {
    val images = PixelSkinImages(
        panels = mapOf(
            PanelStyle.Paper to ImageBitmap.imageResource(R.drawable.panel_paper),
            PanelStyle.Parchment to ImageBitmap.imageResource(R.drawable.panel_parchment),
            PanelStyle.Night to ImageBitmap.imageResource(R.drawable.panel_night),
            PanelStyle.Cork to ImageBitmap.imageResource(R.drawable.panel_cork),
        ),
        nightFill = ImageBitmap.imageResource(R.drawable.bg_night),
        buttons = mapOf(
            ButtonStyle.Primary to ButtonImages(
                ImageBitmap.imageResource(R.drawable.button_primary_idle),
                ImageBitmap.imageResource(R.drawable.button_primary_pressed),
                ImageBitmap.imageResource(R.drawable.button_primary_disabled),
            ),
            ButtonStyle.Secondary to ButtonImages(
                ImageBitmap.imageResource(R.drawable.button_secondary_idle),
                ImageBitmap.imageResource(R.drawable.button_secondary_pressed),
                ImageBitmap.imageResource(R.drawable.button_secondary_disabled),
            ),
            ButtonStyle.Danger to ButtonImages(
                ImageBitmap.imageResource(R.drawable.button_danger_idle),
                ImageBitmap.imageResource(R.drawable.button_danger_pressed),
                ImageBitmap.imageResource(R.drawable.button_danger_disabled),
            ),
        ),
        chip = ButtonImages(
            ImageBitmap.imageResource(R.drawable.chip_idle),
            ImageBitmap.imageResource(R.drawable.chip_pressed),
            ImageBitmap.imageResource(R.drawable.chip_disabled),
        ),
        chipSelected = ButtonImages(
            ImageBitmap.imageResource(R.drawable.chip_selected_idle),
            ImageBitmap.imageResource(R.drawable.chip_selected_pressed),
            ImageBitmap.imageResource(R.drawable.chip_selected_disabled),
        ),
        input = ImageBitmap.imageResource(R.drawable.input_frame),
        polaroid = ImageBitmap.imageResource(R.drawable.polaroid_frame),
        tags = mapOf(
            TagStyle.Open to ImageBitmap.imageResource(R.drawable.tag_amber),
            TagStyle.Done to ImageBitmap.imageResource(R.drawable.tag_moss),
            TagStyle.Skipped to ImageBitmap.imageResource(R.drawable.tag_bark),
            TagStyle.ModelSource to ImageBitmap.imageResource(R.drawable.tag_teal),
            TagStyle.BuiltInSource to ImageBitmap.imageResource(R.drawable.tag_parchment),
            TagStyle.Category to ImageBitmap.imageResource(R.drawable.tag_night),
        ),
        bottomBar = ImageBitmap.imageResource(R.drawable.bar_bottom),
        progressTrack = ImageBitmap.imageResource(R.drawable.progress_track),
        progressFill = ImageBitmap.imageResource(R.drawable.progress_fill),
    )
    return remember(images, colors, dimens, spacing) { PolaroamSkins.pixel(images, colors, dimens, spacing) }
}
