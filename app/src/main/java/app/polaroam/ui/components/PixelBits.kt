package app.polaroam.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.polaroam.R
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.QuestCategory
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun PixelLoadingDots(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    val description = stringResource(R.string.loading)
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
            Box(
                Modifier
                    .size(8.dp)
                    .background(if (index <= step) color else color.copy(alpha = 0.25f)),
            )
        }
    }
}

private const val DOT_COUNT = 3

@Composable
fun DifficultyPips(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
) {
    val colors = PolaroamTheme.colors
    val dimens = PolaroamTheme.dimens
    val description = stringResource(R.string.quest_difficulty_cd, difficulty.level, Difficulty.entries.size)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(dimens.pixel),
    ) {
        Difficulty.entries.forEach { level ->
            Box(
                Modifier
                    .size(dimens.pip)
                    .background(colors.border)
                    .padding(dimens.pixel)
                    .background(if (level.level <= difficulty.level) colors.accent else colors.parchment),
            )
        }
    }
}

@Composable
fun PixelTag(
    text: String,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = PolaroamTheme.typography.caption,
        color = content,
        modifier = modifier
            .background(background)
            .padding(horizontal = PolaroamTheme.spacing.s, vertical = PolaroamTheme.spacing.xxs),
    )
}

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
