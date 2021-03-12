package ootbingo.barinade.bot.statistics

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.core.communication.MessageInfo
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import de.scaramangado.lily.irc.connection.IrcMessageInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.internal.entities.UserImpl
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.Platform
import ootbingo.barinade.bot.racing_services.data.model.Player
import ootbingo.barinade.bot.racing_services.data.model.Race
import ootbingo.barinade.bot.racing_services.data.model.RaceResult
import ootbingo.barinade.bot.racing_services.data.model.ResultType
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import org.assertj.core.api.Assertions.*
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.math.roundToLong
import kotlin.random.Random

internal class BingoStatModuleTest {

  private val playerDaoMock = mock<PlayerHelper>()
  private val module = BingoStatModule(playerDaoMock)
  private val players = mutableMapOf<String, Player>()

  private val commands by lazy {
    mapOf(Pair("average", module::average),
          Pair("median", module::median),
          Pair("forfeits", module::forfeitRatio))
  }

  @BeforeEach
  internal fun setup() {
    doAnswer { players[it.getArgument(0)] }
        .whenever(playerDaoMock).getPlayerByName(any())

    doAnswer {
      val test = players[it.getArgument<Player>(0).srlName!!]
          ?.races
          ?.map { r ->
            val result = r.raceResults.findLast { res ->
              res.resultId.player.srlName == it.getArgument<Player>(0).srlName!!
            }
            ResultInfo(result!!.time, r.goal, r.raceId, r.datetime, result.resultType)
          }
      test
    }.whenever(playerDaoMock).findResultsForPlayer(any())
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
  internal fun displaysMessageWhenAverageForNonBingoPlayerRequested() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    val answer = whenIrcMessageIsSent(username, "!average")

    thenNoBingosFinishedIsReported(username, answer)
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

    givenBingoTimesForPlayer(username, 1, 3, -1, -2, -3, 2, -1)

    val answer = whenIrcMessageIsSent(username, "!median 3")

    thenReportedTimeIsEqualTo(answer, "0:00:02")
    thenDisplayedNumberOfRacesIs(answer, 3)
    thenDisplayedNumberOfForfeitsIs(answer, 3)
  }

  @Test
  internal fun displaysMessageWhenMedianForNonBingoPlayerRequested() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    val answer = whenIrcMessageIsSent(username, "!median")

    thenNoBingosFinishedIsReported(username, answer)
  }

  //</editor-fold>

  //<editor-fold desc="Public Median API">

  @Test
  internal fun calculatesMedianForUser() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31)

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

  @Test
  internal fun ignoresNonBingoRacesWhenCalculatingMedian() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3)
    givenNonBingoTimesForPlayer(username, 25)

    assertThat(module.median(username)).isEqualTo(Duration.ofSeconds(2))
  }

  //</editor-fold>

  //<editor-fold desc="Forfeit Ratio">

  @Test
  internal fun computesCorrectForfeitRatioDiscord() {

    val username = UUID.randomUUID().toString()

    val finishedCount = Random.nextInt(1, 1000)
    val forfeitCount = Random.nextInt(1, 1000)

    val raceTimes = mutableListOf<Int>()

    generateSequence(1) { it }.take(finishedCount).forEach { _ -> raceTimes.add(1) }
    generateSequence(1) { it }.take(forfeitCount).forEach { _ -> raceTimes.add(-1) }

    givenBingoTimesForPlayer(username, *raceTimes.toIntArray())

    val answer = whenDiscordMessageIsSent(username, "!forfeits")

    thenReportedForfeitRatioIsEqualTo(answer, forfeitCount.toDouble() / raceTimes.size)
  }

  @Test
  internal fun computesCorrectForfeitRatioIrc() {

    val username = UUID.randomUUID().toString()

    val finishedCount = Random.nextInt(1, 1000)
    val forfeitCount = Random.nextInt(1, 1000)

    val raceTimes = mutableListOf<Int>()

    generateSequence(1) { it }.take(finishedCount).forEach { _ -> raceTimes.add(1) }
    generateSequence(1) { it }.take(forfeitCount).forEach { _ -> raceTimes.add(-1) }

    givenBingoTimesForPlayer(username, *raceTimes.toIntArray())

    val answer = whenIrcMessageIsSent(username, "!forfeits")

    thenReportedForfeitRatioIsEqualTo(answer, forfeitCount.toDouble() / raceTimes.size)
  }

  @Test
  internal fun ignoresNonBingoTimesForForfeitRatio() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, -3, -4)
    givenNonBingoTimesForPlayer(username, 30000, 2, 36)

    val answer = whenIrcMessageIsSent(username, "!forfeits")

    thenReportedForfeitRatioIsEqualTo(answer, 0.5)
  }

  @Test
  internal fun computesForfeitRatioForDifferentUser() {

    val requestUsername = UUID.randomUUID().toString()
    val queryUsername = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(requestUsername, 10000)
    givenBingoTimesForPlayer(queryUsername, 1, 2, -3, -4, -5)

    val answer = whenDiscordMessageIsSent(requestUsername, "!forfeits $queryUsername")

    thenReportedForfeitRatioIsEqualTo(answer, 0.6)
  }

  @Test
  internal fun displaysMessageWhenForfeitRatioForNonBingoPlayerRequested() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    val answer = whenIrcMessageIsSent(username, "!forfeits")

    thenNoBingosFinishedIsReported(username, answer)
  }

  @Test
  internal fun displaysMessageWhenForfeitRatioForUnknownPlayerRequested() {

    val username = UUID.randomUUID().toString()

    val answer = whenIrcMessageIsSent(username, "!forfeits")

    thenUserNotFoundIsReported(username, answer)
  }

  //</editor-fold>

  //<editor-fold desc="Public Forfeit API">

  @Test
  internal fun calculatesForfeitRatioForUser() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, 5, -7, -9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31)

    assertThat(module.forfeitRatio(username)).isEqualTo(0.125)
  }

  @Test
  internal fun calculatesForfeitRatioWithoutForfeits() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31)

    assertThat(module.forfeitRatio(username)).isEqualTo(0.0)
  }

  @Test
  internal fun forfeitRatioNullWhenUserNotFound() {

    val username = UUID.randomUUID().toString()

    assertThat(module.forfeitRatio(username)).isNull()
  }

  @Test
  internal fun forfeitRatioNullWhenUserHasNoBingos() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 25)

    assertThat(module.forfeitRatio(username)).isNull()
  }

  @Test
  internal fun ignoresNonBingoRacesWhenCalculatingForfeitRatio() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, -2)
    givenNonBingoTimesForPlayer(username, 25)

    assertThat(module.forfeitRatio(username)).isEqualTo(1.0)
  }

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

    var timestamp = Random.nextLong(99999, 1568937600)

    times
        .map {
          RaceResult(RaceResult.ResultId(Race(), Player(srlName = username, raceResults = mutableListOf())), 1,
                     Duration.ofSeconds(it.toLong()),
                     if (it > 0) ResultType.FINISH else ResultType.FORFEIT)
        }
        .map {

          val goal = if (bingo) "speedrunslive.com/tools/oot-bingo"
          else ""

          Race("0", goal, Instant.ofEpochSecond(timestamp--), Platform.SRL,
               mutableListOf(it))
        }
        .map {
          val spy = spy(it)
          whenever(spy.isBingo()).thenReturn(bingo)
          spy
        }
        .forEach { races.add(it) }

    races.forEach {
      it.raceResults.forEach { result -> result.resultId.race = it }
    }

    val oldPlayer = players[username] ?: Player(null, 0, null, username, null, mutableListOf())
    val player = oldPlayer.copy(raceResults = (oldPlayer.raceResults + races.mapNotNull {
      it.raceResults.findLast { result -> result.resultId.player.srlName == oldPlayer.srlName }
    }).toMutableList())

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

    val discordUser = UserImpl(0, mock())
    discordUser.name = user

    val discordMessageMock = mock<Message>()
    whenever(discordMessageMock.author).thenReturn(discordUser)

    return whenMessageIsSent(message, DiscordMessageInfo.withMessage(discordMessageMock))
  }

  private fun whenIrcMessageIsSent(username: String, message: String): Answer<AnswerInfo>? {

    val messageInfoMock = mock<IrcMessageInfo>()
    whenever(messageInfoMock.nick).thenReturn(username)
    whenever(messageInfoMock.channel).thenReturn("")

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

  private fun thenReportedForfeitRatioIsEqualTo(answer: Answer<AnswerInfo>?, forfeitRatio: Double) {

    val actualForfeitRatio = answer
        ?.text
        ?.substringAfterLast(':')
        ?.substringBefore('%')
        ?.trim()
        ?.toDouble()
        ?.let { it * 100 }
        ?.roundToLong()
        ?.toDouble()
        ?.let { it / 100 }

    assertThat(actualForfeitRatio).isCloseTo(forfeitRatio * 100, Percentage.withPercentage(0.25))
  }

  private fun thenNoBingosFinishedIsReported(username: String, answer: Answer<AnswerInfo>?) =
      assertThat(answer?.text).matches("""^.*$username .*not finish.*any bingo.*$""")

  private fun thenUserNotFoundIsReported(username: String, answer: Answer<AnswerInfo>?) =
      assertThat(answer?.text).matches("""^.*$username .*not found.*$""")

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
