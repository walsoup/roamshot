package app.polaroam.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.domain.model.WalkPreset
import app.polaroam.ui.components.PixelArt
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelChip
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PixelTextField
import app.polaroam.ui.components.PixelWalker
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.skin.tiledBackground
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun HomeScreen(
    onOpenWalk: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.OpenWalk -> onOpenWalk(event.walkId)
            }
        }
    }

    HomeContent(
        state = state,
        onWalkTypeChange = viewModel::onWalkTypeChange,
        onPresetSelected = viewModel::onPresetSelected,
        onStartWalk = viewModel::onStartWalk,
        onResumeWalk = onOpenWalk,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onWalkTypeChange: (String) -> Unit,
    onPresetSelected: (WalkPreset) -> Unit,
    onStartWalk: () -> Unit,
    onResumeWalk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PolaroamTheme.colors
    val typography = PolaroamTheme.typography
    val spacing = PolaroamTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .tiledBackground(R.drawable.bg_ink)
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.l),
    ) {
        Text(stringResource(R.string.app_name).uppercase(), style = typography.display, color = colors.highlight)
        Text(stringResource(R.string.home_tagline), style = typography.body, color = colors.onBackground)

        state.resumable?.let { resume ->
            PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s),
                ) {
                    val preset = WalkPreset.match(resume.walkType)
                    PixelArt(
                        id = if (preset != null) walkPresetScene(preset) else R.drawable.illo_walk_resume,
                        contentDescription = null,
                        pixelSize = PolaroamTheme.dimens.smallPixel,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.home_resume_title), style = typography.label)
                        Text(
                            stringResource(R.string.home_resume_body, resume.walkType, resume.resolved, resume.total),
                            style = typography.caption,
                        )
                    }
                }
                PixelButton(
                    text = stringResource(R.string.home_resume_action),
                    onClick = { onResumeWalk(resume.walkId) },
                    style = ButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        PixelPanel(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.home_walk_type_label), style = typography.label)
            PixelTextField(
                value = state.walkType,
                onValueChange = onWalkTypeChange,
                placeholder = stringResource(R.string.home_walk_type_placeholder),
                enabled = !state.isStarting,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { if (state.canStart) onStartWalk() }),
            )
            Text(
                stringResource(R.string.home_walk_type_counter, state.walkType.length, state.maxLength),
                style = typography.caption,
            )
            Text(stringResource(R.string.home_presets_label), style = typography.caption)
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.s),
                modifier = Modifier.fillMaxWidth(),
            ) {
                state.presets.chunked(2).forEach { rowPresets ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.s),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rowPresets.forEach { preset ->
                            PresetCard(
                                preset = preset,
                                selected = state.walkType == preset.label,
                                onClick = { onPresetSelected(preset) },
                                enabled = !state.isStarting,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowPresets.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        state.errorRes?.let { Text(stringResource(it), style = typography.body, color = colors.danger) }

        if (state.isStarting) {
            PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.m),
                ) {
                    PixelWalker()
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Crafting your adventure...",
                            style = typography.label,
                            color = colors.highlight,
                        )
                        Text(
                            text = "On-device AI is creating custom photo quests",
                            style = typography.caption,
                            color = colors.onBackground,
                        )
                    }
                }
            }
        }

        PixelButton(
            text = if (state.isStarting) "Crafting Quests..." else stringResource(R.string.home_start_walk),
            onClick = onStartWalk,
            enabled = state.canStart,
            loading = state.isStarting,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@DrawableRes
fun walkPresetScene(preset: WalkPreset): Int = when (preset) {
    WalkPreset.NEIGHBORHOOD -> R.drawable.scene_neighborhood
    WalkPreset.CITY_PARK -> R.drawable.scene_city_park
    WalkPreset.FOREST_TRAIL -> R.drawable.scene_forest_trail
    WalkPreset.COASTAL_WALK -> R.drawable.scene_coastal_walk
    WalkPreset.RAINY_STROLL -> R.drawable.scene_rainy_stroll
    WalkPreset.MOUNTAIN_TRAIL -> R.drawable.scene_mountain_trail
    WalkPreset.NIGHT_WALK -> R.drawable.scene_night_walk
}

@Composable
private fun PresetCard(
    preset: WalkPreset,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val spacing = PolaroamTheme.spacing
    PixelChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(spacing.xs),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PixelArt(
                id = walkPresetScene(preset),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(spacing.xxs))
            Text(
                text = preset.label,
                style = PolaroamTheme.typography.label,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1D1A2F)
@Composable
private fun HomeContentPreview() {
    PolaroamTheme {
        HomeContent(
            state = HomeUiState(
                walkType = "Forest trail",
                resumable = ResumableWalk("w1", "City park", 2, 4),
            ),
            onWalkTypeChange = {},
            onPresetSelected = {},
            onStartWalk = {},
            onResumeWalk = {},
        )
    }
}
