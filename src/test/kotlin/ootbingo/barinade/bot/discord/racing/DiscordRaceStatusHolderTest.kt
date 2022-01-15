package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.connection.DiscordPlayerRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceRepository
import ootbingo.barinade.bot.discord.data.model.DiscordPlayer
import ootbingo.barinade.bot.discord.data.model.DiscordRace
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState.*
import ootbingo.barinade.bot.discord.data.model.DiscordRaceType
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*
import kotlin.random.Random

@Disabled
@Suppress("UsePropertyAccessSyntax")
internal class DiscordRaceStatusHolderTest {

  private val playerRepositoryMock = mock<DiscordPlayerRepository>().apply {
    doAnswer {
      it.getArgument<User>(0)
          .let { u -> DiscordPlayer(u.idLong, u.asTag) }
    }.whenever(this).fromDiscordUser(any())
  }

  private val raceRepositoryMock = mock<DiscordRaceRepository>().apply {
    doAnswer {
      it.getArgument<DiscordRace>(0)
    }.whenever(this).save(any())
  }

  private val holder = DiscordRaceStatusHolder(
      playerRepositoryMock,
      raceRepositoryMock,
      mock(),
      mock<TextChannel>().apply {
        whenever(name).thenReturn("")
        whenever(idLong).thenReturn(0)
      },
      DiscordRaceType.GENERIC,
  )

  @Test
  internal fun addEntrant() {

    val entrant = randomUser()

    assertThat(holder.addEntrant(entrant)).isTrue()
    assertThat(holder.addEntrant(entrant)).isFalse()
    assertThat(holder.addEntrant(randomUser())).isTrue()
    assertThat(holder.addEntrant(entrant)).isFalse()
    holder.removeEntrant(entrant)
    assertThat(holder.addEntrant(entrant)).isTrue()
  }

  @Test
  internal fun removeEntrant() {

    val entrant = randomUser()

    assertThat(holder.removeEntrant(entrant)).isFalse()
    holder.addEntrant(entrant)
    assertThat(holder.removeEntrant(randomUser())).isFalse()
    assertThat(holder.removeEntrant(entrant)).isTrue()
    assertThat(holder.removeEntrant(entrant)).isFalse()
  }

  @Test
  internal fun changeState() {

    val entrant = randomUser()
    holder.addEntrant(entrant)

    assertThat(holder.setStatusForEntrant(entrant, NOT_READY)).isFalse()
    assertThat(holder.setStatusForEntrant(entrant, READY)).isTrue()
    assertThat(holder.setStatusForEntrant(entrant, READY)).isFalse()
  }

  @Test
  internal fun countAndChangeState() {

    val entrant1 = randomUser()
    val entrant2 = randomUser()

    assertThat(holder.countPerEntrantState()).isEmpty()

    holder.addEntrant(entrant1)
    holder.addEntrant(entrant2)
    assertThat(holder.countPerEntrantState()).containsExactlyInAnyOrderEntriesOf(mapOf(NOT_READY to 2))

    holder.setStatusForEntrant(entrant1, READY)
    assertThat(holder.countPerEntrantState()).containsExactlyInAnyOrderEntriesOf(mapOf(
        NOT_READY to 1,
        READY to 1,
    ))

    holder.setStatusForAll(FORFEIT)
    assertThat(holder.countPerEntrantState()).containsExactlyInAnyOrderEntriesOf(mapOf(FORFEIT to 2))
  }

  private fun randomUser() = mock<User>()
      .apply {
        val name = UUID.randomUUID().toString()
        whenever(idLong).thenReturn(Random.nextLong())
        whenever(asTag).thenReturn("@$name")
        whenever(this.name).thenReturn(name)
      }
}
