package app.polaroam.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.polaroam.data.db.PolaroamDatabase
import app.polaroam.data.repository.RoomProgressRepository
import app.polaroam.data.repository.RoomWalkRepository
import app.polaroam.domain.model.Completion
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestCategory
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomRepositoriesTest {

    private lateinit var db: PolaroamDatabase
    private lateinit var walks: RoomWalkRepository
    private lateinit var progress: RoomProgressRepository

    private val quests = listOf(
        Quest("q1", "Bench Story", "Find a bench with character.", QuestCategory.OBJECTS, Difficulty.EASY, listOf("bench", "wood")),
        Quest("q2", "Duck Patrol", "Photograph a duck on the water.", QuestCategory.ANIMALS, Difficulty.MEDIUM, listOf("duck", "bird")),
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), PolaroamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        walks = RoomWalkRepository(db.walkDao())
        progress = RoomProgressRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun completion(questId: String, points: Int) = Completion(
        questId = questId,
        photoPath = "/photos/$questId.jpg",
        points = points,
        firstTry = true,
        label = "bench",
        confidence = 0.9f,
        completedAt = Instant.parse("2026-10-10T10:00:00Z"),
        day = LocalDate.of(2026, 10, 10),
    )

    @Test
    fun walkRoundTripKeepsQuestOrderAndLabels() = runTest {
        val walkId = walks.createWalk("City park", QuestSource.MODEL, quests, Instant.EPOCH)

        val walk = walks.observeWalk(walkId).first()!!

        assertThat(walk.source).isEqualTo(QuestSource.MODEL)
        assertThat(walk.quests.map { it.quest.id }).containsExactly("q1", "q2").inOrder()
        assertThat(walk.quests[1].quest.targetLabels).containsExactly("duck", "bird").inOrder()
        assertThat(walk.quests.all { it.status == QuestStatus.OPEN }).isTrue()
    }

    @Test
    fun recordCompletionUpdatesQuestProgressAndGallery() = runTest {
        walks.createWalk("City park", QuestSource.BUILT_IN, quests, Instant.EPOCH)

        val state = progress.recordCompletion(completion("q1", 15), attempts = 1) { it.copy(totalScore = it.totalScore + 15) }

        assertThat(state.totalScore).isEqualTo(15)
        assertThat(walks.getWalkQuest("q1")?.status).isEqualTo(QuestStatus.COMPLETED)
        assertThat(progress.observeProgress().first().questsCompleted).isEqualTo(1)
        assertThat(walks.observeCompletedQuests().first().single().quest.title).isEqualTo("Bench Story")
    }

    @Test
    fun completingTwiceRollsBackTheSecondAttempt() = runTest {
        walks.createWalk("City park", QuestSource.BUILT_IN, quests, Instant.EPOCH)
        progress.recordCompletion(completion("q1", 15), attempts = 1) { it.copy(totalScore = it.totalScore + 15) }

        try {
            progress.recordCompletion(completion("q1", 15), attempts = 2) { it.copy(totalScore = it.totalScore + 15) }
            fail("Expected the second completion to be rejected")
        } catch (expected: IllegalStateException) {
            // rejected as intended
        }

        assertThat(progress.observeProgress().first().state.totalScore).isEqualTo(15)
        assertThat(progress.observeProgress().first().questsCompleted).isEqualTo(1)
    }

    @Test
    fun failedAttemptsAndSkipsOnlyAffectOpenQuests() = runTest {
        walks.createWalk("City park", QuestSource.BUILT_IN, quests, Instant.EPOCH)

        assertThat(walks.recordFailedAttempt("q2")).isTrue()
        assertThat(walks.skipQuest("q2")).isTrue()
        assertThat(walks.recordFailedAttempt("q2")).isFalse()
        assertThat(walks.skipQuest("q2")).isFalse()
        assertThat(walks.getWalkQuest("q2")?.attempts).isEqualTo(1)
    }
}
