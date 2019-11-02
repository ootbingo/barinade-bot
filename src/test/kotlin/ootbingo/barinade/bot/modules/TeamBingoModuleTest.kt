package ootbingo.barinade.bot.modules

import de.scaramanga.lily.core.communication.Answer
import de.scaramanga.lily.core.communication.AnswerInfo
import de.scaramanga.lily.core.communication.Command
import de.scaramanga.lily.core.communication.MessageInfo
import de.scaramanga.lily.irc.connection.IrcMessageInfo
import org.assertj.core.api.Assertions.*
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.time.Duration
import java.util.UUID

internal class TeamBingoModuleTest {

  private val bingoStatModuleMock = mock(BingoStatModule::class.java)
  private val module = TeamBingoModule(bingoStatModuleMock)

  private val commands by lazy {
    mapOf(Pair("teamtime", module::teamTime))
  }

  @Test
  internal fun teamTimeForSingleTimeWithoutForfeits() {

    val soft = SoftAssertions()

    mapOf(Pair("1:20:00", "3:20:00"),
          Pair("1:10:00", "2:47:00"),
          Pair("1:32:48", "4:02:14"))
        .forEach { (normal, blackout) ->
          val answer = whenMessageIsSent("!teamtime $normal")
          thenCalculatedTeamTimeEquals(answer, blackout, soft)
        }

    soft.assertAll()
  }

  @Test
  internal fun teamTimeForSingleUserWithoutForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)

    val answer = whenMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "3:53:00")
  }

  @Test
  internal fun teamTimeForSingleUserWithMediumForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(20).toSeconds(), forfeitRatio = 0.25)

    val answer = whenMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "3:53:00")
  }

  @Test
  internal fun teamTimeForSingleUserWithMaxForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(20).toSeconds(), forfeitRatio = 0.5)

    val answer = whenMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "4:26:00")
  }

  @Test
  internal fun teamTimeForSingleUserWithMoreThanMaxForfeits() {

    val username = UUID.randomUUID().toString()

    givenUser(username, median = Duration.ofHours(1).plusMinutes(20).toSeconds(), forfeitRatio = 0.51)

    val answer = whenMessageIsSent("!teamtime $username")
    thenCalculatedTeamTimeEquals(answer, "4:26:00")
  }

  @Test
  internal fun teamTimeForTwoUsersWithoutForfeits() {

    val username1 = UUID.randomUUID().toString()
    val username2 = UUID.randomUUID().toString()

    givenUser(username1, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)
    givenUser(username2, median = Duration.ofHours(1).plusMinutes(24).toSeconds(), forfeitRatio = 0.0)

    val answer = whenMessageIsSent("!teamtime $username1 $username2")
    thenCalculatedTeamTimeEquals(answer, "2:08:47")
  }

  @Test
  internal fun errorMessageWhenUnknownUsers() {

    val username1 = UUID.randomUUID().toString()
    val username2 = UUID.randomUUID().toString()
    val username3 = UUID.randomUUID().toString()

    givenUser(username1, median = Duration.ofHours(1).plusMinutes(30).toSeconds(), forfeitRatio = 0.0)

    val answer = whenMessageIsSent("!teamtime $username1 $username2 $username3 1:20:00")

    thenErrorMessageMentions(answer, username2, username3)
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

  //<editor-fold desc="Given">

  private fun givenUser(username: String, median: Long, forfeitRatio: Double) {

    `when`(bingoStatModuleMock.median(username)).thenReturn(Duration.ofSeconds(median))
    `when`(bingoStatModuleMock.forfeitRatio(username)).thenReturn(forfeitRatio)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenMessageIsSent(message: String): Answer<AnswerInfo>? {

    val messageInfoMock = Mockito.mock(IrcMessageInfo::class.java)
    Mockito.`when`(messageInfoMock.nick).thenReturn("")
    Mockito.`when`(messageInfoMock.channel).thenReturn("")

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