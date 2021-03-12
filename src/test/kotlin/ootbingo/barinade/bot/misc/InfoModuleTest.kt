package ootbingo.barinade.bot.misc

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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

internal class InfoModuleTest {

  private val module = InfoModule()

  private val commands = mapOf("golds" to module::golds, "silvers" to module::silvers)
  private var answer: String? = null

  //<editor-fold desc="!golds">

  @Test
  internal fun goldsMultilineOnDiscord() {
    whenDiscordMessageIsSent("", "!golds")
    thenAnswerIsPreformatted()
  }

  @Test
  internal fun goldsSingleLineOnRacetime() {
    whenRacetimeMessageIsSent("", "!golds")
    thenAnswerIsSingleLine()
  }

  //</editor-fold>

  //<editor-fold desc="!silvers">

  @Test
  internal fun silversMultilineOnDiscord() {
    whenDiscordMessageIsSent("", "!silvers")
    thenAnswerIsPreformatted()
  }

  @Test
  internal fun silversSingleLineOnRacetime() {
    whenRacetimeMessageIsSent("", "!silvers")
    thenAnswerIsSingleLine()
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

  private fun thenAnswerIsPreformatted() {
    assertThat(answer).startsWith("```")
    assertThat(answer).endsWith("```")
  }

  private fun thenAnswerIsSingleLine() {
    assertThat(answer).doesNotStartWith("```")
    assertThat(answer).doesNotContain("\n")
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