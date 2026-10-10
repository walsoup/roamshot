package app.polaroam.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.ui.components.DifficultyPips
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelLoadingDots
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PixelTag
import app.polaroam.ui.components.categoryLabel
import app.polaroam.ui.preview.PreviewData
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun QuestBoardScreen(
    onQuestSelected: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: QuestBoardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuestBoardContent(state = state, onQuestSelected = onQuestSelected, onBack = onBack)
}

@Composable
fun QuestBoardContent(
    state: QuestBoardUiState,
    onQuestSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PolaroamTheme.colors.cork),
    ) {
        when (state) {
            QuestBoardUiState.Loading -> PixelLoadingDots(
                modifier = Modifier.align(Alignment.Center),
                color = PolaroamTheme.colors.photoPaper,
            )
            QuestBoardUiState.NotFound -> BoardMessage(stringResource(R.string.board_not_found), onBack)
            QuestBoardUiState.Error -> BoardMessage(stringResource(R.string.board_error), onBack)
            is QuestBoardUiState.Loaded -> LoadedBoard(state, onQuestSelected, onBack)
        }
    }
}

@Composable
private fun LoadedBoard(
    state: QuestBoardUiState.Loaded,
    onQuestSelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    val spacing = PolaroamTheme.spacing
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors

    LazyColumn(
        contentPadding = PaddingValues(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
                Text(state.walkType, style = typography.title)
                Text(
                    stringResource(R.string.board_progress, state.completedCount, state.totalCount, state.earnedPoints),
                    style = typography.caption,
                )
                PixelTag(
                    text = stringResource(
                        if (state.source == QuestSource.MODEL) R.string.board_source_model else R.string.board_source_builtin,
                    ),
                    background = if (state.source == QuestSource.MODEL) colors.info else colors.parchment,
                    content = colors.onPaper,
                )
            }
        }

        if (state.isFinished) {
            item(key = "finished") {
                PixelPanel(style = PanelStyle.Parchment, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.board_finished_title), style = typography.title, color = colors.successDeep)
                    Text(stringResource(R.string.board_finished_body, state.earnedPoints), style = typography.body)
                    PixelButton(
                        text = stringResource(R.string.board_back),
                        onClick = onBack,
                        style = ButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        items(state.cards, key = { it.questId }) { card ->
            QuestCard(card = card, onClick = { onQuestSelected(card.questId) })
        }
    }
}

@Composable
private fun QuestCard(card: QuestCardUi, onClick: () -> Unit) {
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors
    val spacing = PolaroamTheme.spacing

    PixelPanel(
        style = if (card.isOpen) PanelStyle.Paper else PanelStyle.Parchment,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = card.isOpen, role = Role.Button, onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(card.title, style = typography.label, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(spacing.s))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.quest_points, card.earnedPoints ?: card.basePoints),
                    style = typography.label,
                    color = colors.border,
                )
                if (card.isOpen) {
                    Text(stringResource(R.string.quest_first_try_hint, card.firstTryBonus), style = typography.caption)
                }
            }
        }
        Text(card.objective, style = typography.body)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            PixelTag(categoryLabel(card.category), background = colors.surface, content = colors.onSurface)
            DifficultyPips(card.difficulty)
            Spacer(Modifier.weight(1f))
            StatusTag(card.status)
        }
    }
}

@Composable
private fun StatusTag(status: QuestStatus) {
    val colors = PolaroamTheme.colors
    when (status) {
        QuestStatus.OPEN -> PixelTag(stringResource(R.string.quest_status_open), colors.accent, colors.onAccent)
        QuestStatus.COMPLETED -> PixelTag(stringResource(R.string.quest_status_done), colors.success, colors.photoPaper)
        QuestStatus.SKIPPED -> PixelTag(stringResource(R.string.quest_status_skipped), colors.border, colors.photoPaper)
    }
}

@Composable
private fun BoardMessage(message: String, onBack: () -> Unit) {
    val spacing = PolaroamTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.l),
        verticalArrangement = Arrangement.Center,
    ) {
        PixelPanel(modifier = Modifier.fillMaxWidth()) {
            Text(message, style = PolaroamTheme.typography.body)
            PixelButton(text = stringResource(R.string.board_back), onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuestBoardPreview() {
    PolaroamTheme {
        QuestBoardContent(state = PreviewData.boardState, onQuestSelected = {}, onBack = {})
    }
}
