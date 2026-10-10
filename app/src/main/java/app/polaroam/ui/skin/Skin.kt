package app.polaroam.ui.skin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    /** Space the frame occupies on each side; content is laid out inside it. */
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
    ) : FrameSkin {
        override val contentInset: PaddingValues
            get() = if (sunken) {
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
    }

    @Immutable
    data class NinePatch(
        val image: ImageBitmap,
        val insets: NinePatchInsets,
        /** On-screen size of one source pixel. */
        val pixelSize: Dp,
        override val contentInset: PaddingValues,
    ) : FrameSkin
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
data class PolaroidSkin(
    val frame: FrameSkin,
    val photoBackground: Color,
    val captionColor: Color,
    val sidePadding: Dp,
    val captionPadding: Dp,
)

enum class PanelStyle { Paper, Parchment, Night, Cork }

enum class ButtonStyle { Primary, Secondary, Danger }

@Immutable
data class PolaroamSkin(
    val panels: Map<PanelStyle, PanelSkin>,
    val buttons: Map<ButtonStyle, ButtonSkin>,
    val chip: ButtonSkin,
    val chipSelected: ButtonSkin,
    val input: FrameSkin,
    val polaroid: PolaroidSkin,
) {
    fun panel(style: PanelStyle): PanelSkin = panels.getValue(style)
    fun button(style: ButtonStyle): ButtonSkin = buttons.getValue(style)
}

object PolaroamSkins {

    fun flat(colors: PolaroamColors, dimens: PolaroamDimens, spacing: PolaroamSpacing): PolaroamSkin {
        fun frame(fill: Color, border: Color = colors.border) =
            FrameSkin.Flat(fill, border, dimens.border, colors.shadow, dimens.shadow)

        fun button(fill: Color, content: Color) = ButtonSkin(
            idle = FrameSkin.Flat(
                fill = fill,
                border = colors.border,
                borderWidth = dimens.border,
                shadow = colors.shadow,
                shadowOffset = dimens.shadow,
                highlight = colors.photoPaper.copy(alpha = 0.35f),
            ),
            pressed = FrameSkin.Flat(
                fill = fill,
                border = colors.border,
                borderWidth = dimens.border,
                shadow = colors.shadow,
                shadowOffset = dimens.shadow,
                sunken = true,
            ),
            disabled = FrameSkin.Flat(
                fill = colors.parchment,
                border = colors.border.copy(alpha = 0.6f),
                borderWidth = dimens.border,
                shadowOffset = dimens.shadow,
            ),
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
            input = FrameSkin.Flat(colors.photoPaper, colors.border, dimens.border),
            polaroid = PolaroidSkin(
                frame = FrameSkin.Flat(
                    fill = colors.photoPaper,
                    border = colors.parchment,
                    borderWidth = dimens.pixel,
                    shadow = colors.shadow,
                    shadowOffset = dimens.shadow,
                ),
                photoBackground = colors.background,
                captionColor = colors.onPaper,
                sidePadding = spacing.s,
                captionPadding = spacing.s,
            ),
        )
    }
}
