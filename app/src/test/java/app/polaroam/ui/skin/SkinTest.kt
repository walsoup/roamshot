package app.polaroam.ui.skin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.polaroam.ui.theme.PolaroamColors
import app.polaroam.ui.theme.PolaroamDimens
import app.polaroam.ui.theme.PolaroamSpacing
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.Mockito

class SkinTest {

    private val colors = PolaroamColors.Default
    private val dimens = PolaroamDimens()
    private val spacing = PolaroamSpacing()

    private fun bitmap(): ImageBitmap = Mockito.mock(ImageBitmap::class.java)
    private fun buttonImages() = ButtonImages(bitmap(), bitmap(), bitmap())

    private val pixelSkin = PolaroamSkins.pixel(
        images = PixelSkinImages(
            panels = PanelStyle.entries.associateWith { bitmap() },
            nightFill = bitmap(),
            buttons = ButtonStyle.entries.associateWith { buttonImages() },
            chip = buttonImages(),
            chipSelected = buttonImages(),
            input = bitmap(),
            polaroid = bitmap(),
            tags = TagStyle.entries.associateWith { bitmap() },
            bottomBar = bitmap(),
            progressTrack = bitmap(),
            progressFill = bitmap(),
        ),
        colors = colors,
        dimens = dimens,
        spacing = spacing,
    )
    private val flatSkin = PolaroamSkins.flat(colors, dimens, spacing)

    private fun PaddingValues.ltr(): List<Dp> = listOf(
        calculateLeftPadding(LayoutDirection.Ltr),
        calculateTopPadding(),
        calculateRightPadding(LayoutDirection.Ltr),
        calculateBottomPadding(),
    )

    private fun PolaroamSkin.allButtons() = buttons.values + listOf(chip, chipSelected)

    @Test
    fun bothSkinsCoverEveryStyle() {
        for (skin in listOf(flatSkin, pixelSkin)) {
            assertThat(skin.panels.keys).containsExactlyElementsIn(PanelStyle.entries)
            assertThat(skin.buttons.keys).containsExactlyElementsIn(ButtonStyle.entries)
            assertThat(skin.tags.keys).containsExactlyElementsIn(TagStyle.entries)
        }
    }

    @Test
    fun flatFrameDefaultInsetFollowsBorderAndShadow() {
        val normal = FrameSkin.Flat(colors.paper, colors.border, borderWidth = 4.dp, shadowOffset = 4.dp)
        val sunken = FrameSkin.Flat(colors.paper, colors.border, borderWidth = 4.dp, shadowOffset = 4.dp, sunken = true)

        assertThat(normal.contentInset.ltr()).containsExactly(4.dp, 4.dp, 8.dp, 8.dp).inOrder()
        assertThat(sunken.contentInset.ltr()).containsExactly(8.dp, 8.dp, 4.dp, 4.dp).inOrder()
    }

    @Test
    fun pixelSkinUsesNinePatchArtEverywhere() {
        val frames = pixelSkin.panels.values.map { it.frame } +
            pixelSkin.allButtons().flatMap { listOf(it.idle, it.pressed, it.disabled) } +
            pixelSkin.tags.values.map { it.frame } +
            listOf(
                pixelSkin.input,
                pixelSkin.polaroid.frame,
                pixelSkin.bottomBar,
                pixelSkin.progress.track,
                pixelSkin.progress.fill,
            )
        for (frame in frames) {
            assertThat(frame).isInstanceOf(FrameSkin.NinePatch::class.java)
        }
    }

    @Test
    fun ninePatchInsetsMatchTheAssetSpec() {
        fun insets(frame: FrameSkin) = (frame as FrameSkin.NinePatch).insets

        for (panel in pixelSkin.panels.values) {
            assertThat(insets(panel.frame)).isEqualTo(NinePatchInsets(8, 8, 10, 10))
        }
        for (button in pixelSkin.buttons.values) {
            assertThat(insets(button.idle)).isEqualTo(NinePatchInsets(8, 8, 10, 10))
            assertThat(insets(button.pressed)).isEqualTo(NinePatchInsets(8, 8, 10, 10))
            assertThat(insets(button.disabled)).isEqualTo(NinePatchInsets(8, 8, 8, 8))
        }
        for (chip in listOf(pixelSkin.chip, pixelSkin.chipSelected)) {
            assertThat(insets(chip.idle)).isEqualTo(NinePatchInsets(8, 6, 10, 8))
            assertThat(insets(chip.pressed)).isEqualTo(NinePatchInsets(8, 6, 10, 8))
            assertThat(insets(chip.disabled)).isEqualTo(NinePatchInsets(8, 6, 8, 6))
        }
        assertThat(insets(pixelSkin.input)).isEqualTo(NinePatchInsets(6, 6, 6, 6))
        assertThat(insets(pixelSkin.polaroid.frame)).isEqualTo(NinePatchInsets(8, 8, 10, 18))
        assertThat(insets(pixelSkin.bottomBar)).isEqualTo(NinePatchInsets(0, 4, 0, 0))
        assertThat(insets(pixelSkin.progress.track)).isEqualTo(NinePatchInsets(4, 4, 4, 4))
        for (tag in pixelSkin.tags.values) {
            assertThat(insets(tag.frame)).isEqualTo(NinePatchInsets(4, 4, 4, 4))
        }
    }

    @Test
    fun panelsPadContentByTheSpecsFourFourSixSixPixels() {
        for (skin in listOf(flatSkin, pixelSkin)) {
            for (panel in skin.panels.values) {
                assertThat(panel.frame.contentInset.ltr()).containsExactly(8.dp, 8.dp, 12.dp, 12.dp).inOrder()
            }
        }
    }

    @Test
    fun pressedButtonsMoveTheirContentTwoArtPixelsDownRight() {
        for (skin in listOf(flatSkin, pixelSkin)) {
            for (button in skin.allButtons()) {
                assertThat(button.idle.contentInset.ltr()).containsExactly(8.dp, 8.dp, 12.dp, 12.dp).inOrder()
                assertThat(button.pressed.contentInset.ltr()).containsExactly(12.dp, 12.dp, 8.dp, 8.dp).inOrder()
                assertThat(button.disabled.contentInset.ltr()).isEqualTo(button.idle.contentInset.ltr())
            }
        }
    }

    @Test
    fun polaroidInsetLeavesTheBorderAndCaptionChin() {
        for (skin in listOf(flatSkin, pixelSkin)) {
            assertThat(skin.polaroid.frame.contentInset.ltr()).containsExactly(8.dp, 8.dp, 12.dp, 32.dp).inOrder()
        }
    }

    @Test
    fun onlyNightPanelsCarryAFillTexture() {
        for ((style, panel) in pixelSkin.panels) {
            val texture = (panel.frame as FrameSkin.NinePatch).fillTexture
            if (style == PanelStyle.Night) assertThat(texture).isNotNull() else assertThat(texture).isNull()
        }
    }
}
