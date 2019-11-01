package ootbingo.barinade.bot.modules

import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.discord.connection.DiscordMessageInfo
import de.scaramanga.lily.irc.connection.IrcMessageInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.UserImpl
import ootbingo.barinade.bot.data.PlayerRepository
import ootbingo.barinade.bot.model.Player
import ootbingo.barinade.bot.model.Race
import ootbingo.barinade.bot.model.RaceResult
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

internal class BingoStatModuleTest {

  private val playerRepositoryMock = mock(PlayerRepository::class.java)
  private val module = BingoStatModule(playerRepositoryMock)
  private val players = mutableMapOf<String, Player>()

  private val commands by lazy {
    mapOf(Pair("average", module::average))
  }

  @BeforeEach
  internal fun setup() {
    doAnswer { players[it.getArgument(0)] }
        .`when`(playerRepositoryMock).getPlayerByName(ArgumentMatchers.anyString())
  }

  @Test
  internal fun computesCorrectAverageDiscord() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1000, 1200, 800, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 999)

    val answer = whenDiscordMessageIsSent(username, "!average")

    thenReportedTimeIsEqualTo(answer, "0:16:40")
  }

  @Test
  internal fun computesCorrectAverageIrc() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 10000, 12000, 8000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 999)

    val answer = whenIrcMessageIsSent(username, "!average")

    thenReportedTimeIsEqualTo(answer, "2:46:40")
  }

  @Test
  internal fun ignoresNonBingoTimesForAverage() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 10000)
    givenNonBingoTimesForPlayer(username, 30000)

    val answer = whenIrcMessageIsSent(username, "!average")

    thenReportedTimeIsEqualTo(answer, "2:46:40")
  }

  @Test
  internal fun errorWhenNoMessageInfo() {

    val answer = whenMessageIsSent("!average", MessageInfo.empty())

    thenErrorIsReported(answer)
  }

  private fun givenBingoTimesForPlayer(username: String, vararg times: Int) {
    givenTimesForPlayer(username, true, *times)
  }

  private fun givenNonBingoTimesForPlayer(username: String, vararg times: Int) {
    givenTimesForPlayer(username, false, *times)
  }

  private fun givenTimesForPlayer(username: String, bingo: Boolean, vararg times: Int) {

    val races = ArrayList<Race>()

    var timestamp = Random.nextLong(99999, 123456789)

    times
        .map {
          RaceResult(Race("", "", ZonedDateTime.now(), 1, emptyList()),
                     Player(0, username, emptyList()), 1, Duration.ofSeconds(it.toLong()), "")
        }
        .map {
          Race("0", "", ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp--), ZoneId.of("UTC")), 1, listOf(it))
        }
        .map {
          val spy = spy(it)
          `when`(spy.isBingo()).thenReturn(bingo)
          spy
        }
        .forEach { races.add(it) }

    val oldPlayer = players[username] ?: Player(0, username, races)
    val player = oldPlayer.copy(races = oldPlayer.races + races)

    players[username] = player
  }

  private fun whenMessageIsSent(message: String, messageInfo: MessageInfo): Answer<AnswerInfo> {

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    return commands.getValue(command).invoke(generateCommand(message, messageInfo))
  }

  private fun whenDiscordMessageIsSent(user: String, message: String): Answer<AnswerInfo> {

    val discordUser = UserImpl(0, mock(JDAImpl::class.java))
    discordUser.name = user

    val discordMessageMock = mock(Message::class.java)
    `when`(discordMessageMock.author).thenReturn(discordUser)

    return whenMessageIsSent(message, DiscordMessageInfo.withMessage(discordMessageMock))
  }

  private fun whenIrcMessageIsSent(username: String, message: String): Answer<AnswerInfo> {

    val messageInfoMock = mock(IrcMessageInfo::class.java)
    `when`(messageInfoMock.nick).thenReturn(username)
    `when`(messageInfoMock.channel).thenReturn("")

    return whenMessageIsSent(message, messageInfoMock)
  }

  private fun thenReportedTimeIsEqualTo(answer: Answer<AnswerInfo>, time: String) {

    val actualTime = answer.text.split(": ", limit = 2)[1]

    assertThat(actualTime).isEqualTo(time)
  }

  private fun thenErrorIsReported(answer: Answer<AnswerInfo>) {
    assertThat(answer.text).contains("error")
  }

  private fun generateCommand(message: String, messageInfo: MessageInfo): Command {

    val parts = message.substring(1).split(" ")

    return object : Command {
      override fun getMessageInfo(): MessageInfo {
        return messageInfo
      }

      override fun getArgument(n: Int): String {
        return parts[n + 1]
      }

      override fun getName(): String {
        return parts[0]
      }

      override fun getArgumentCount(): Int {
        return parts.size - 1
      }
    }
  }
}