package app.polaroam.ui.preview

import app.polaroam.ai.canned.CannedQuestGenerator
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.scoring.ScoreCalculator
import app.polaroam.ui.board.QuestBoardUiState
import app.polaroam.ui.board.QuestCardUi
import app.polaroam.ui.gallery.GalleryItemUi
import java.time.LocalDate
import kotlin.random.Random

/** Static data for Compose previews, built from the canned generator. */
object PreviewData {
    private val scoring = ScoreCalculator()
    private var nextId = 0

    val quests = CannedQuestGenerator(random = Random(7), idFactory = { "preview-${nextId++}" })
        .pick("City park", 4)

    val boardState = QuestBoardUiState.Loaded(
        walkType = "City park",
        source = QuestSource.BUILT_IN,
        cards = quests.mapIndexed { index, quest ->
            val status = when (index) {
                0 -> QuestStatus.COMPLETED
                1 -> QuestStatus.SKIPPED
                else -> QuestStatus.OPEN
            }
            QuestCardUi(
                questId = quest.id,
                poolKey = quest.poolKey,
                title = quest.title,
                objective = quest.objective,
                category = quest.category,
                difficulty = quest.difficulty,
                basePoints = scoring.basePoints(quest.difficulty),
                firstTryBonus = ScoreCalculator.FIRST_TRY_BONUS,
                status = status,
                earnedPoints = if (status == QuestStatus.COMPLETED) scoring.pointsFor(quest.difficulty, 1) else null,
            )
        },
        completedCount = 1,
        totalCount = quests.size,
        earnedPoints = scoring.pointsFor(quests.first().difficulty, 1),
        isFinished = false,
    )

    val galleryItems = quests.mapIndexed { index, quest ->
        GalleryItemUi(
            questId = quest.id,
            title = quest.title,
            walkType = "City park",
            photoPath = "",
            points = scoring.basePoints(quest.difficulty),
            day = LocalDate.of(2026, 10, 10),
            tiltDegrees = (index % 3 - 1) * 1.5f,
        )
    }
}
