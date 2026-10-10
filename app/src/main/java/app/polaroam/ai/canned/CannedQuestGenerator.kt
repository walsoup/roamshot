package app.polaroam.ai.canned

import app.polaroam.domain.ai.QuestGenerator
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestRules
import app.polaroam.domain.model.WalkPreset
import java.util.UUID
import kotlin.random.Random

/** Picks quests from the built-in pool. Also serves as the fallback when the LLM fails. */
class CannedQuestGenerator(
    private val pool: List<PoolQuest> = CannedQuestPool.all,
    private val random: Random = Random.Default,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) : QuestGenerator {

    override suspend fun generate(walkType: String, count: Int): List<Quest> = pick(walkType, count)

    fun pick(walkType: String, count: Int): List<Quest> {
        val wanted = QuestRules.clampCount(count)
        val preset = WalkPreset.match(walkType)
        val daytimeGeneric = pool.filter { it.presets.isEmpty() }

        val primary = when (preset) {
            null -> daytimeGeneric
            else -> pool.filter { preset in it.presets }
        }
        // Night walks never borrow daytime quests.
        val fillers = if (preset == WalkPreset.NIGHT_WALK) emptyList() else daytimeGeneric

        return (primary.shuffled(random) + fillers.shuffled(random))
            .distinctBy { it.key }
            .take(wanted)
            .sortedBy { it.difficulty.level }
            .map { it.toQuest(idFactory()) }
    }
}
