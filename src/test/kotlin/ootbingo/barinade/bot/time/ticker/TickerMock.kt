package ootbingo.barinade.bot.time.ticker

import org.junit.jupiter.api.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class TickerMock : Ticker {

  var started = false
    private set

  private var elapsedNanos = 0L

  fun advanceBy(duration: Duration) {
    elapsedNanos += duration.toLong(DurationUnit.NANOSECONDS)
  }

  override fun start() {
    started = true
  }

  override val elapsedTime: Duration
    get() = if (started) elapsedNanos.nanoseconds else fail("Ticker must be started before querying time")
}
