package ootbingo.barinade.bot.extensions

import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.core.communication.MessageInfo
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import de.scaramangado.lily.irc.connection.IrcMessageInfo
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.RacetimeMessageInfo

fun MessageInfo.getUsername(): String? =
    when (this) {
      is DiscordMessageInfo -> this.message.author.name
      is IrcMessageInfo -> this.nick
      is RacetimeMessageInfo -> this.message.user?.name
      else -> null
    }

fun conditionalAnswer(command: Command, block: AnswerBuilder.() -> Unit): Answer<AnswerInfo> {
  val builder = AnswerBuilder().apply(block)

  return when (command.messageInfo) {
    is RacetimeMessageInfo -> builder.racetimeMessage
    else -> builder.discordMessage
  }.let { Answer.ofText(it) }
}

class AnswerBuilder(
    var discordMessage: String = "",
    var racetimeMessage: String = ""
)
