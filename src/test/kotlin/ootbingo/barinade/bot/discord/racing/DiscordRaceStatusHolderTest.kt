package ootbingo.barinade.bot.discord.racing

import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.connection.DiscordPlayerRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceEntryRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceRepository
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState.*
import ootbingo.barinade.bot.discord.data.model.DiscordRaceType
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.*
import kotlin.random.Random

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class DiscordRaceStatusHolderTest(
    @Autowired private val playerRepository: DiscordPlayerRepository,
    @Autowired private val raceRepository: DiscordRaceRepository,
    @Autowired private val entryRepository: DiscordRaceEntryRepository,
) {

  private val holder = DiscordRaceStatusHolder(
      playerRepository,
      raceRepository,
      entryRepository,
      Json,
      mock<TextChannel> {
        whenever(it.name).thenReturn("")
        whenever(it.idLong).thenReturn(Random.nextLong(0, Long.MAX_VALUE))
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

  @Test
  internal fun addAdditionalInfo() {

    val key1 = UUID.randomUUID().toString()
    val value1 = UUID.randomUUID().toString()
    val key2 = UUID.randomUUID().toString()
    val value2 = UUID.randomUUID().toString()

    holder.addAdditionalInfo(key1, value1)
    holder.addAdditionalInfo(key2, value2)

    Json.decodeFromString<Map<String, String>>(
        checkNotNull(raceRepository.findById(holder.raceId)?.additionalInfo)
    ).run {
      assertThat(this[key1]).isEqualTo(value1)
      assertThat(this[key2]).isEqualTo(value2)
      assertThat(this.size).isEqualTo(2)
    }
  }

  private fun randomUser() = mock<User>()
      .apply {
        val name = UUID.randomUUID().toString()
        whenever(idLong).thenReturn(Random.nextLong())
        whenever(asTag).thenReturn("@$name")
        whenever(this.name).thenReturn(name)
      }
}
