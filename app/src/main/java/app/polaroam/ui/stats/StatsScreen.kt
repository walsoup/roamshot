package app.polaroam.ui.stats

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.ai.model.QuestModelChoice
import app.polaroam.domain.model.ModelState
import app.polaroam.ui.components.PixelArt
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelChip
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PixelProgressBar
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.skin.tiledBackground
import app.polaroam.ui.theme.PolaroamTheme
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onDownloadModel()
    }

    StatsContent(
        state = state,
        onSelectModel = viewModel::onSelectModel,
        onDownloadModel = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onDownloadModel()
            }
        },
        onCancelDownload = viewModel::onCancelDownload,
        onDeleteModel = viewModel::onDeleteModel,
    )
}

@Composable
fun StatsContent(
    state: StatsUiState,
    onSelectModel: (QuestModelChoice) -> Unit,
    onDownloadModel: () -> Unit,
    onCancelDownload: () -> Unit,
    onDeleteModel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PolaroamTheme.spacing
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .tiledBackground(R.drawable.bg_ink)
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.l),
    ) {
        Text(stringResource(R.string.stats_title), style = typography.display, color = colors.highlight)

        StatTile(
            label = stringResource(R.string.stats_total_score),
            value = state.totalScore.toString(),
            icon = R.drawable.icon_points,
        )
        StatTile(
            label = stringResource(R.string.stats_current_streak),
            value = pluralStringResource(R.plurals.streak_days, state.currentStreak, state.currentStreak),
            footnote = stringResource(
                R.string.stats_best_streak,
                pluralStringResource(R.plurals.streak_days, state.longestStreak, state.longestStreak),
            ),
            icon = R.drawable.icon_streak,
        )
        StatTile(
            label = stringResource(R.string.stats_quests_completed),
            value = state.questsCompleted.toString(),
            icon = R.drawable.icon_camera,
        )

        ModelPanel(
            selectedModel = state.selectedModel,
            onSelectModel = onSelectModel,
            state = state.modelState,
            onDownload = onDownloadModel,
            onCancel = onCancelDownload,
            onDelete = onDeleteModel,
        )

        DeveloperFooter()

        Spacer(Modifier.height(spacing.xl))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    @DrawableRes icon: Int,
    footnote: String? = null,
) {
    val typography = PolaroamTheme.typography
    PixelPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PolaroamTheme.spacing.m),
        ) {
            PixelArt(id = icon, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = typography.caption)
                Text(value, style = typography.display, color = PolaroamTheme.colors.border)
                footnote?.let { Text(it, style = typography.caption) }
            }
        }
    }
}

private class ModelAction(val label: String, val style: ButtonStyle, val onClick: () -> Unit)

private class ModelLook(
    @DrawableRes val illustration: Int,
    val title: String,
    val message: String,
    val action: ModelAction?,
)

@Composable
private fun modelLook(
    choice: QuestModelChoice,
    state: ModelState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
): ModelLook = when (state) {
    ModelState.NotConfigured -> ModelLook(
        illustration = R.drawable.illo_model_missing,
        title = choice.displayName,
        message = stringResource(R.string.model_not_configured),
        action = null,
    )
    ModelState.NotDownloaded -> ModelLook(
        illustration = R.drawable.illo_model_wifi,
        title = "${choice.displayName} (Not Downloaded)",
        message = "Download ${choice.shortName} (~${choice.sizeMb} MB) and CLIP for offline AI quest generation.",
        action = ModelAction("Download ${choice.shortName}", ButtonStyle.Primary, onDownload),
    )
    is ModelState.Downloading -> ModelLook(
        illustration = R.drawable.illo_model_downloading,
        title = "Downloading ${choice.shortName}...",
        message = state.progress?.let { stringResource(R.string.model_downloading, (it * 100).roundToInt()) }
            ?: stringResource(R.string.model_downloading_indeterminate),
        action = ModelAction(stringResource(R.string.model_cancel), ButtonStyle.Secondary, onCancel),
    )
    ModelState.Ready -> ModelLook(
        illustration = R.drawable.illo_model_ready,
        title = "${choice.displayName} Active",
        message = "${choice.shortName} and CLIP are ready on this phone. New walks will use ${choice.shortName}.",
        action = ModelAction("Remove ${choice.shortName}", ButtonStyle.Danger, onDelete),
    )
    is ModelState.Failed -> ModelLook(
        illustration = R.drawable.illo_model_failed,
        title = "Download Failed",
        message = stringResource(R.string.model_failed, state.reason),
        action = ModelAction(stringResource(R.string.model_retry), ButtonStyle.Primary, onDownload),
    )
}

@Composable
private fun ModelPanel(
    selectedModel: QuestModelChoice,
    onSelectModel: (QuestModelChoice) -> Unit,
    state: ModelState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val typography = PolaroamTheme.typography
    val spacing = PolaroamTheme.spacing
    val look = modelLook(selectedModel, state, onDownload, onCancel, onDelete)
    PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
        Text("ON-DEVICE AI MODEL", style = typography.label)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            QuestModelChoice.entries.forEach { choice ->
                PixelChip(
                    selected = choice == selectedModel,
                    onClick = { onSelectModel(choice) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = choice.displayName,
                        style = typography.caption,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.m),
        ) {
            PixelArt(id = look.illustration, contentDescription = null, pixelSize = PolaroamTheme.dimens.smallPixel)
            Column(modifier = Modifier.weight(1f)) {
                Text(look.title, style = typography.label)
                Text(look.message, style = typography.caption)
            }
        }
        if (state is ModelState.Downloading) {
            PixelProgressBar(progress = state.progress ?: 0f)
        }
        look.action?.let { action ->
            PixelButton(
                text = action.label,
                onClick = action.onClick,
                style = action.style,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DeveloperFooter() {
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors
    PixelPanel(
        style = PanelStyle.Night,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "POLAROAM",
                    style = typography.label,
                    color = colors.highlight,
                )
                Text(
                    text = "v${app.polaroam.BuildConfig.VERSION_NAME}",
                    style = typography.caption,
                    color = colors.accent,
                )
            }
            PixelArt(
                id = R.drawable.sticker_heart,
                contentDescription = null,
                pixelSize = PolaroamTheme.dimens.smallPixel,
            )
        }
        Text(
            text = stringResource(R.string.stats_dev_credits),
            style = typography.body,
        )
        Text(
            text = stringResource(R.string.stats_dev_copyright),
            style = typography.caption,
        )
        Text(
            text = stringResource(R.string.stats_dev_tagline),
            style = typography.caption,
            color = colors.highlight,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1D1A2F)
@Composable
private fun StatsPreview() {
    PolaroamTheme {
        StatsContent(
            state = StatsUiState(
                isLoading = false,
                totalScore = 245,
                currentStreak = 3,
                longestStreak = 7,
                questsCompleted = 12,
                modelState = ModelState.Downloading(0.42f),
            ),
            onSelectModel = {},
            onDownloadModel = {},
            onCancelDownload = {},
            onDeleteModel = {},
        )
    }
}
