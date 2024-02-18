package ootbingo.barinade.bot.time.ticker

import kotlinx.datetime.Clock
import org.springframework.stereotype.Component

@Component
class TickerFactory(private val clock: Clock) {

  fun createTicker() = DefaultTicker(clock)
}
