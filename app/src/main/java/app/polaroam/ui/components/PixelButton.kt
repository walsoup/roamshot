package app.polaroam.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
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
            Text(text = text, style = PolaroamTheme.typography.button, textAlign = TextAlign.Center)
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
) {
    val skin = if (selected) PolaroamTheme.skin.chipSelected else PolaroamTheme.skin.chip
    SkinnedButton(
        skin = skin,
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        enabled = enabled,
        role = Role.Tab,
    ) {
        Text(text = text, style = PolaroamTheme.typography.label, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun SkinnedButton(
    skin: ButtonSkin,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    role: Role,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val frame = when {
        !enabled -> skin.disabled
        pressed -> skin.pressed
        else -> skin.idle
    }
    val spacing = PolaroamTheme.spacing

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
                .padding(horizontal = spacing.l, vertical = spacing.s),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
