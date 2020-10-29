package ootbingo.barinade.bot.extensions

import java.time.Duration

fun Duration.standardFormat(): String {
  return String.format("%d:%02d:%02d", this.toHours(), this.toMinutesPart(), this.toSecondsPart())
}
