package ootbingo.barinade.bot.time.ticker

import org.springframework.stereotype.Component
import kotlin.time.Clock

@Component
class TickerFactory(private val clock: Clock) {

  fun createTicker() = DefaultTicker(clock)
}
