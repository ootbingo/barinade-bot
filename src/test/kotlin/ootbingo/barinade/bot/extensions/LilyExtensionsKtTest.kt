package ootbingo.barinade.bot.extensions

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class LilyExtensionsKtTest {

  @Test
  internal fun returnsRacetimeUsername() {

    val username = UUID.randomUUID().toString()

    val info = RacetimeMessageInfo(ChatMessage(user = RacetimeUser("abc", username)))

    assertThat(info.getUsername()).isEqualTo(username)
  }
}
