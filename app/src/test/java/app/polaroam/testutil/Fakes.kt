package app.polaroam.testutil

import android.graphics.Bitmap
import app.polaroam.ai.llm.LlmEngine
import app.polaroam.ai.llm.LlmPrompt
import app.polaroam.domain.model.CompletedQuest
import app.polaroam.domain.model.Completion
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.PlayerProgress
import app.polaroam.domain.model.ProgressState
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.model.Walk
import app.polaroam.domain.model.WalkQuest
import app.polaroam.domain.repository.PhotoStore
import app.polaroam.domain.repository.ProgressRepository
import app.polaroam.domain.repository.WalkRepository
import java.io.File
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.mockito.Mockito

fun testQuest(
    id: String = "q1",
    difficulty: Difficulty = Difficulty.MEDIUM,
    labels: List<String> = listOf("dog", "cat"),
) = Quest(
    id = id,
    title = "Quest $id",
    objective = "Photograph something nice for $id.",
    category = QuestCategory.ANIMALS,
    difficulty = difficulty,
    targetLabels = labels,
)

class FakeWalkRepository : WalkRepository {
    val walks = MutableStateFlow<Map<String, Walk>>(emptyMap())
    var failOnGet = false
    private var nextId = 1

    override suspend fun createWalk(
        walkType: String,
        source: QuestSource,
        quests: List<Quest>,
        startedAt: Instant,
    ): String {
        val id = "walk-${nextId++}"
        val walk = Walk(
            id = id,
            walkType = walkType,
            startedAt = startedAt,
            source = source,
            quests = quests.mapIndexed { index, quest -> WalkQuest(quest, index, QuestStatus.OPEN, 0, null) },
        )
        walks.value = walks.value + (id to walk)
        return id
    }

    override fun observeWalk(walkId: String): Flow<Walk?> = walks.map { it[walkId] }

    override fun observeLatestWalk(): Flow<Walk?> = walks.map { all -> all.values.maxByOrNull { it.startedAt } }

    override suspend fun getWalkQuest(questId: String): WalkQuest? {
        if (failOnGet) throw IOException("database unavailable")
        return findQuest(questId)
    }

    fun findQuest(questId: String): WalkQuest? =
        walks.value.values.asSequence().flatMap { it.quests }.firstOrNull { it.quest.id == questId }

    override suspend fun recordFailedAttempt(questId: String): Boolean = updateQuest(questId) {
        if (it.status == QuestStatus.OPEN) it.copy(attempts = it.attempts + 1) else null
    }

    override suspend fun skipQuest(questId: String): Boolean = updateQuest(questId) {
        if (it.status == QuestStatus.OPEN) it.copy(status = QuestStatus.SKIPPED) else null
    }

    fun markCompleted(questId: String, attempts: Int, completion: Completion): Boolean = updateQuest(questId) {
        if (it.status == QuestStatus.OPEN) {
            it.copy(status = QuestStatus.COMPLETED, attempts = attempts, completion = completion)
        } else {
            null
        }
    }

    override fun observeCompletedQuests(): Flow<List<CompletedQuest>> = walks.map { all ->
        all.values.flatMap { walk ->
            walk.quests.mapNotNull { wq -> wq.completion?.let { CompletedQuest(wq.quest, walk.walkType, it) } }
        }
    }

    private fun updateQuest(questId: String, transform: (WalkQuest) -> WalkQuest?): Boolean {
        var changed = false
        walks.value = walks.value.mapValues { (_, walk) ->
            walk.copy(
                quests = walk.quests.map { wq ->
                    if (wq.quest.id != questId) {
                        wq
                    } else {
                        transform(wq)?.also { changed = true } ?: wq
                    }
                },
            )
        }
        return changed
    }
}

class FakeProgressRepository(
    private val walks: FakeWalkRepository,
) : ProgressRepository {
    val state = MutableStateFlow(ProgressState.EMPTY)
    val completions = mutableListOf<Completion>()
    var failOnRecord = false

    override fun observeProgress(): Flow<PlayerProgress> = state.map { PlayerProgress(it, completions.size) }

    override suspend fun recordCompletion(
        completion: Completion,
        attempts: Int,
        update: (ProgressState) -> ProgressState,
    ): ProgressState {
        if (failOnRecord) throw IOException("disk full")
        check(walks.markCompleted(completion.questId, attempts, completion)) { "Quest not open" }
        completions += completion
        val next = update(state.value)
        state.value = next
        return next
    }
}

class FakePhotoStore(private val directory: File) : PhotoStore {
    val discarded = mutableListOf<File>()
    val persisted = mutableListOf<File>()
    var failDecode = false
    private var counter = 0

    override fun newCaptureFile(): File = File(directory, "capture-${counter++}.jpg").apply { writeText("jpeg") }

    override suspend fun decodeForVerification(capture: File): Bitmap {
        if (failDecode) throw IOException("not an image")
        return Mockito.mock(Bitmap::class.java)
    }

    override suspend fun persist(capture: File, questId: String): File {
        val target = File(directory, "photo-$questId.jpg")
        capture.renameTo(target)
        persisted += target
        return target
    }

    override suspend fun discard(file: File) {
        discarded += file
        file.delete()
    }
}

class FakeLlmEngine(vararg replies: suspend () -> String) : LlmEngine {
    private val queue = ArrayDeque(replies.toList())
    val prompts = mutableListOf<LlmPrompt>()

    override suspend fun complete(prompt: LlmPrompt): String {
        prompts += prompt
        val next = queue.removeFirstOrNull() ?: error("FakeLlmEngine ran out of replies")
        return next()
    }
}
