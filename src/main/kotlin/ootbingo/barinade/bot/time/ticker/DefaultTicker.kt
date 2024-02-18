package ootbingo.barinade.bot.time.ticker

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

class DefaultTicker(private val clock: Clock) : Ticker {

  private lateinit var startTime: Instant

  override fun start() {
    startTime = clock.now()
  }

  override val elapsedTime: Duration
    get() = clock.now() - startTime
}
