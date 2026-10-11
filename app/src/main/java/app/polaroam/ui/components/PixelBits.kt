package app.polaroam.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.polaroam.R
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.QuestCategory
import app.polaroam.ui.skin.TagStyle
import app.polaroam.ui.skin.drawFrame
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.theme.PolaroamTheme
import kotlin.math.roundToInt

/** Three loading squares tinted to [color]; the art is a plain cream square. */
@Composable
fun PixelLoadingDots(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    val description = stringResource(R.string.loading)
    val dot = ImageBitmap.imageResource(R.drawable.dot_loading)
    val dotSize = PolaroamTheme.dimens.pixel * dot.width
    val transition = rememberInfiniteTransition(label = "dots")
    val step by transition.animateValue(
        initialValue = 0,
        targetValue = DOT_COUNT,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "dotStep",
    )
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(PolaroamTheme.spacing.xs),
    ) {
        repeat(DOT_COUNT) { index ->
            Image(
                bitmap = dot,
                contentDescription = null,
                modifier = Modifier.size(dotSize),
                alpha = if (index <= step) 1f else 0.25f,
                colorFilter = ColorFilter.tint(color),
                filterQuality = FilterQuality.None,
            )
        }
    }
}

private const val DOT_COUNT = 3

/** The walking sprite, for loading states on cork screens. */
@Composable
fun PixelWalker(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.loading)
    val sheet = ImageBitmap.imageResource(R.drawable.sprite_walker)
    val frameSize = sheet.height
    val frameCount = sheet.width / frameSize
    val transition = rememberInfiniteTransition(label = "walker")
    val step by transition.animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(tween(durationMillis = frameCount * WALKER_FRAME_MS, easing = LinearEasing)),
        label = "walkerFrame",
    )
    Canvas(
        modifier = modifier
            .size(PolaroamTheme.dimens.pixel * frameSize)
            .semantics { contentDescription = description },
    ) {
        drawImage(
            image = sheet,
            srcOffset = IntOffset((step % frameCount) * frameSize, 0),
            srcSize = IntSize(frameSize, frameSize),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.None,
        )
    }
}

private const val WALKER_FRAME_MS = 160

@Composable
fun DifficultyPips(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.quest_difficulty_cd, difficulty.level, Difficulty.entries.size)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(PolaroamTheme.dimens.pixel),
    ) {
        Difficulty.entries.forEach { level ->
            PixelArt(
                id = if (level.level <= difficulty.level) R.drawable.pip_filled else R.drawable.pip_empty,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun PixelTag(
    text: String,
    style: TagStyle,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    val tag = PolaroamTheme.skin.tag(style)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PolaroamTheme.spacing.xxs),
        modifier = modifier.pixelFrame(tag.frame),
    ) {
        if (icon != null) {
            PixelArt(id = icon, contentDescription = null, pixelSize = PolaroamTheme.dimens.smallPixel)
        }
        Text(text = text, style = PolaroamTheme.typography.caption, color = tag.content)
    }
}

@Composable
fun PixelProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val skin = PolaroamTheme.skin.progress
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PolaroamTheme.dimens.pixel * PROGRESS_HEIGHT_PX)
            .pixelFrame(skin.track),
    ) {
        val fraction = progress.coerceIn(0f, 1f)
        if (fraction > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .drawBehind { drawFrame(skin.fill) },
            )
        }
    }
}

private const val PROGRESS_HEIGHT_PX = 8

@Composable
fun categoryLabel(category: QuestCategory): String = stringResource(
    when (category) {
        QuestCategory.NATURE -> R.string.category_nature
        QuestCategory.ANIMALS -> R.string.category_animals
        QuestCategory.ARCHITECTURE -> R.string.category_architecture
        QuestCategory.TEXTURES -> R.string.category_textures
        QuestCategory.COLORS -> R.string.category_colors
        QuestCategory.OBJECTS -> R.string.category_objects
        QuestCategory.SKY -> R.string.category_sky
    },
)

@Composable
fun PixelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = PolaroamTheme.colors
    val textStyle = PolaroamTheme.typography.body.copy(color = colors.onPaper)
    val frame = PolaroamTheme.skin.input
    val spacing = PolaroamTheme.spacing
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        textStyle = textStyle,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(colors.onPaper),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pixelFrame(frame)
                    .padding(horizontal = spacing.m, vertical = spacing.m),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle.copy(color = colors.onPaper.copy(alpha = 0.5f)))
                }
                inner()
            }
        },
    )
}
