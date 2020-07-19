package ootbingo.barinade.bot.data.model

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Duration

internal class RaceResultTest {

  @Test
  internal fun isForfeitWhenResultForfeit() {

    val result = RaceResult(RaceResult.ResultId(Race(), Player()), 999, Duration.ofSeconds(42), ResultType.FORFEIT)

    assertThat(result.isForfeit()).isTrue()
  }

  @Test
  internal fun isForfeitWhenResultDQ() {

    val result = RaceResult(RaceResult.ResultId(Race(), Player()), 999, Duration.ofSeconds(42), ResultType.DQ)

    assertThat(result.isForfeit()).isTrue()
  }

  @Test
  internal fun isNotForfeitWhenResultFinish() {

    val result = RaceResult(RaceResult.ResultId(Race(), Player()), 999, Duration.ofSeconds(42), ResultType.FINISH)

    assertThat(result.isForfeit()).isFalse()
  }
}
