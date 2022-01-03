package ootbingo.barinade.bot.testutils

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.core.communication.MessageInfo
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import de.scaramangado.lily.irc.connection.IrcMessageInfo
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.internal.entities.UserImpl
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

abstract class ModuleTest {

  protected abstract val commands: Map<String, (Command) -> Answer<AnswerInfo>?>
  protected var answer: String? = null

  protected fun whenDiscordMessageIsSent(user: String, message: String): Answer<AnswerInfo>? {

    val discordUser = UserImpl(0, mock())
    discordUser.name = user

    val discordMessageMock = mock<Message>()
    whenever(discordMessageMock.author).thenReturn(discordUser)

    return whenMessageIsSent(message, DiscordMessageInfo.withMessage(discordMessageMock))
  }

  protected fun whenIrcMessageIsSent(username: String, message: String): Answer<AnswerInfo>? {

    val messageInfoMock = mock<IrcMessageInfo>()
    whenever(messageInfoMock.nick).thenReturn(username)
    whenever(messageInfoMock.channel).thenReturn("")

    return whenMessageIsSent(message, messageInfoMock)
  }

  protected fun whenRacetimeMessageIsSent(user: String, message: String) {

    val racetimeUser = RacetimeUser("", user)

    val racetimeMessage = ChatMessage(user = racetimeUser, message = message, messagePlain = message)

    whenMessageIsSent(message, RacetimeMessageInfo(racetimeMessage))
  }

  private fun whenMessageIsSent(message: String, messageInfo: MessageInfo): Answer<AnswerInfo>? {

    require(message.matches(Regex("!.*"))) { "Not a valid command" }

    val parts = message.split(" ")
    val command = parts[0].replace("!", "")

    require(commands.containsKey(command)) { "Command not known" }

    return commands.getValue(command).invoke(generateCommand(message, messageInfo))
        .also { answer = it?.text }
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
