package app.polaroam.data.db

import app.polaroam.data.db.entity.CompletionEntity
import app.polaroam.data.db.entity.GalleryRow
import app.polaroam.data.db.entity.ProgressEntity
import app.polaroam.data.db.entity.QuestEntity
import app.polaroam.data.db.entity.WalkEntity
import app.polaroam.domain.model.CompletedQuest
import app.polaroam.domain.model.Completion
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.ProgressState
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.model.StreakState
import app.polaroam.domain.model.Walk
import app.polaroam.domain.model.WalkQuest
import java.time.Instant
import java.time.LocalDate

internal fun Quest.toEntity(walkId: String, position: Int) = QuestEntity(
    id = id,
    walkId = walkId,
    position = position,
    title = title,
    objective = objective,
    category = category.name,
    difficulty = difficulty.level,
    targetLabels = targetLabels,
    status = QuestStatus.OPEN.name,
    attempts = 0,
    poolKey = poolKey,
)

private fun questOf(
    id: String,
    title: String,
    objective: String,
    category: String,
    difficulty: Int,
    targetLabels: List<String>,
    poolKey: String?,
) = Quest(
    id = id,
    title = title,
    objective = objective,
    category = QuestCategory.fromWire(category) ?: QuestCategory.OBJECTS,
    difficulty = Difficulty.fromLevel(difficulty) ?: Difficulty.EASY,
    targetLabels = targetLabels,
    poolKey = poolKey,
)

internal fun QuestEntity.toQuest() = questOf(id, title, objective, category, difficulty, targetLabels, poolKey)

internal fun QuestEntity.toWalkQuest(completion: CompletionEntity?) = WalkQuest(
    quest = toQuest(),
    position = position,
    status = QuestStatus.entries.firstOrNull { it.name == status } ?: QuestStatus.OPEN,
    attempts = attempts,
    completion = completion?.toDomain(),
)

internal fun WalkEntity.toDomain(quests: List<WalkQuest>) = Walk(
    id = id,
    walkType = walkType,
    startedAt = Instant.ofEpochMilli(startedAtEpochMs),
    source = QuestSource.entries.firstOrNull { it.name == source } ?: QuestSource.BUILT_IN,
    quests = quests,
)

internal fun CompletionEntity.toDomain() = Completion(
    questId = questId,
    photoPath = photoPath,
    points = points,
    firstTry = firstTry,
    label = label,
    confidence = confidence,
    completedAt = Instant.ofEpochMilli(completedAtEpochMs),
    day = LocalDate.parse(localDay),
)

internal fun Completion.toEntity() = CompletionEntity(
    questId = questId,
    photoPath = photoPath,
    points = points,
    firstTry = firstTry,
    label = label,
    confidence = confidence,
    completedAtEpochMs = completedAt.toEpochMilli(),
    localDay = day.toString(),
)

internal fun GalleryRow.toDomain() = CompletedQuest(
    quest = questOf(questId, title, objective, category, difficulty, targetLabels, poolKey),
    walkType = walkType,
    completion = Completion(
        questId = questId,
        photoPath = photoPath,
        points = points,
        firstTry = firstTry,
        label = label,
        confidence = confidence,
        completedAt = Instant.ofEpochMilli(completedAtEpochMs),
        day = LocalDate.parse(localDay),
    ),
)

internal fun ProgressEntity.toDomain() = ProgressState(
    totalScore = totalScore,
    streak = StreakState(
        current = currentStreak,
        longest = longestStreak,
        lastActiveDay = lastActiveDay?.let(LocalDate::parse),
    ),
)

internal fun ProgressState.toEntity() = ProgressEntity(
    totalScore = totalScore,
    currentStreak = streak.current,
    longestStreak = streak.longest,
    lastActiveDay = streak.lastActiveDay?.toString(),
)
