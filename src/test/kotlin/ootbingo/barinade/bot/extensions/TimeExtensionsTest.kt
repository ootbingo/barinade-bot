package ootbingo.barinade.bot.extensions

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import java.time.Duration

class TimeExtensionsTest {

  @Test
  internal fun convertsDurationToCorrectStandardFormat() {

    val durations = mapOf(Pair(Duration.ofDays(2).plusSeconds(5), "48:00:05"),
                          Pair(Duration.ofHours(7).plusMinutes(77), "8:17:00"),
                          Pair(Duration.ofSeconds(100), "0:01:40"))

    val soft = SoftAssertions()

    durations.forEach {
      soft.assertThat(it.key.standardFormat()).isEqualTo(it.value)
    }

    soft.assertAll()
  }
}
