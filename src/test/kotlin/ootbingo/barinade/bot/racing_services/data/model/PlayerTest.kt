package ootbingo.barinade.bot.racing_services.data.model

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

internal class PlayerTest {

  @Test
  internal fun equalsById() {

    val id = Random.nextLong()

    val player1 = Player(id, null, null, "name", null, mutableListOf())
    val player2 = Player(id, null, null, "other name", null, mutableListOf(RaceResult()))

    assertThat(player1).isEqualTo(player2)
  }

  @Test
  internal fun notEqualWhenIdDiffers() {

    val name = UUID.randomUUID().toString()
    val results = mutableListOf(RaceResult())

    val player1 = Player(42, null, null, name, null, results)
    val player2 = Player(43, null, null, name, null, results)

    assertThat(player1).isNotEqualTo(player2)
  }
}
