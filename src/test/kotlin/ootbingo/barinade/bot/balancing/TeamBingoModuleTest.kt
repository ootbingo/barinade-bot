package ootbingo.barinade.bot.balancing

import com.nhaarman.mockitokotlin2.mock
import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.irc.connection.IrcMessageInfo
import ootbingo.barinade.bot.statistics.BingoStatModule
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

internal class TeamBingoModuleTest {

  private val bingoStatModuleMock = mock(BingoStatModule::class.java)
  private val teamBalancerMock = mock(TeamBalancer::class.java)
  private val partitionerMock = mock<(List<TeamMember>, Int) -> List<List<Team>>>()
  private val module = TeamBingoModule(bingoStatModuleMock, teamBalancerMock, partitionerMock)

  private val commands by lazy {
    mapOf(Pair("teamtime", module::teamTime),
          Pair("balance", module::balance))
  }

  //<editor-fold desc="!teamtime">

  @Test
  internal fun teamTimeForSingleTimeWithoutForfeits() {

    val soft = SoftAssertions()

    mapOf(Pair("1:20:00", "3:20:00"),
          Pair("1:10:00", "2:47:00"),
          Pair("1:32:48", "4:02:14"))
        .forEach { (normal, blackout) ->
          val answer = whenIrcMessageIsSent("!teamtime $normal")
          thenCalculatedTeamTimeEquals(answer, blackout, soft)
        }

    soft.assertAll()
  }

  @Test
  internal fun teamTimeForSingleUserWithoutForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)

    val answer = whenIrcMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "3:53:00")
  }

  @Test
  internal fun teamTimeForSingleUserWithMediumForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(20).toSeconds(), forfeitRatio = 0.25)

    val answer = whenIrcMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "3:53:00")
  }

  @Test
  internal fun teamTimeForSingleUserWithMaxForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(20).toSeconds(), forfeitRatio = 0.5)

    val answer = whenIrcMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "4:26:00")
  }

  @Test
  internal fun teamTimeForSingleUserWithMoreThanMaxForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(20).toSeconds(), forfeitRatio = 0.51)

    val answer = whenIrcMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "4:26:00")
  }

  @Test
  internal fun teamTimeForTwoUsersWithoutForfeits() {

    val username1 = UUID.randomUUID().toString()
    val username2 = UUID.randomUUID().toString()

    givenUser(username1, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)
    givenUser(username2, median = Duration.ofHours(1).plusMinutes(24).toSeconds(), forfeitRatio = 0.0)

    val answer = whenIrcMessageIsSent("!teamtime $username1 $username2")
    thenCalculatedTeamTimeEquals(answer, "2:08:47")
  }

  @Test
  internal fun errorMessageWhenUnknownUsersTeamTime() {

    val username1 = UUID.randomUUID().toString()
    val username2 = UUID.randomUUID().toString()
    val username3 = UUID.randomUUID().toString()

    givenUser(username1, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)

    val answer = whenIrcMessageIsSent("!teamtime $username1 $username2 $username3 1:20:00")

    thenErrorMessageMentions(answer, username2, username3)
  }

  //</editor-fold>

  //<editor-fold desc="!balance">

  @Test
  internal fun reportsCorrectBalancing() {

    val usernames = (1..6).map { UUID.randomUUID().toString() }
    usernames.forEach { givenUser(it, 0, 0.0) }

    val teamTimes = (1..2).map { Random.nextLong(0, 10000) }.map { Duration.ofSeconds(it) }

    doAnswer { listOf(listOf(Team(listOf(TeamMember(usernames[0], 0, 0.0))))) }
        .`when`(partitionerMock).invoke(anyList(), eq(3))

    doAnswer {
      val team1 = mock(Team::class.java)
      val team2 = mock(Team::class.java)

      `when`(team1.members).thenReturn(usernames.subList(0, 3).map { TeamMember(it, 0, 0.0) })
      `when`(team2.members).thenReturn(usernames.subList(3, 6).map { TeamMember(it, 0, 0.0) })

      `when`(team1.predictedTime).thenReturn(teamTimes[0])
      `when`(team2.predictedTime).thenReturn(teamTimes[1])

      `when`(team1.toString()).thenCallRealMethod()
      `when`(team2.toString()).thenCallRealMethod()

      listOf(team1, team2)
    }
        .`when`(teamBalancerMock).findBestTeamBalance(anyList())

    val answer = whenIrcMessageIsSent("!balance " + usernames.joinToString(" "))

    thenReportedTeamsAre(answer,
                         Pair(usernames.subList(0, 3).toSet(), teamTimes[0]),
                         Pair(usernames.subList(3, 6).toSet(), teamTimes[1]))
  }

  @Test
  internal fun errorMessageWhenUnknownUsersBalance() {

    val username1 = UUID.randomUUID().toString()
    val username2 = UUID.randomUUID().toString()
    val username3 = UUID.randomUUID().toString()

    givenUser(username1, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)

    val answer = whenIrcMessageIsSent("!balance $username1 $username2 $username3 1:20:00 1:20:00 1:20:00")

    thenErrorMessageMentions(answer, username2, username3)
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenUser(username: String, median: Long, forfeitRatio: Double) {

    `when`(bingoStatModuleMock.median(username)).thenReturn(Duration.ofSeconds(median))
    `when`(bingoStatModuleMock.forfeitRatio(username)).thenReturn(forfeitRatio)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenIrcMessageIsSent(message: String): Answer<AnswerInfo>? {

    val messageInfoMock = mock(IrcMessageInfo::class.java)
    `when`(messageInfoMock.nick).thenReturn("")
    `when`(messageInfoMock.channel).thenReturn("")

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    return commands.getValue(command).invoke(generateCommand(message, messageInfoMock))
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenCalculatedTeamTimeEquals(answer: Answer<AnswerInfo>?, expectedTeamTime: String) {

    val actualTeamTime = answer
        ?.text
        ?.split("in:", limit = 2)
        ?.get(1)
        ?.trim()

    assertThat(actualTeamTime).isEqualTo(expectedTeamTime)
  }

  private fun thenCalculatedTeamTimeEquals(answer: Answer<AnswerInfo>?, expectedTime: String, soft: SoftAssertions) {

    val actualTeamTime = answer
        ?.text
        ?.split("in:", limit = 2)
        ?.get(1)
        ?.trim()

    soft.assertThat(actualTeamTime).isEqualTo(expectedTime)
  }

  private fun thenReportedTeamsAre(answer: Answer<AnswerInfo>?, vararg expectedTeams: Pair<Set<String>, Duration>) {

    require(answer != null)

    val lines = answer.text
        .split("\n")
        .filter { it.matches(Regex("[a-z0-9\\-, ]*\\(\\d+:\\d\\d:\\d\\d\\)")) }

    val actualTeamMembers = lines
        .map { it.split(" (")[0] }
        .map { it.split(",") }

    val actualTimes = lines
        .map { it.split(" (")[1] }
        .map { it.split(")")[0] }
        .map {
          val parts = it.split(":").map { p -> p.toLong() }
          Duration.ofHours(parts[0]).plusMinutes(parts[1]).plusSeconds(parts[2])
        }

    val actualTeams = actualTeamMembers.indices
        .map { Pair(actualTeamMembers[it].map { name -> name.trim() }.toSet(), actualTimes[it]) }

    assertThat(actualTeams).containsExactlyInAnyOrder(*expectedTeams)
  }

  private fun thenErrorMessageMentions(answer: Answer<AnswerInfo>?, vararg usernames: String) {

    requireNotNull(answer)
    require(answer.text.matches(Regex("Error[^:]*:[^:]*")))

    val errorUsers = answer.text
        .split(":")[1]
        .trim()
        .split(",")
        .map { it.trim() }

    assertThat(usernames).containsExactlyInAnyOrderElementsOf(errorUsers)
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