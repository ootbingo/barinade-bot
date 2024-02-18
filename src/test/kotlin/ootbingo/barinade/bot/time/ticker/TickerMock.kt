package ootbingo.barinade.bot.time.ticker

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
    get() = elapsedNanos.nanoseconds
}
