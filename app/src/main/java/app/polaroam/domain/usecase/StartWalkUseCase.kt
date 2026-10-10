package app.polaroam.domain.usecase

import app.polaroam.core.result.runCatchingCancellable
import app.polaroam.core.time.AppClock
import app.polaroam.domain.ai.QuestGenerator
import app.polaroam.domain.model.QuestRules
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.WalkTypeInput
import app.polaroam.domain.repository.WalkRepository
import javax.inject.Inject

sealed interface StartWalkResult {
    data class Started(val walkId: String, val source: QuestSource) : StartWalkResult
    data object InvalidWalkType : StartWalkResult
    data class Failed(val cause: Throwable) : StartWalkResult
}

class StartWalkUseCase @Inject constructor(
    private val generator: QuestGenerator,
    private val walks: WalkRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(rawWalkType: String, count: Int = QuestRules.DEFAULT_QUESTS): StartWalkResult {
        val walkType = WalkTypeInput.normalize(rawWalkType)
        if (walkType.isEmpty()) return StartWalkResult.InvalidWalkType

        return runCatchingCancellable {
            val generated = generator.generateDetailed(walkType, QuestRules.clampCount(count))
            check(generated.quests.isNotEmpty()) { "Quest generator returned no quests" }
            val walkId = walks.createWalk(walkType, generated.source, generated.quests, clock.now())
            StartWalkResult.Started(walkId, generated.source)
        }.getOrElse { StartWalkResult.Failed(it) }
    }
}
