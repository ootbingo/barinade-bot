package ootbingo.barinade.bot.time

import org.springframework.stereotype.Component
import kotlin.time.Duration

@Component
class SleepFunction {

  @Suppress("NOTHING_TO_INLINE") // Needed for mocking
  final inline operator fun invoke(duration: Duration) {
    sleep(duration.inWholeMilliseconds)
  }

  fun sleep(millis: Long) {
    Thread.sleep(millis)
  }
}
