package app.polaroam.domain.usecase

import android.graphics.Bitmap
import app.polaroam.ai.vision.StubPhotoVerifier
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.model.Difficulty
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.QuestSource
import app.polaroam.domain.model.QuestStatus
import app.polaroam.domain.model.Verdict
import app.polaroam.domain.scoring.ScoreCalculator
import app.polaroam.domain.streak.StreakCalculator
import app.polaroam.testutil.FakePhotoStore
import app.polaroam.testutil.FakeProgressRepository
import app.polaroam.testutil.FakeWalkRepository
import app.polaroam.testutil.FixedClock
import app.polaroam.testutil.testQuest
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SubmitPhotoUseCaseTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var walks: FakeWalkRepository
    private lateinit var progress: FakeProgressRepository
    private lateinit var photos: FakePhotoStore
    private val clock = FixedClock()

    @Before
    fun setUp() {
        walks = FakeWalkRepository()
        progress = FakeProgressRepository(walks)
        photos = FakePhotoStore(tmp.root)
    }

    private fun useCase(verifier: PhotoVerifier) = SubmitPhotoUseCase(
        verifier = verifier,
        walks = walks,
        progress = progress,
        photos = photos,
        scoring = ScoreCalculator(),
        streaks = StreakCalculator(),
        clock = clock,
    )

    private suspend fun startWalk(vararg quests: Quest) {
        walks.createWalk("City park", QuestSource.BUILT_IN, quests.toList(), clock.now())
    }

    @Test
    fun `first-try match awards base points plus bonus and completes the quest`() = runTest {
        startWalk(testQuest("q1", Difficulty.MEDIUM))
        val capture = photos.newCaptureFile()

        val result = useCase(StubPhotoVerifier.alwaysMatch())("q1", capture)

        assertThat(result).isInstanceOf(SubmitResult.Match::class.java)
        result as SubmitResult.Match
        assertThat(result.points).isEqualTo(25)
        assertThat(result.firstTry).isTrue()
        assertThat(result.streak).isEqualTo(1)
        assertThat(result.verdict.label).isEqualTo("dog")
        assertThat(photos.persisted).hasSize(1)
        assertThat(walks.findQuest("q1")?.status).isEqualTo(QuestStatus.COMPLETED)
        assertThat(progress.state.value.totalScore).isEqualTo(25)
    }

    @Test
    fun `no match records the attempt and discards the photo`() = runTest {
        startWalk(testQuest("q1"))
        val capture = photos.newCaptureFile()

        val result = useCase(StubPhotoVerifier.neverMatch())("q1", capture)

        assertThat(result).isEqualTo(
            SubmitResult.NoMatch(
                Verdict(false, StubPhotoVerifier.MISS_CONFIDENCE, StubPhotoVerifier.MISS_LABEL),
                attempt = 1,
            ),
        )
        assertThat(walks.findQuest("q1")?.attempts).isEqualTo(1)
        assertThat(walks.findQuest("q1")?.status).isEqualTo(QuestStatus.OPEN)
        assertThat(photos.discarded).containsExactly(capture)
        assertThat(progress.completions).isEmpty()
    }

    @Test
    fun `match after a miss gets no first-try bonus`() = runTest {
        startWalk(testQuest("q1", Difficulty.HARD))
        val submit = useCase(StubPhotoVerifier(listOf(false, true)))

        submit("q1", photos.newCaptureFile())
        val result = submit("q1", photos.newCaptureFile())

        result as SubmitResult.Match
        assertThat(result.points).isEqualTo(30)
        assertThat(result.firstTry).isFalse()
        assertThat(walks.findQuest("q1")?.attempts).isEqualTo(2)
    }

    @Test
    fun `completed quest cannot be submitted again`() = runTest {
        startWalk(testQuest("q1"))
        val submit = useCase(StubPhotoVerifier.alwaysMatch())
        submit("q1", photos.newCaptureFile())
        val second = photos.newCaptureFile()

        val result = submit("q1", second)

        assertThat(result).isEqualTo(SubmitResult.QuestUnavailable)
        assertThat(photos.discarded).contains(second)
        assertThat(progress.completions).hasSize(1)
    }

    @Test
    fun `skipped and unknown quests are unavailable`() = runTest {
        startWalk(testQuest("q1"))
        walks.skipQuest("q1")
        val submit = useCase(StubPhotoVerifier.alwaysMatch())

        assertThat(submit("q1", photos.newCaptureFile())).isEqualTo(SubmitResult.QuestUnavailable)
        assertThat(submit("missing", photos.newCaptureFile())).isEqualTo(SubmitResult.QuestUnavailable)
    }

    @Test
    fun `undecodable photo fails without counting an attempt`() = runTest {
        startWalk(testQuest("q1"))
        photos.failDecode = true
        val capture = photos.newCaptureFile()

        val result = useCase(StubPhotoVerifier.alwaysMatch())("q1", capture)

        assertThat(result).isInstanceOf(SubmitResult.Failed::class.java)
        assertThat(walks.findQuest("q1")?.attempts).isEqualTo(0)
        assertThat(photos.discarded).containsExactly(capture)
    }

    @Test
    fun `verifier crash is reported as a failure`() = runTest {
        startWalk(testQuest("q1"))
        val crashing = object : PhotoVerifier {
            override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict = error("model crashed")
        }

        val result = useCase(crashing)("q1", photos.newCaptureFile())

        assertThat(result).isInstanceOf(SubmitResult.Failed::class.java)
        assertThat(walks.findQuest("q1")?.status).isEqualTo(QuestStatus.OPEN)
    }

    @Test
    fun `failed save removes the persisted photo`() = runTest {
        startWalk(testQuest("q1"))
        progress.failOnRecord = true

        val result = useCase(StubPhotoVerifier.alwaysMatch())("q1", photos.newCaptureFile())

        assertThat(result).isInstanceOf(SubmitResult.Failed::class.java)
        assertThat(photos.discarded).containsExactlyElementsIn(photos.persisted)
        assertThat(progress.state.value.totalScore).isEqualTo(0)
    }

    @Test
    fun `scores accumulate across quests`() = runTest {
        startWalk(testQuest("q1", Difficulty.EASY), testQuest("q2", Difficulty.HARD))
        val submit = useCase(StubPhotoVerifier.alwaysMatch())

        submit("q1", photos.newCaptureFile())
        submit("q2", photos.newCaptureFile())

        assertThat(progress.state.value.totalScore).isEqualTo(15 + 35)
        assertThat(progress.state.value.streak.current).isEqualTo(1)
    }

    @Test
    fun `streak uses the local calendar day`() = runTest {
        clock.zoneId = ZoneId.of("Asia/Tokyo")
        // 23:30 local on Oct 10
        clock.instant = Instant.parse("2026-10-10T14:30:00Z")
        startWalk(testQuest("q1"), testQuest("q2"))
        val submit = useCase(StubPhotoVerifier.alwaysMatch())

        submit("q1", photos.newCaptureFile())
        // 00:30 local on Oct 11, one hour later
        clock.instant = Instant.parse("2026-10-10T15:30:00Z")
        val result = submit("q2", photos.newCaptureFile())

        result as SubmitResult.Match
        assertThat(result.streak).isEqualTo(2)
    }
}
