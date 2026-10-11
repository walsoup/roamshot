package app.polaroam.ui.board

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
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
import app.polaroam.ui.components.PanelIllustration
import app.polaroam.ui.components.PixelArt
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PixelTag
import app.polaroam.ui.components.PixelWalker
import app.polaroam.ui.components.categoryLabel
import app.polaroam.ui.preview.PreviewData
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.skin.TagStyle
import app.polaroam.ui.skin.tiledBackground
import app.polaroam.ui.theme.PolaroamTheme

@Composable
fun QuestBoardScreen(
    onQuestSelected: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: QuestBoardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuestBoardContent(
        state = state,
        onQuestSelected = onQuestSelected,
        onBack = onBack,
        onAbandon = viewModel::onAbandonWalk,
    )
}

@Composable
fun QuestBoardContent(
    state: QuestBoardUiState,
    onQuestSelected: (String) -> Unit,
    onBack: () -> Unit,
    onAbandon: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .tiledBackground(R.drawable.bg_cork),
    ) {
        when (state) {
            QuestBoardUiState.Loading -> PixelWalker(Modifier.align(Alignment.Center))
            QuestBoardUiState.NotFound -> BoardMessage(R.drawable.illo_not_found, stringResource(R.string.board_not_found), onBack)
            QuestBoardUiState.Error -> BoardMessage(R.drawable.illo_error, stringResource(R.string.board_error), onBack)
            is QuestBoardUiState.Loaded -> LoadedBoard(state, onQuestSelected, onBack, onAbandon)
        }
    }
}

@Composable
private fun LoadedBoard(
    state: QuestBoardUiState.Loaded,
    onQuestSelected: (String) -> Unit,
    onBack: () -> Unit,
    onAbandon: () -> Unit,
) {
    val spacing = PolaroamTheme.spacing
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors
    var showAbandonConfirm by remember { mutableStateOf(false) }

    if (showAbandonConfirm) {
        Dialog(onDismissRequest = { showAbandonConfirm = false }) {
            PixelPanel(style = PanelStyle.Night, modifier = Modifier.fillMaxWidth()) {
                PanelIllustration(R.drawable.illo_no_match)
                Text("Abandon Walk?", style = typography.title, color = colors.danger)
                Text(
                    "Any photos you took will remain safely in your gallery. Unfinished quests will be skipped.",
                    style = typography.body,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.s),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PixelButton(
                        text = "Keep Walking",
                        onClick = { showAbandonConfirm = false },
                        style = ButtonStyle.Secondary,
                        modifier = Modifier.weight(1f),
                    )
                    PixelButton(
                        text = "Abandon",
                        onClick = {
                            showAbandonConfirm = false
                            onAbandon()
                        },
                        style = ButtonStyle.Danger,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

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
                if (state.source == QuestSource.MODEL) {
                    PixelTag(
                        text = stringResource(R.string.board_source_model),
                        style = TagStyle.ModelSource,
                        icon = R.drawable.icon_source_model,
                    )
                } else {
                    PixelTag(
                        text = stringResource(R.string.board_source_builtin),
                        style = TagStyle.BuiltInSource,
                        icon = R.drawable.icon_source_builtin,
                    )
                }
            }
        }

        if (state.isFinished) {
            item(key = "finished") {
                PixelPanel(style = PanelStyle.Parchment, modifier = Modifier.fillMaxWidth()) {
                    PanelIllustration(R.drawable.illo_walk_complete)
                    Text(
                        stringResource(R.string.board_finished_title).uppercase(),
                        style = typography.title,
                        color = colors.successDeep,
                    )
                    Text(
                        "You completed your stroll! You earned ${state.earnedPoints} points and took ${state.completedCount} photos.",
                        style = typography.body,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PixelTag(
                            text = "+${state.earnedPoints} pts",
                            style = TagStyle.Open,
                            icon = R.drawable.icon_points,
                        )
                        PixelTag(
                            text = "${state.completedCount}/${state.totalCount} Snapped",
                            style = TagStyle.Done,
                            icon = R.drawable.icon_status_done,
                        )
                    }
                    PixelButton(
                        text = "Finish & Return Home",
                        onClick = onBack,
                        style = ButtonStyle.Primary,
                        icon = R.drawable.icon_back,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        items(state.cards, key = { it.questId }) { card ->
            QuestCard(card = card, onClick = { onQuestSelected(card.questId) })
        }

        if (!state.isFinished) {
            item(key = "abandon") {
                PixelButton(
                    text = "Abandon Walk",
                    onClick = { showAbandonConfirm = true },
                    style = ButtonStyle.Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun QuestCard(card: QuestCardUi, onClick: () -> Unit) {
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors
    val spacing = PolaroamTheme.spacing
    val iconPixel = PolaroamTheme.dimens.smallPixel

    PixelPanel(
        style = if (card.isOpen) PanelStyle.Paper else PanelStyle.Parchment,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = card.isOpen, role = Role.Button, onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            QuestStamp(poolKey = card.poolKey, title = card.title, category = card.category)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(card.title, style = typography.label, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(spacing.s))
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                        ) {
                            PixelArt(id = R.drawable.icon_points, contentDescription = null, pixelSize = iconPixel)
                            Text(
                                text = stringResource(R.string.quest_points, card.earnedPoints ?: card.basePoints),
                                style = typography.label,
                                color = colors.border,
                            )
                        }
                        if (card.isOpen) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                            ) {
                                PixelArt(id = R.drawable.icon_first_try, contentDescription = null, pixelSize = iconPixel)
                                Text(stringResource(R.string.quest_first_try_hint, card.firstTryBonus), style = typography.caption)
                            }
                        }
                    }
                }
                Text(card.objective, style = typography.body)
                Spacer(Modifier.height(spacing.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.s),
                ) {
                    PixelTag(
                        text = categoryLabel(card.category),
                        style = TagStyle.Category,
                        icon = categoryIcon(card.category),
                    )
                    DifficultyPips(card.difficulty)
                    Spacer(Modifier.weight(1f))
                    StatusTag(card.status)
                }
            }
        }
    }
}

private data class StatusLook(@StringRes val label: Int, val style: TagStyle, @DrawableRes val icon: Int)

private fun QuestStatus.look(): StatusLook = when (this) {
    QuestStatus.OPEN -> StatusLook(R.string.quest_status_open, TagStyle.Open, R.drawable.icon_status_open)
    QuestStatus.COMPLETED -> StatusLook(R.string.quest_status_done, TagStyle.Done, R.drawable.icon_status_done)
    QuestStatus.SKIPPED -> StatusLook(R.string.quest_status_skipped, TagStyle.Skipped, R.drawable.icon_status_skipped)
}

@Composable
private fun StatusTag(status: QuestStatus) {
    val look = status.look()
    PixelTag(text = stringResource(look.label), style = look.style, icon = look.icon)
}

@Composable
private fun BoardMessage(@DrawableRes illustration: Int, message: String, onBack: () -> Unit) {
    val spacing = PolaroamTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.l),
        verticalArrangement = Arrangement.Center,
    ) {
        PixelPanel(modifier = Modifier.fillMaxWidth()) {
            PanelIllustration(illustration)
            Text(message, style = PolaroamTheme.typography.body)
            PixelButton(
                text = stringResource(R.string.board_back),
                onClick = onBack,
                icon = R.drawable.icon_back,
                modifier = Modifier.fillMaxWidth(),
            )
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
