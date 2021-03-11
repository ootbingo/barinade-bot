package ootbingo.barinade.bot.misc

import com.nhaarman.mockitokotlin2.*
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.core.communication.MessageInfo
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.internal.entities.UserImpl
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList

internal class RandomAnswerModuleTest {

  //<editor-fold desc="Setup">

  private lateinit var shameList: List<String>
  private val module = spy(RandomAnswerModule { shameList })

  private val commands = mapOf("shame" to module::shame, "pick" to module::pick)

  private var answer: String? = null

  //</editor-fold>

  //<editor-fold desc="Meta Function">

  @Test
  internal fun metaFunctionReturnsRandomValueFromList() {

    val values = IntStream.range(0, 20).mapToObj { UUID.randomUUID().toString() }.toList()

    val answers = values.map { module.randomValue(values) }

    answers.forEach {
      assertThat(it).isIn(values)
    }

    assertThat(answers.toSet()).hasSizeGreaterThan(1)
  }

  //</editor-fold>

  //<editor-fold desc="!shame">

  @Test
  internal fun shameCallsMetaFunctionWithShameList() {

    val shame1 = UUID.randomUUID().toString()
    val shame2 = UUID.randomUUID().toString()
    val shame3 = UUID.randomUUID().toString()

    givenShameList(shame1, shame2, shame3)

    whenDiscordMessageIsSent("", "!shame")

    thenMetaFunctionIsCalledWith(shame1, shame2, shame3)
  }

  @Test
  internal fun shameReturnsResultOfMetaFunctionDiscord() {

    val answer = UUID.randomUUID().toString()

    givenMetaFunctionReturns(answer)
    givenShameList("")

    whenDiscordMessageIsSent("", "!shame")

    thenAnswerMatches(answer)
  }

  @Test
  internal fun shameReturnsResultOfMetaFunctionRacetime() {

    val answer = UUID.randomUUID().toString()

    givenMetaFunctionReturns(answer)
    givenShameList("")

    whenRacetimeMessageIsSent("", "!shame")

    thenAnswerMatches(answer)
  }

  //</editor-fold>

  //<editor-fold desc="!pick">

  @Test
  internal fun pickCallsMetaFunction() {

    whenRacetimeMessageIsSent("any", "!pick")

    thenMetaFunctionIsCalledWith(
        "ROW1", "ROW2", "ROW3", "ROW4", "ROW5", "COL1", "COL2", "COL3", "COL4", "COL5", "TL-BR", "BL-TR"
    )
  }

  @Test
  internal fun pickReturnsRandomRowAndUsername() {

    val row = UUID.randomUUID().toString()
    val username = UUID.randomUUID().toString()

    givenMetaFunctionReturns(row)

    whenRacetimeMessageIsSent(username, "!pick")

    thenAnswerMatches(Regex("""$username:.*$row"""))
  }

  @Test
  internal fun pickDoesNotReturnOnDiscord() {

    whenDiscordMessageIsSent("any", "!pick")

    thenNoAnswerIsSent()
  }

  //</editor-fold>

  //<editor-fold desc="Given">

  private fun givenShameList(vararg messages: String) {
    shameList = messages.toList()
  }

  private fun givenMetaFunctionReturns(random: String) {
    whenever(module.randomValue(any())).thenReturn(random)
  }

  //</editor-fold>

  //<editor-fold desc="When">

  private fun whenDiscordMessageIsSent(user: String, message: String) {

    val discordUser = UserImpl(0, mock())
    discordUser.name = user

    val discordMessageMock = mock<Message>()
    whenever(discordMessageMock.author).thenReturn(discordUser)

    return whenMessageIsSent(message, DiscordMessageInfo.withMessage(discordMessageMock))
  }

  private fun whenRacetimeMessageIsSent(user: String, message: String) {

    val racetimeUser = RacetimeUser("", user)

    val racetimeMessage = ChatMessage(user = racetimeUser, message = message, messagePlain = message)

    whenMessageIsSent(message, RacetimeMessageInfo(racetimeMessage))
  }

  private fun whenMessageIsSent(message: String, messageInfo: MessageInfo) {

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    answer = commands.getValue(command).invoke(generateCommand(message, messageInfo))?.text
  }

  //</editor-fold>

  //<editor-fold desc="Then">

  private fun thenMetaFunctionIsCalledWith(vararg expectedValues: String) {
    val captor = argumentCaptor<Collection<String>>()
    verify(module).randomValue(captor.capture())
    assertThat(captor.lastValue).containsExactlyInAnyOrderElementsOf(expectedValues.toList())
  }

  private fun thenAnswerMatches(expectedText: String) {
    assertThat(answer).isEqualTo(expectedText)
  }

  private fun thenAnswerMatches(regex: Regex) {
    assertThat(answer).matches(regex.toPattern())
  }

  private fun thenNoAnswerIsSent() {
    assertThat(answer).isNull()
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
