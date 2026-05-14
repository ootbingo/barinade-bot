package ootbingo.barinade.bot.time.ticker

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class DefaultTicker(private val clock: Clock) : Ticker {

  private lateinit var startTime: Instant

  override fun start() {
    startTime = clock.now()
  }

  override val elapsedTime: Duration
    get() = clock.now() - startTime
}
