package ootbingo.barinade.bot.racing_services.bingosync

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class BingosyncRoomConfigTest {

  @Test
  internal fun buildCorrectPostString() {

    val roomName = UUID.randomUUID().toString()
    val roomPassword = "secure"
    val roomVariant = BingosyncRoomConfig.Variant.BLACKOUT
    val roomSeed = 42

    val actualPayload = bingosyncRoomConfig {
      name = roomName
      password = roomPassword
      variant = roomVariant
      seed = roomSeed
      lockout = true
      hideCard = false
    }.toHttpPayload()

    val expectedPayload = buildString {

      fun and() = append("&")

      append("room_name=$roomName")
      and()
      append("passphrase=$roomPassword")
      and()
      append("nickname=BingoBot")
      and()
      append("game_type=1")
      and()
      append("variant_type=${roomVariant.id}")
      and()
      append("lockout_mode=2")
      and()
      append("seed=$roomSeed")
      and()
      append("is_spectator=on")
    }

    assertThat(actualPayload).isEqualTo(expectedPayload)
  }
}
