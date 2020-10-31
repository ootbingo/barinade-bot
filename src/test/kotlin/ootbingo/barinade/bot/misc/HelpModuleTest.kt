package ootbingo.barinade.bot.misc

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.core.communication.MessageInfo
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.internal.entities.UserImpl
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class HelpModuleTest {

  private val module = HelpModule()
  private val commands = mapOf(Pair("help", module::help))

  @Test
  internal fun answersWithHelpText() {

    val answer = whenDiscordMessageIsSent("", "!help")

    assertThat(answer?.text).isNotBlank()
  }

  private fun whenDiscordMessageIsSent(user: String, message: String): Answer<AnswerInfo>? {

    val discordUser = UserImpl(0, mock())
    discordUser.name = user

    val discordMessageMock = mock<Message>()
    whenever(discordMessageMock.author).thenReturn(discordUser)

    return whenMessageIsSent(message, DiscordMessageInfo.withMessage(discordMessageMock))
  }

  private fun whenMessageIsSent(message: String, messageInfo: MessageInfo): Answer<AnswerInfo>? {

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    return commands.getValue(command).invoke(generateCommand(message, messageInfo))
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
