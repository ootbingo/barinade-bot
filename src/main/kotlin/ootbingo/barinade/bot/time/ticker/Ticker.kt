package ootbingo.barinade.bot.time.ticker

import kotlin.time.Duration

interface Ticker {

  fun start()
  val elapsedTime: Duration
}
