package ootbingo.barinade.bot.data.model

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.random.Random

internal class PlayerTest {

  @Test
  internal fun equalsBySrlId() {

    val id = Random.nextLong()

    val player1 = Player(id, "name", mutableListOf())
    val player2 = Player(id, "other name", mutableListOf(RaceResult()))

    assertThat(player1).isEqualTo(player2)
  }

  @Test
  internal fun notEqualWhenSrlIdDiffers() {

    val name = UUID.randomUUID().toString()
    val results = mutableListOf(RaceResult())

    val player1 = Player(42, name, results)
    val player2 = Player(43, name, results)

    assertThat(player1).isNotEqualTo(player2)
  }
}
