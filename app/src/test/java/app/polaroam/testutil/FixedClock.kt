package app.polaroam.testutil

import app.polaroam.core.time.AppClock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class FixedClock(
    var instant: Instant = Instant.parse("2026-10-10T10:00:00Z"),
    var zoneId: ZoneId = ZoneOffset.UTC,
) : AppClock {
    override fun now(): Instant = instant
    override fun zone(): ZoneId = zoneId

    fun advance(duration: Duration) {
        instant = instant.plus(duration)
    }
}
