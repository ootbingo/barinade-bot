package ootbingo.barinade.bot.discord.racing

import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.EntrantStatus.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

@Suppress("UsePropertyAccessSyntax")
internal class DiscordRaceStatusHolderTest {

  private val holder = DiscordRaceStatusHolder()

  @Test
  internal fun addEntrant() {

    val entrant = randomEntrant()

    assertThat(holder.addEntrant(entrant)).isTrue()
    assertThat(holder.addEntrant(entrant)).isFalse()
    assertThat(holder.addEntrant(randomEntrant())).isTrue()
    assertThat(holder.addEntrant(entrant)).isFalse()
    holder.removeEntrant(entrant)
    assertThat(holder.addEntrant(entrant)).isTrue()
  }

  @Test
  internal fun removeEntrant() {

    val entrant = randomEntrant()

    assertThat(holder.removeEntrant(entrant)).isFalse()
    holder.addEntrant(entrant)
    assertThat(holder.removeEntrant(randomEntrant())).isFalse()
    assertThat(holder.removeEntrant(entrant)).isTrue()
    assertThat(holder.removeEntrant(entrant)).isFalse()
  }

  @Test
  internal fun changeState() {

    val entrant = randomEntrant()
    holder.addEntrant(entrant)

    assertThat(holder.setStatusForEntrant(entrant, NOT_READY)).isFalse()
    assertThat(holder.setStatusForEntrant(entrant, READY)).isTrue()
    assertThat(holder.setStatusForEntrant(entrant, READY)).isFalse()
  }

  @Test
  internal fun countAndChangeState() {

    val entrant1 = randomEntrant()
    val entrant2 = randomEntrant()

    assertThat(holder.countPerStatus()).isEmpty()

    holder.addEntrant(entrant1)
    holder.addEntrant(entrant2)
    assertThat(holder.countPerStatus()).containsExactlyInAnyOrderEntriesOf(mapOf(NOT_READY to 2))

    holder.setStatusForEntrant(entrant1, READY)
    assertThat(holder.countPerStatus()).containsExactlyInAnyOrderEntriesOf(mapOf(
        NOT_READY to 1,
        READY to 1,
    ))

    holder.setStatusForAll(UNDEFINED)
    assertThat(holder.countPerStatus()).containsExactlyInAnyOrderEntriesOf(mapOf(UNDEFINED to 2))
  }

  private fun randomEntrant() = DiscordEntrant(Random.nextLong(), "@${UUID.randomUUID()}")
}
