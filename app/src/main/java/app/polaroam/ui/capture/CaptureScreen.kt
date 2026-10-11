package app.polaroam.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.polaroam.R
import app.polaroam.core.result.runCatchingCancellable
import app.polaroam.domain.model.Quest
import app.polaroam.ui.board.QuestStamp
import app.polaroam.ui.components.PanelIllustration
import app.polaroam.ui.components.PixelArt
import app.polaroam.ui.components.PixelButton
import app.polaroam.ui.components.PixelLoadingDots
import app.polaroam.ui.components.PixelPanel
import app.polaroam.ui.components.PolaroidPhoto
import app.polaroam.ui.preview.PreviewData
import app.polaroam.ui.skin.ButtonStyle
import app.polaroam.ui.skin.PanelStyle
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.skin.tiledBackground
import app.polaroam.ui.theme.PolaroamTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun CaptureScreen(
    onFinished: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permission = rememberCameraPermissionState()
    val controller = rememberCameraController()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CaptureEvent.Finished -> onFinished()
            }
        }
    }

    LaunchedEffect(permission.status) {
        if (permission.status == CameraPermissionStatus.NotRequested) permission.request()
    }

    LaunchedEffect(state.phase) {
        when (state.phase) {
            is CapturePhase.Match -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            is CapturePhase.NoMatch -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> Unit
        }
    }

    CaptureContent(
        state = state,
        permission = permission.status,
        onRequestPermission = permission.request,
        onOpenSettings = permission.openSettings,
        onShutter = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val file = viewModel.beginCapture() ?: return@CaptureContent
            scope.launch {
                runCatchingCancellable { controller.takePictureTo(file, ContextCompat.getMainExecutor(context)) }
                    .onSuccess { viewModel.onPhotoCaptured(it) }
                    .onFailure { viewModel.onCaptureFailed(file) }
            }
        },
        onRetry = viewModel::onRetry,
        onSkip = viewModel::onSkip,
        onDone = viewModel::onDone,
        onBack = onFinished,
        cameraPreview = { modifier -> CameraPreview(controller, modifier) },
    )
}

@Composable
fun CaptureContent(
    state: CaptureUiState,
    permission: CameraPermissionStatus,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onShutter: () -> Unit,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    cameraPreview: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PolaroamTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .tiledBackground(R.drawable.bg_ink)
            .padding(spacing.l),
        verticalArrangement = Arrangement.spacedBy(spacing.m),
    ) {
        QuestHeader(quest = state.quest, nextAttempt = state.nextAttempt)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pixelFrame(PolaroamTheme.skin.panel(PanelStyle.Night).frame),
        ) {
            when (permission) {
                CameraPermissionStatus.Granted -> {
                    cameraPreview(Modifier.fillMaxSize())
                    PixelArt(
                        id = R.drawable.viewfinder,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                CameraPermissionStatus.PermanentlyDenied -> PermissionPrompt(
                    permanentlyDenied = true,
                    onAction = onOpenSettings,
                )
                CameraPermissionStatus.NotRequested, CameraPermissionStatus.Denied -> PermissionPrompt(
                    permanentlyDenied = false,
                    onAction = onRequestPermission,
                )
            }
            PhaseOverlay(state = state, onRetry = onRetry, onSkip = onSkip, onDone = onDone, onBack = onBack)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(spacing.m), verticalAlignment = Alignment.CenterVertically) {
            PixelButton(
                text = stringResource(R.string.capture_back),
                onClick = onBack,
                style = ButtonStyle.Secondary,
                icon = R.drawable.icon_back,
            )
            PixelButton(
                text = stringResource(R.string.capture_snap),
                onClick = onShutter,
                enabled = state.canShoot && permission == CameraPermissionStatus.Granted,
                loading = state.phase == CapturePhase.Capturing,
                icon = R.drawable.icon_shutter,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuestHeader(quest: Quest?, nextAttempt: Int) {
    val typography = PolaroamTheme.typography
    val spacing = PolaroamTheme.spacing
    PixelPanel(style = PanelStyle.Paper, modifier = Modifier.fillMaxWidth()) {
        if (quest == null) {
            PixelLoadingDots()
        } else {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(spacing.s),
            ) {
                QuestStamp(poolKey = quest.poolKey, title = quest.title, category = quest.category)
                Column(modifier = Modifier.weight(1f)) {
                    Text(quest.title, style = typography.label)
                    Text(quest.objective, style = typography.body)
                    Text(
                        stringResource(R.string.capture_look_for, quest.targetLabels.joinToString(", ")),
                        style = typography.caption,
                    )
                    if (nextAttempt > 1) {
                        Text(stringResource(R.string.capture_attempt, nextAttempt), style = typography.caption)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PermissionPrompt(permanentlyDenied: Boolean, onAction: () -> Unit) {
    PixelPanel(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(PolaroamTheme.spacing.l),
    ) {
        PanelIllustration(if (permanentlyDenied) R.drawable.illo_permission_denied else R.drawable.illo_permission)
        Text(stringResource(R.string.capture_permission_title), style = PolaroamTheme.typography.label)
        Text(
            stringResource(if (permanentlyDenied) R.string.capture_permission_denied else R.string.capture_permission_rationale),
            style = PolaroamTheme.typography.body,
        )
        PixelButton(
            text = stringResource(if (permanentlyDenied) R.string.capture_permission_settings else R.string.capture_permission_grant),
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BoxScope.PhaseOverlay(
    state: CaptureUiState,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val typography = PolaroamTheme.typography
    val colors = PolaroamTheme.colors
    val overlayModifier = Modifier
        .align(Alignment.Center)
        .padding(PolaroamTheme.spacing.l)
        .fillMaxWidth()

    when (val phase = state.phase) {
        CapturePhase.Loading, CapturePhase.Ready, CapturePhase.Capturing -> Unit

        CapturePhase.Scanning -> PixelPanel(modifier = overlayModifier) {
            PanelIllustration(R.drawable.illo_scanning)
            Text(stringResource(R.string.capture_scanning), style = typography.label)
            PixelLoadingDots()
        }

        is CapturePhase.Match -> PixelPanel(modifier = overlayModifier) {
            PanelIllustration(R.drawable.illo_match)
            Text(stringResource(R.string.capture_match_title), style = typography.title, color = colors.successDeep)
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                PolaroidPhoto(
                    photoPath = phase.photoPath,
                    caption = state.quest?.title.orEmpty(),
                    contentDescription = stringResource(R.string.capture_photo_cd, state.quest?.title.orEmpty()),
                    tiltDegrees = -2f,
                    modifier = Modifier.width(160.dp),
                )
                PixelArt(
                    id = R.drawable.sticker_star,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp),
                )
            }
            Text(stringResource(R.string.capture_points_earned, phase.points), style = typography.label)
            if (phase.firstTry) Text(stringResource(R.string.capture_first_try), style = typography.caption)
            Text(sawText(phase.label, phase.confidence), style = typography.caption)
            Text(pluralStringResource(R.plurals.streak_days, phase.streak, phase.streak), style = typography.caption)
            PixelButton(text = stringResource(R.string.capture_done), onClick = onDone, modifier = Modifier.fillMaxWidth())
        }

        is CapturePhase.NoMatch -> PixelPanel(modifier = overlayModifier) {
            PanelIllustration(R.drawable.illo_no_match)
            Text(stringResource(R.string.capture_no_match_title), style = typography.title, color = colors.danger)
            Text(stringResource(R.string.capture_no_match_body), style = typography.body)
            Text(sawText(phase.label, phase.confidence), style = typography.caption)
            PixelButton(text = stringResource(R.string.capture_retry), onClick = onRetry, modifier = Modifier.fillMaxWidth())
            PixelButton(
                text = stringResource(R.string.capture_skip),
                onClick = onSkip,
                style = ButtonStyle.Danger,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        CapturePhase.AlreadyResolved -> PixelPanel(modifier = overlayModifier) {
            PanelIllustration(R.drawable.illo_already_done)
            Text(stringResource(R.string.capture_already_resolved), style = typography.body)
            PixelButton(text = stringResource(R.string.capture_back), onClick = onBack, modifier = Modifier.fillMaxWidth())
        }

        is CapturePhase.Error -> PixelPanel(modifier = overlayModifier) {
            PanelIllustration(phase.illustration)
            Text(stringResource(phase.messageRes), style = typography.body, color = colors.danger)
            if (phase.canRetry) {
                PixelButton(text = stringResource(R.string.capture_retry), onClick = onRetry, modifier = Modifier.fillMaxWidth())
            }
            PixelButton(
                text = stringResource(R.string.capture_back),
                onClick = onBack,
                style = ButtonStyle.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun sawText(label: String, confidence: Float): String =
    if (label.isBlank()) {
        stringResource(R.string.capture_saw_nothing)
    } else {
        stringResource(R.string.capture_saw, label, (confidence * 100).roundToInt())
    }

@Preview(showBackground = true)
@Composable
private fun CaptureNoMatchPreview() {
    PolaroamTheme {
        CaptureContent(
            state = CaptureUiState(
                quest = PreviewData.quests.first(),
                attempts = 1,
                phase = CapturePhase.NoMatch(label = "wall", confidence = 0.41f),
            ),
            permission = CameraPermissionStatus.Granted,
            onRequestPermission = {},
            onOpenSettings = {},
            onShutter = {},
            onRetry = {},
            onSkip = {},
            onDone = {},
            onBack = {},
            cameraPreview = { modifier -> Box(modifier.background(PolaroamTheme.colors.surface)) },
        )
    }
}
