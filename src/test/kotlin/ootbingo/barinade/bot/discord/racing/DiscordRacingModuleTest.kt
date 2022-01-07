package ootbingo.barinade.bot.discord.racing

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import java.util.*
import kotlin.random.Random
import kotlin.streams.asStream

internal class DiscordRacingModuleTest : ModuleTest() {

  //<editor-fold desc="Setup">

  private val discordRaceRoomManagerMock = mock<DiscordRaceRoomManager>()

  private val module = DiscordRacingModule(discordRaceRoomManagerMock)

  override val commands: Map<String, (Command) -> Answer<AnswerInfo>?> =
      mapOf(
          "enter" to module::enter,
          "unenter" to module::unenter,
          "ready" to module::ready,
          "unready" to module::unready,
          "start" to module::start,
      )

  companion object {

    @JvmStatic
    private fun raceRoomCommands() = sequenceOf(
        "!enter" to DiscordRaceRoom::enter,
        "!unenter" to DiscordRaceRoom::unenter,
        "!ready" to DiscordRaceRoom::ready,
        "!unready" to DiscordRaceRoom::unready,
        "!start" to DiscordRaceRoom::start,
    ).asStream()
  }

  //</editor-fold>

  @ParameterizedTest
  @MethodSource("raceRoomCommands")
  internal fun forwardsCommands(commandMapping: Pair<String, RaceRoomCommand>) {

    val raceRoom = mock<DiscordRaceRoom>()

    val user = mock<User>()
    val userId = Random.nextLong()
    val userTag = UUID.randomUUID().toString()

    whenever(user.idLong).thenReturn(userId)
    whenever(user.asTag).thenReturn(userTag)

    givenManagerReturnsRaceRoom(raceRoom)

    whenDiscordMessageIsSent(user, commandMapping.first, mock<TextChannel>())

    thenCommand(commandMapping.second) isInvokedIn raceRoom on DiscordEntrant(userId, userTag)
  }

  @ParameterizedTest
  @MethodSource("raceRoomCommands")
  internal fun doesNothingIfChannelIsNoTextChannel(commandMapping: Pair<String, RaceRoomCommand>) {

    val raceRoom = mock<DiscordRaceRoom>()

    givenManagerReturnsRaceRoom(raceRoom)

    whenDiscordMessageIsSent(mock<User>(), commandMapping.first)

    thenNoActionIsTaken()
  }

  @ParameterizedTest
  @MethodSource("raceRoomCommands")
  internal fun doesNothingIfChannelIsNoRaceRoomFound(commandMapping: Pair<String, RaceRoomCommand>) {

    givenManagerReturnsRaceRoom(null)

    whenDiscordMessageIsSent(mock<User>(), commandMapping.first, mock<TextChannel>())

    thenAnswerIsSent(null)
  }

  @ParameterizedTest
  @MethodSource("raceRoomCommands")
  internal fun forwardsAnswerFromRaceRoom(commandMapping: Pair<String, RaceRoomCommand>) {

    val raceRoom = mock<DiscordRaceRoom>()
    val answer = UUID.randomUUID().toString()

    val user = mock<User>()
    val userId = Random.nextLong()
    val userTag = UUID.randomUUID().toString()

    whenever(user.idLong).thenReturn(userId)
    whenever(user.asTag).thenReturn(userTag)

    givenManagerReturnsRaceRoom(raceRoom)
    givenInvocationOf(commandMapping.second) by user inChannel raceRoom returns answer

    whenDiscordMessageIsSent(user, commandMapping.first, mock<TextChannel>())

    thenAnswerIsSent(answer)
  }

  //<editor-fold desc="Given">

  private fun givenManagerReturnsRaceRoom(raceRoom: DiscordRaceRoom?) {
    whenever(discordRaceRoomManagerMock.getRaceRoomForChannel(any())).thenReturn(raceRoom)
  }

  private fun givenInvocationOf(command: RaceRoomCommand) = command

  private infix fun RaceRoomCommand.by(user: User) = this to user

  private infix fun Pair<RaceRoomCommand, User>.inChannel(room: DiscordRaceRoom) = this to room

  private infix fun Pair<Pair<RaceRoomCommand, User>, DiscordRaceRoom>.returns(value: String) {
    whenever(first.first.invoke(second, DiscordEntrant(first.second))).thenReturn(value)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenCommand(command: RaceRoomCommand) = command

  private infix fun RaceRoomCommand.isInvokedIn(raceRoom: DiscordRaceRoom) =
      this to raceRoom

  private infix fun Pair<RaceRoomCommand, DiscordRaceRoom>.on(entrant: DiscordEntrant) {
    verify(this.second).apply { this@on.first.invoke(this, entrant) }
  }

  private fun thenNoActionIsTaken() {
    verifyNoInteractions(discordRaceRoomManagerMock)
    assertThat(answer).isNull()
  }

  private fun thenAnswerIsSent(expectedAnswer: String?) {
    assertThat(answer).isEqualTo(expectedAnswer)
  }

  //</editor-fold>
}
