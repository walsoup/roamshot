package app.polaroam.ui.stats

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.domain.model.ModelState
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
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
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.l),
    ) {
        Text(stringResource(R.string.stats_title), style = typography.display, color = colors.highlight)

        StatTile(
            label = stringResource(R.string.stats_total_score),
            value = state.totalScore.toString(),
        )
        StatTile(
            label = stringResource(R.string.stats_current_streak),
            value = pluralStringResource(R.plurals.streak_days, state.currentStreak, state.currentStreak),
            footnote = stringResource(
                R.string.stats_best_streak,
                pluralStringResource(R.plurals.streak_days, state.longestStreak, state.longestStreak),
            ),
        )
        StatTile(
            label = stringResource(R.string.stats_quests_completed),
            value = state.questsCompleted.toString(),
        )

        ModelPanel(
            state = state.modelState,
            onDownload = onDownloadModel,
            onCancel = onCancelDownload,
            onDelete = onDeleteModel,
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, footnote: String? = null) {
    val typography = PolaroamTheme.typography
    PixelPanel(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = typography.caption)
        Text(value, style = typography.display, color = PolaroamTheme.colors.border)
        footnote?.let { Text(it, style = typography.caption) }
    }
}

@Composable
private fun ModelPanel(
    state: ModelState,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val typography = PolaroamTheme.typography
    PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.model_title), style = typography.label)
        when (state) {
            ModelState.NotConfigured -> Text(stringResource(R.string.model_not_configured), style = typography.body)

            ModelState.NotDownloaded -> {
                Text(stringResource(R.string.model_not_downloaded), style = typography.body)
                PixelButton(text = stringResource(R.string.model_download), onClick = onDownload, modifier = Modifier.fillMaxWidth())
            }

            is ModelState.Downloading -> {
                val progress = state.progress
                Text(
                    text = if (progress == null) {
                        stringResource(R.string.model_downloading_indeterminate)
                    } else {
                        stringResource(R.string.model_downloading, (progress * 100).roundToInt())
                    },
                    style = typography.body,
                )
                PixelProgressBar(progress = progress ?: 0f)
                PixelButton(
                    text = stringResource(R.string.model_cancel),
                    onClick = onCancel,
                    style = ButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ModelState.Ready -> {
                Text(stringResource(R.string.model_ready), style = typography.body)
                PixelButton(
                    text = stringResource(R.string.model_delete),
                    onClick = onDelete,
                    style = ButtonStyle.Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            is ModelState.Failed -> {
                Text(stringResource(R.string.model_failed, state.reason), style = typography.body)
                PixelButton(text = stringResource(R.string.model_retry), onClick = onDownload, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PixelProgressBar(progress: Float) {
    val colors = PolaroamTheme.colors
    val dimens = PolaroamTheme.dimens
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(colors.border)
            .padding(dimens.pixel)
            .background(colors.background),
    ) {
        Row(Modifier.fillMaxSize()) {
            val fraction = progress.coerceIn(0f, 1f)
            if (fraction > 0f) {
                Box(
                    Modifier
                        .weight(fraction)
                        .fillMaxSize()
                        .background(colors.accent),
                )
            }
            if (fraction < 1f) {
                Box(Modifier.weight(1f - fraction).fillMaxSize())
            }
        }
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
            onDownloadModel = {},
            onCancelDownload = {},
            onDeleteModel = {},
        )
    }
}
