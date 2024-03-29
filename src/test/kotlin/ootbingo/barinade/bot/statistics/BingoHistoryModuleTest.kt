package ootbingo.barinade.bot.statistics

import de.scaramangado.lily.irc.connection.IrcMessageInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.UserImpl
import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.model.*
import ootbingo.barinade.bot.racing_services.data.model.helper.ResultInfo
import ootbingo.barinade.bot.statistics.validation.RaceGoalValidator
import ootbingo.barinade.bot.testutils.ModuleTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.random.Random

internal class BingoHistoryModuleTest : ModuleTest() {

  //<editor-fold desc="Setup">

  private val playerHelperMock = mock<PlayerHelper>()
  private val raceGoalValidatorMock = mock<RaceGoalValidator>()
  private val module = BingoHistoryModule { QueryService(playerHelperMock, raceGoalValidatorMock) }
  private val players = mutableMapOf<String, Player>()

  override val commands by lazy {
    mapOf(
        "results" to module::results,
        "best" to module::best,
        "racer" to module::racer
    )
  }

  private val thenAnswer: String get() = answer!!

  @BeforeEach
  internal fun setup() {
    doAnswer { players[it.getArgument(0)] }
        .whenever(playerHelperMock).getPlayerByName(Mockito.anyString())

    doAnswer { players[it.getArgument(0)] }.whenever(playerHelperMock).getPlayerByName(any())

    doAnswer {
      it.getArgument<Player>(0)
          ?.races
          ?.map { r ->
            val result = r.raceResults.findLast { res ->
              res.resultId.player.srlName == it.getArgument<Player>(0).srlName
            }
            ResultInfo(result!!.time, r.goal, r.raceId, r.datetime, result.resultType)
          }
    }.whenever(playerHelperMock).findResultsForPlayer(any())

    whenever(raceGoalValidatorMock.isBingo(any(), any(), any())).thenReturn(false)
  }

  //</editor-fold>

  //<editor-fold desc="!results">

  @Test
  internal fun returnsCorrectResultsToDiscord() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3, 4, 5)

    whenUser(username) sendsDiscordMessage "!results"

    thenAnswer mentionsPlayer username andListsResults listOf("0:00:01", "0:00:02", "0:00:03", "0:00:04", "0:00:05")
  }

  @Test
  internal fun returnsCorrectResultsToIrc() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3, 4, 5)

    whenUser(username) sendsIrcMessage "!results"

    thenAnswer mentionsPlayer username andListsResults listOf("0:00:01", "0:00:02", "0:00:03", "0:00:04", "0:00:05")
  }

  @Test
  internal fun onlyListsBingoRacesWhenQueryingResults() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3)
    givenNonBingoTimesForPlayer(username, 11)

    whenUser(username) sendsIrcMessage "!results"

    thenAnswer listsResults listOf("0:00:01", "0:00:02", "0:00:03")
  }

  @Test
  internal fun returnsTenResultsByDefault() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)

    whenUser(username) sendsIrcMessage "!results"

    thenAnswer listsResults (1..10).map { "0:00:01" }
  }

  @Test
  internal fun doesNotListForfeitsAsResults() {

    val username = UUID.randomUUID().toString()

    givenTimesForPlayer(username, true,
        BingoTime(42, ResultType.FORFEIT),
        BingoTime(1, ResultType.FINISH),
        BingoTime(2, ResultType.FINISH),
        BingoTime(-3, ResultType.FORFEIT),
        BingoTime(3, ResultType.FINISH),
        BingoTime(-99, ResultType.DQ))

    whenUser(username) sendsIrcMessage "!results"

    thenAnswer listsResults listOf("0:00:01", "0:00:02", "0:00:03")
  }

  @Test
  internal fun returnsResultsForOtherUsers() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenBingoTimesForPlayer(playingUser, 5, 7, 3600)

    whenUser(askingUser) sendsIrcMessage "!results $playingUser"

    thenAnswer listsResults listOf("0:00:05", "0:00:07", "1:00:00")
  }

  @Test
  internal fun returnsSpecifiedNumberOfResults() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3, 4, 5)

    whenUser(username) sendsIrcMessage "!results 3"

    thenAnswer listsResults listOf("0:00:01", "0:00:02", "0:00:03")
  }

  @Test
  internal fun returnsSpecifiedNumberOfResultsForDifferentPlayer1() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenBingoTimesForPlayer(playingUser, 5, 7, 3600, 7, 42)

    whenUser(askingUser) sendsIrcMessage "!results $playingUser 3"

    thenAnswer listsResults listOf("0:00:05", "0:00:07", "1:00:00")
  }

  @Test
  internal fun returnsSpecifiedNumberOfResultsForDifferentPlayer2() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenBingoTimesForPlayer(playingUser, 5, 7, 3600, 7, 42)

    whenUser(askingUser) sendsIrcMessage "!results 3 $playingUser"

    thenAnswer listsResults listOf("0:00:05", "0:00:07", "1:00:00")
  }

  @Test
  internal fun displaysMessageWhenResultsForNonBingoPlayerRequested() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    whenUser(username) sendsIrcMessage "!results"

    thenNoBingosFinishedIsReported()
  }

  @Test
  internal fun displaysMessageWhenResultsForUnknownPlayerRequested() {

    val username = UUID.randomUUID().toString()

    whenUser(username) sendsIrcMessage "!results"

    thenUnknownPlayerIsReported()
  }

  //</editor-fold>

  //<editor-fold desc="!best">

  @Test
  internal fun returnsCorrectBestsToDiscord() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 10, 9, 8, 7, 6, 1, 3, 5, 2, 4)

    whenUser(username) sendsDiscordMessage "!best"

    thenAnswer mentionsPlayer username andListsResults listOf("0:00:01", "0:00:02", "0:00:03", "0:00:04", "0:00:05")
  }

  @Test
  internal fun returnsCorrectBestsToIrc() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 10, 9, 8, 7, 6, 1, 3, 5, 2, 4)

    whenUser(username) sendsIrcMessage "!best"

    thenAnswer mentionsPlayer username andListsResults listOf("0:00:01", "0:00:02", "0:00:03", "0:00:04", "0:00:05")
  }

  @Test
  internal fun onlyListsBingoRacesWhenQueryingBests() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 2, 3)
    givenNonBingoTimesForPlayer(username, 11)

    whenUser(username) sendsIrcMessage "!best"

    thenAnswer listsResults listOf("0:00:01", "0:00:02", "0:00:03")
  }

  @Test
  internal fun doesNotListForfeitsAsBests() {

    val username = UUID.randomUUID().toString()

    givenTimesForPlayer(username, true,
        BingoTime(2, ResultType.FORFEIT),
        BingoTime(10, ResultType.FINISH),
        BingoTime(11, ResultType.FINISH),
        BingoTime(1, ResultType.DQ))

    whenUser(username) sendsIrcMessage "!best"

    thenAnswer listsResults listOf("0:00:10", "0:00:11")
  }

  @Test
  internal fun returnsBestsForOtherUsers() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenBingoTimesForPlayer(playingUser, 5, 7, 3600)

    whenUser(askingUser) sendsIrcMessage "!best $playingUser"

    thenAnswer listsResults listOf("0:00:05", "0:00:07", "1:00:00")
  }

  @Test
  internal fun returnsSpecifiedNumberOfBests() {

    val username = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(username, 1, 5, 3, 4, 2)

    whenUser(username) sendsIrcMessage "!best 3"

    thenAnswer listsResults listOf("0:00:01", "0:00:02", "0:00:03")
  }

  @Test
  internal fun returnsSpecifiedNumberOfBestsForDifferentPlayer1() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenBingoTimesForPlayer(playingUser, 5, 7, 3600, 7, 42)

    whenUser(askingUser) sendsIrcMessage "!best $playingUser 3"

    thenAnswer listsResults listOf("0:00:05", "0:00:07", "0:00:07")
  }

  @Test
  internal fun returnsSpecifiedNumberOfBestsForDifferentPlayer2() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenBingoTimesForPlayer(playingUser, 5, 7, 3600, 7, 42)

    whenUser(askingUser) sendsIrcMessage "!best 3 $playingUser"

    thenAnswer listsResults listOf("0:00:05", "0:00:07", "0:00:07")
  }

  @Test
  internal fun displaysMessageWhenBestsForNonBingoPlayerRequested() {

    val username = UUID.randomUUID().toString()

    givenNonBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    whenUser(username) sendsIrcMessage "!best"

    thenNoBingosFinishedIsReported()
  }

  @Test
  internal fun displaysMessageWhenBestsForUnknownPlayerRequested() {

    val username = UUID.randomUUID().toString()

    whenUser(username) sendsIrcMessage "!best"

    thenUnknownPlayerIsReported()
  }

  //</editor-fold>

  //<editor-fold desc="!racer">

  @Test
  internal fun returnsCorrectRacerInfoToDiscord() {

    val username = UUID.randomUUID().toString()

    givenTimesForPlayer(username, true,
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.DQ),
        BingoTime(1, ResultType.FORFEIT))

    whenUser(username) sendsDiscordMessage "!racer"

    thenAnswer mentionsPlayer username hasCompleted 2 andHasForfeited 1
  }

  @Test
  internal fun returnsCorrectRacerInfoToIrc() {

    val username = UUID.randomUUID().toString()

    givenTimesForPlayer(username, true,
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FORFEIT),
        BingoTime(1, ResultType.DQ),
        BingoTime(1, ResultType.FORFEIT))

    whenUser(username) sendsIrcMessage "!racer"

    thenAnswer mentionsPlayer username hasCompleted 1 andHasForfeited 2
  }

  @Test
  internal fun onlyConsidersBingoRacesWhenQueryingRacerInfo() {

    val username = UUID.randomUUID().toString()

    givenTimesForPlayer(username, true,
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FORFEIT),
        BingoTime(1, ResultType.FORFEIT))
    givenNonBingoTimesForPlayer(username, 1, 2, 3, 4)

    whenUser(username) sendsIrcMessage "!racer"

    thenAnswer mentionsPlayer username hasCompleted 3 andHasForfeited 2
  }

  @Test
  internal fun returnsRacerInfoForOtherUsers() {

    val askingUser = UUID.randomUUID().toString()
    val playingUser = UUID.randomUUID().toString()

    givenBingoTimesForPlayer(askingUser, 1)
    givenTimesForPlayer(playingUser, true,
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FINISH),
        BingoTime(1, ResultType.FORFEIT),
        BingoTime(1, ResultType.FORFEIT))

    whenUser(askingUser) sendsIrcMessage "!racer $playingUser"

    thenAnswer mentionsPlayer playingUser hasCompleted 3 andHasForfeited 2
  }

  @Test
  internal fun displaysMessageWhenRacerInfoForNonBingoPlayerRequested() {

    val username = UUID.randomUUID().toString()

    givenTimesForPlayer(username, true, BingoTime(1, ResultType.DQ))
    givenNonBingoTimesForPlayer(username, 1, 3, -2, -3, 2, -1)

    whenUser(username) sendsIrcMessage "!racer"

    thenNoBingosFinishedIsReported()
  }

  @Test
  internal fun displaysMessageWhenRacerInfoForUnknownPlayerRequested() {

    val username = UUID.randomUUID().toString()

    whenUser(username) sendsIrcMessage "!racer"

    thenUnknownPlayerIsReported()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenBingoTimesForPlayer(username: String, vararg times: Int) {
    givenTimesForPlayer(username, true, *(times.map { BingoTime(it) }.toTypedArray()))
  }

  private fun givenNonBingoTimesForPlayer(username: String, vararg times: Int) {
    givenTimesForPlayer(username, false, *(times.map { BingoTime(it) }.toTypedArray()))
  }

  private data class BingoTime(val time: Int, val resultType: ResultType = ResultType.FINISH)

  private fun givenTimesForPlayer(username: String, bingo: Boolean, vararg times: BingoTime) {

    val races = ArrayList<Race>()

    var timestamp = Random.nextLong(99999, 1568937600)

    times
        .map {
          RaceResult(RaceResult.ResultId(Race(), Player(srlName = username, racetimeName = username)),
              1, Duration.ofSeconds(it.time.toLong()), it.resultType)
        }
        .map {

          val goal = if (bingo) "speedrunslive.com/tools/oot-bingo"
          else ""

          Race("0", goal, Instant.ofEpochSecond(timestamp--), Platform.SRL, mutableListOf(it))
        }
        .forEach {
          whenever(raceGoalValidatorMock.isBingo(it.raceId, it.goal, it.datetime)).thenReturn(bingo)
          races.add(it)
        }

    races.forEach {
      it.raceResults.forEach { result -> result.resultId.race = it }
    }

    val oldPlayer = players[username] ?: Player(0, srlName = username, racetimeName = username)
    val player = oldPlayer.copy(raceResults = (oldPlayer.raceResults + races.mapNotNull {
      it.raceResults.findLast { result -> result.resultId.player.srlName == oldPlayer.srlName }
    }).toMutableList())

    players[username] = player
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenUser(username: String) = username

  private infix fun String.sendsDiscordMessage(message: String) {

    val discordUser = UserImpl(0, Mockito.mock(JDAImpl::class.java))
    discordUser.name = this

    val discordMessageMock = Mockito.mock(Message::class.java)
    whenever(discordMessageMock.author).thenReturn(discordUser)

    whenDiscordMessageIsSent(this, message)
  }

  private infix fun String.sendsIrcMessage(message: String) {

    val messageInfoMock = Mockito.mock(IrcMessageInfo::class.java)
    whenever(messageInfoMock.nick).thenReturn(this)
    whenever(messageInfoMock.channel).thenReturn("")

    whenIrcMessageIsSent(this, message)
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private infix fun String.mentionsPlayer(username: String): String {

    assertThat(this).contains(username)
    return this
  }

  private infix fun String.andListsResults(results: List<String>) = listsResults(results)

  private infix fun String.hasCompleted(expectedCount: Int): String {

    val actualCount = Regex("""[A-Za-z0-9-_.]+ has finished (?<count>\d+) bingos and forfeited \d+""")
        .find(this)!!
        .groups["count"]!!
        .value
        .toInt()

    assertThat(actualCount).isEqualTo(expectedCount)

    return this
  }

  private infix fun String.andHasForfeited(expectedCount: Int): String {

    val actualCount = Regex("""[A-Za-z0-9-_.]+ has finished \d+ bingos and forfeited (?<count>\d+)""")
        .find(this)!!
        .groups["count"]!!
        .value
        .toInt()

    assertThat(actualCount).isEqualTo(expectedCount)

    return this
  }

  private infix fun String.listsResults(expectedResults: List<String>) {

    val actualResults = Regex("""^.*:(( ?\d{1,2}:\d{1,2}:\d{1,2},?)+).*$""")
        .find(this)!!
        .groupValues[1]
        .split(",")
        .map { it.trim() }
        .toList()

    assertThat(actualResults).containsExactlyElementsOf(expectedResults)
  }

  private fun thenNoBingosFinishedIsReported() {
    assertThat(thenAnswer).endsWith("has not finished any bingos")
  }

  private fun thenUnknownPlayerIsReported() {
    assertThat(thenAnswer).contains("not found")
  }

  //</editor-fold>
}
