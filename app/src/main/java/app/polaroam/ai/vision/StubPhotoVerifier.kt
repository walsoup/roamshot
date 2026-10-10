package app.polaroam.ai.vision

import android.graphics.Bitmap
import app.polaroam.domain.ai.PhotoVerifier
import app.polaroam.domain.model.Quest
import app.polaroam.domain.model.Verdict
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay

/**
 * Deterministic verifier for tests, previews and devices without the real model.
 * Answers follow [script] in order and repeat the last entry once it runs out.
 */
class StubPhotoVerifier(
    private val script: List<Boolean> = listOf(true),
    private val delayMillis: Long = 0L,
) : PhotoVerifier {

    init {
        require(script.isNotEmpty()) { "script must not be empty" }
    }

    private val calls = AtomicInteger(0)

    val callCount: Int get() = calls.get()

    override suspend fun verify(bitmap: Bitmap, quest: Quest): Verdict {
        if (delayMillis > 0) delay(delayMillis)
        val index = calls.getAndIncrement().coerceAtMost(script.lastIndex)
        return if (script[index]) {
            Verdict(match = true, confidence = MATCH_CONFIDENCE, label = quest.targetLabels.first())
        } else {
            Verdict(match = false, confidence = MISS_CONFIDENCE, label = MISS_LABEL)
        }
    }

    companion object {
        const val MATCH_CONFIDENCE = 0.92f
        const val MISS_CONFIDENCE = 0.41f
        const val MISS_LABEL = "wall"

        fun alwaysMatch(delayMillis: Long = 0L) = StubPhotoVerifier(listOf(true), delayMillis)
        fun neverMatch(delayMillis: Long = 0L) = StubPhotoVerifier(listOf(false), delayMillis)
    }
}
