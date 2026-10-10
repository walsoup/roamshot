package app.polaroam.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.domain.model.WalkPreset
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelChip
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PixelTextField
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
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

@OptIn(ExperimentalLayoutApi::class)
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
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.l),
    ) {
        Text(stringResource(R.string.app_name), style = typography.display, color = colors.highlight)
        Text(stringResource(R.string.home_tagline), style = typography.body, color = colors.onBackground)

        state.resumable?.let { resume ->
            PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_resume_title), style = typography.label)
                Text(
                    stringResource(R.string.home_resume_body, resume.walkType, resume.resolved, resume.total),
                    style = typography.caption,
                )
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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.s),
                verticalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                state.presets.forEach { preset ->
                    PixelChip(
                        text = preset.label,
                        selected = state.walkType == preset.label,
                        onClick = { onPresetSelected(preset) },
                        enabled = !state.isStarting,
                    )
                }
            }
        }

        state.errorRes?.let { Text(stringResource(it), style = typography.body, color = colors.danger) }

        PixelButton(
            text = stringResource(R.string.home_start_walk),
            onClick = onStartWalk,
            enabled = state.canStart,
            loading = state.isStarting,
            modifier = Modifier.fillMaxWidth(),
        )
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
