package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class DiscordRaceRoomManagerTest {

  private val manager = DiscordRaceRoomManager()

  @Test
  internal fun addAndGetRaceRoom() {

    val channel = mock<TextChannel>()
    val room = TestRaceRoom(channel)

    manager.addRaceRoom(channel, room)

    assertThat(manager.getRaceRoomForChannel(channel)).isInstanceOf(TestRaceRoom::class.java)
  }

  @Test
  internal fun getUnknownRoom() {
    assertThat(manager.getRaceRoomForChannel(mock())).isNull()
  }

  @Test
  internal fun addRoomOnlyOnce() {

    val channel = mock<TextChannel>()
    val room1 = TestRaceRoom(channel)
    val room2 = TestRaceRoom(channel)

    manager.addRaceRoom(channel, room1)
    assertThatCode { manager.addRaceRoom(channel, room2) }.isExactlyInstanceOf(IllegalArgumentException::class.java)
  }

  private class TestRaceRoom(discordChannel: TextChannel) : DiscordRaceRoom(
      mock(),
      discordChannel,
      {},
      {},
      mock(),
  ) {

    override fun readyToStart() = true
    override fun done(entrant: User): String? = null
  }
}
