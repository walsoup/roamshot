package app.polaroam.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import app.polaroam.ui.skin.ButtonSkin
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    @DrawableRes icon: Int? = null,
) {
    SkinnedButton(
        skin = PolaroamTheme.skin.button(style),
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading,
        role = Role.Button,
    ) {
        if (loading) {
            PixelLoadingDots()
        } else {
            IconLabel(text = text, icon = icon, style = PolaroamTheme.typography.button)
        }
    }
}

@Composable
fun PixelChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
    contentPadding: PaddingValues = PixelButtonDefaults.contentPadding,
) {
    PixelChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
    ) {
        IconLabel(text = text, icon = icon, style = PolaroamTheme.typography.label, maxLines = 1)
    }
}

/** A chip around arbitrary content, e.g. a preset card with a scene above its label. */
@Composable
fun PixelChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PixelButtonDefaults.contentPadding,
    content: @Composable () -> Unit,
) {
    SkinnedButton(
        skin = if (selected) PolaroamTheme.skin.chipSelected else PolaroamTheme.skin.chip,
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        enabled = enabled,
        role = Role.Tab,
        contentPadding = contentPadding,
        content = content,
    )
}

object PixelButtonDefaults {
    /** Added inside the frame's own content inset. */
    val contentPadding: PaddingValues
        @Composable get() = PaddingValues(
            horizontal = PolaroamTheme.spacing.m,
            vertical = PolaroamTheme.spacing.xs,
        )
}

@Composable
private fun IconLabel(text: String, @DrawableRes icon: Int?, style: TextStyle, maxLines: Int = Int.MAX_VALUE) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PolaroamTheme.spacing.xxs),
    ) {
        if (icon != null) {
            PixelArt(id = icon, contentDescription = null, pixelSize = PolaroamTheme.dimens.smallPixel)
        }
        Text(
            text = text,
            style = style,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            softWrap = false,
        )
    }
}

@Composable
private fun SkinnedButton(
    skin: ButtonSkin,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    role: Role,
    contentPadding: PaddingValues = PixelButtonDefaults.contentPadding,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val frame = when {
        !enabled -> skin.disabled
        pressed -> skin.pressed
        else -> skin.idle
    }

    CompositionLocalProvider(LocalContentColor provides if (enabled) skin.content else skin.disabledContent) {
        Box(
            modifier = modifier
                .defaultMinSize(minHeight = PolaroamTheme.dimens.minTouchTarget)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = role,
                    onClick = onClick,
                )
                .pixelFrame(frame)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
