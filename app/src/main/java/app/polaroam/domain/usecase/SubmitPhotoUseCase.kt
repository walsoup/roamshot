package app.polaroam.domain.usecase

import app.polaroam.core.result.runCatchingCancellable
import app.polaroam.core.time.AppClock
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.model.Completion
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.model.Verdict
import app.polaroam.domain.repository.PhotoStore
import app.polaroam.domain.repository.ProgressRepository
import app.polaroam.domain.repository.WalkRepository
import app.polaroam.domain.scoring.ScoreCalculator
import app.polaroam.domain.streak.StreakCalculator
import java.io.File
import javax.inject.Inject

sealed interface SubmitResult {
    data class Match(
        val verdict: Verdict,
        val points: Int,
        val firstTry: Boolean,
        val photoPath: String,
        val streak: Int,
    ) : SubmitResult

    data class NoMatch(val verdict: Verdict, val attempt: Int) : SubmitResult

    /** The quest does not exist or was already completed or skipped. */
    data object QuestUnavailable : SubmitResult

    data class Failed(val cause: Throwable) : SubmitResult
}

class SubmitPhotoUseCase @Inject constructor(
    private val verifier: PhotoVerifier,
    private val walks: WalkRepository,
    private val progress: ProgressRepository,
    private val photos: PhotoStore,
    private val scoring: ScoreCalculator,
    private val streaks: StreakCalculator,
    private val clock: AppClock,
) {
    suspend operator fun invoke(questId: String, capture: File): SubmitResult {
        val walkQuest = runCatchingCancellable { walks.getWalkQuest(questId) }
            .getOrElse {
                photos.discard(capture)
                return SubmitResult.Failed(it)
            }
        if (walkQuest == null || walkQuest.status != QuestStatus.OPEN) {
            photos.discard(capture)
            return SubmitResult.QuestUnavailable
        }

        val attempt = walkQuest.attempts + 1
        val verdict = runCatchingCancellable {
            val bitmap = photos.decodeForVerification(capture)
            verifier.verify(bitmap, walkQuest.quest)
        }.getOrElse {
            photos.discard(capture)
            return SubmitResult.Failed(it)
        }

        if (!verdict.match) {
            photos.discard(capture)
            return runCatchingCancellable {
                if (walks.recordFailedAttempt(questId)) {
                    SubmitResult.NoMatch(verdict, attempt)
                } else {
                    SubmitResult.QuestUnavailable
                }
            }.getOrElse { SubmitResult.Failed(it) }
        }

        val points = scoring.pointsFor(walkQuest.quest.difficulty, attempt)
        val firstTry = attempt == 1
        var saved: File? = null
        return runCatchingCancellable {
            val savedFile = photos.persist(capture, questId).also { saved = it }
            val completion = Completion(
                questId = questId,
                photoPath = savedFile.absolutePath,
                points = points,
                firstTry = firstTry,
                label = verdict.label,
                confidence = verdict.confidence,
                completedAt = clock.now(),
                day = clock.today(),
            )
            val newState = progress.recordCompletion(completion, attempt) { state ->
                state.copy(
                    totalScore = state.totalScore + points,
                    streak = streaks.onQuestVerified(state.streak, completion.day),
                )
            }
            SubmitResult.Match(
                verdict = verdict,
                points = points,
                firstTry = firstTry,
                photoPath = savedFile.absolutePath,
                streak = newState.streak.current,
            )
        }.getOrElse { error ->
            photos.discard(saved ?: capture)
            SubmitResult.Failed(error)
        }
    }
}
