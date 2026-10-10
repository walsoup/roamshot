package app.polaroam.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun PixelPanel(
    modifier: Modifier = Modifier,
    style: PanelStyle = PanelStyle.Paper,
    contentPadding: PaddingValues = PaddingValues(PolaroamTheme.spacing.m),
    spacing: Dp = PolaroamTheme.spacing.s,
    content: @Composable ColumnScope.() -> Unit,
) {
    val panel = PolaroamTheme.skin.panel(style)
    CompositionLocalProvider(LocalContentColor provides panel.content) {
        Column(
            modifier = modifier
                .pixelFrame(panel.frame)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content,
        )
    }
}
