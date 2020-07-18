package ootbingo.barinade.bot.data.model

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.*
import java.time.Duration

internal class RaceResultTest {

  @ParameterizedTest
  @ValueSource(longs = [-1, -2, -5, -99, -1000])
  internal fun isForfeitWhenTimeIsNegative(time: Long) {

    val result = RaceResult(RaceResult.ResultId(mock(Race::class.java), Player(0, "", mutableListOf())),
                            999, Duration.ofSeconds(time))

    assertThat(result.isForfeit()).isTrue()
  }
}
