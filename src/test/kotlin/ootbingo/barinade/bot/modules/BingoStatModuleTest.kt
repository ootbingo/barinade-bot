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
    mapOf(Pair("average", module::average),
          Pair("median", module::median))
  }

  @BeforeEach
  internal fun setup() {
    doAnswer { players[it.getArgument(0)] }
        .`when`(playerRepositoryMock).getPlayerByName(ArgumentMatchers.anyString())
  }

  //<editor-fold desc="Average">

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
  internal fun computesAverageForDifferentUser() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1)

    val answer = whenDiscordMessageIsSent(requestUsername, "!average $queryUsername")

    thenReportedTimeIsEqualTo(answer, "0:00:01")
  }

  @Test
  internal fun computesAverageForDifferentAmountOfRaces() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 1, 1, 30000)

    val answer = whenDiscordMessageIsSent(username, "!average 3")

    thenReportedTimeIsEqualTo(answer, "0:00:01")
  }

  @Test
  internal fun computesAverageForPlayerAndRaceAmount() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1, 1, 1, 5)

    val answer = whenDiscordMessageIsSent(requestUsername, "!average $queryUsername 3")

    thenReportedTimeIsEqualTo(answer, "0:00:01")
  }

  @Test
  internal fun computesAverageForRaceAmountAndPlayer() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1, 1, 1, 5)

    val answer = whenDiscordMessageIsSent(requestUsername, "!average 3 $queryUsername")

    thenReportedTimeIsEqualTo(answer, "0:00:01")
  }

  @Test
  internal fun displaysCorrectAmountOfRacesForAverage() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 10000, 12000, 8000)

    val answer = whenIrcMessageIsSent(username, "!average 5")

    thenDisplayedNumberOfRacesIs(answer, 3)
  }

  @Test
  internal fun ignoresForfeitsForAverage() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, -2, -3)

    val answer = whenIrcMessageIsSent(username, "!average 5")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
    thenDisplayedNumberOfRacesIs(answer, 2)
  }

  @Test
  internal fun showsNumberOfIgnoredForfeitsForAverage() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    val answer = whenIrcMessageIsSent(username, "!average 3")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
    thenDisplayedNumberOfRacesIs(answer, 3)
    thenDisplayedNumberOfForfeitsIs(answer, 2)
  }

  @Test
  internal fun errorWhenNoMessageInfoForAverage() {

    val answer = whenMessageIsSent("!average", MessageInfo.empty())

    thenErrorIsReported(answer)
  }

  //</editor-fold>

  //<editor-fold desc="Median">

  @Test
  internal fun computesCorrectMedianDiscord() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

    val answer = whenDiscordMessageIsSent(username, "!median")

    thenReportedTimeIsEqualTo(answer, "0:00:08")
    thenDisplayedNumberOfRacesIs(answer, 15)
  }

  @Test
  internal fun computesCorrectMedianIrc() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

    val answer = whenIrcMessageIsSent(username, "!median")

    thenReportedTimeIsEqualTo(answer, "0:00:08")
    thenDisplayedNumberOfRacesIs(answer, 15)
  }

  @Test
  internal fun ignoresNonBingoTimesForMedian() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3)
    givenNonBingoTimesForPlayer(username, 4, 5)

    val answer = whenIrcMessageIsSent(username, "!median")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
    thenDisplayedNumberOfRacesIs(answer, 3)
  }

  @Test
  internal fun computesMedianForDifferentUser() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)

    val answer = whenDiscordMessageIsSent(requestUsername, "!median $queryUsername")

    thenReportedTimeIsEqualTo(answer, "0:00:08")
  }

  @Test
  internal fun computesMedianForDifferentAmountOfRaces() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3, 4, 5)

    val answer = whenDiscordMessageIsSent(username, "!median 3")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
  }

  @Test
  internal fun computesMedianForPlayerAndRaceAmount() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1, 2, 3, 4, 5)

    val answer = whenDiscordMessageIsSent(requestUsername, "!median $queryUsername 3")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
  }

  @Test
  internal fun computesMedianForRaceAmountAndPlayer() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1, 2, 3, 4, 5)

    val answer = whenDiscordMessageIsSent(requestUsername, "!median 3 $queryUsername")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
  }

  @Test
  internal fun displaysCorrectAmountOfRacesForMedian() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 10000, 12000, 8000)

    val answer = whenIrcMessageIsSent(username, "!median 5")

    thenDisplayedNumberOfRacesIs(answer, 3)
  }

  @Test
  internal fun ignoresForfeitsForMedian() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, -2, -3)

    val answer = whenIrcMessageIsSent(username, "!median 5")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
    thenDisplayedNumberOfRacesIs(answer, 2)
  }

  @Test
  internal fun showsNumberOfIgnoredForfeitsForMedian() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    val answer = whenIrcMessageIsSent(username, "!median 3")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
    thenDisplayedNumberOfRacesIs(answer, 3)
    thenDisplayedNumberOfForfeitsIs(answer, 2)
  }

  @Test
  internal fun errorWhenNoMessageInfoForMedian() {

    val answer = whenMessageIsSent("!median", MessageInfo.empty())

    thenErrorIsReported(answer)
  }

  //<editor-fold desc="Public API">

  @Test
  internal fun calculatesMedianForUser() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31)

    assertThat(module.median(username)).isEqualTo(Duration.ofSeconds(15))
  }

  @Test
  internal fun medianNullWhenUserNotFound() {

    val username = UUID.randomUUID().toString()

    assertThat(module.median(username)).isNull()
  }

  @Test
  internal fun medianNullWhenUserHasNoBingos() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 25)

    assertThat(module.median(username)).isNull()
  }

  //</editor-fold>

  //</editor-fold>

  //<editor-fold desc="Given">

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

    races.forEach {
      it.raceResults.forEach { result -> result.race = it }
    }

    val oldPlayer = players[username] ?: Player(0, username, emptyList())
    val player = oldPlayer.copy(races = oldPlayer.races + races)

    players[username] = player
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenMessageIsSent(message: String, messageInfo: MessageInfo): Answer<AnswerInfo>? {

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    return commands.getValue(command).invoke(generateCommand(message, messageInfo))
  }

  private fun whenDiscordMessageIsSent(user: String, message: String): Answer<AnswerInfo>? {

    val discordUser = UserImpl(0, mock(JDAImpl::class.java))
    discordUser.name = user

    val discordMessageMock = mock(Message::class.java)
    `when`(discordMessageMock.author).thenReturn(discordUser)

    return whenMessageIsSent(message, DiscordMessageInfo.withMessage(discordMessageMock))
  }

  private fun whenIrcMessageIsSent(username: String, message: String): Answer<AnswerInfo>? {

    val messageInfoMock = mock(IrcMessageInfo::class.java)
    `when`(messageInfoMock.nick).thenReturn(username)
    `when`(messageInfoMock.channel).thenReturn("")

    return whenMessageIsSent(message, messageInfoMock)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenReportedTimeIsEqualTo(answer: Answer<AnswerInfo>?, time: String) {

    val actualTime = answer?.text?.split(": ", limit = 2)?.get(1)?.substringBefore('(')?.trim()

    assertThat(actualTime).isEqualTo(time)
  }

  private fun thenDisplayedNumberOfRacesIs(answer: Answer<AnswerInfo>?, raceCount: Int) {

    val actualRaceCount = answer
        ?.text
        ?.substringAfter("last")
        ?.substringBefore("bingos")
        ?.trim()
        ?.toInt()

    assertThat(actualRaceCount).isEqualTo(raceCount)
  }

  private fun thenDisplayedNumberOfForfeitsIs(answer: Answer<AnswerInfo>?, forfeitCount: Int) {

    val actualRaceCount = answer
        ?.text
        ?.substringAfter("(Forfeits: ")
        ?.substringBefore(")")
        ?.trim()
        ?.toInt()

    assertThat(actualRaceCount).isEqualTo(forfeitCount)
  }

  private fun thenErrorIsReported(answer: Answer<AnswerInfo>?) {
    assertThat(answer?.text).contains("error")
  }

  //</editor-fold>

  //<editor-fold desc="Helper">

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

  //</editor-fold>
}