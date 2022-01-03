package ootbingo.barinade.bot.lockout

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import ootbingo.barinade.bot.discord.DiscordChannelService
import java.util.*

@LilyModule
class LockoutModule(
    private val properties: LockoutProperties,
    private val discordChannelService: DiscordChannelService,
) {

  @LilyCommand("lockout")
  fun lockout(command: Command): Answer<AnswerInfo>? =
      command.messageInfo.takeIf { it is DiscordMessageInfo }
          ?.let { it as DiscordMessageInfo }
          ?.run {
            discordChannelService.createChannel {
              name = "lockout-${UUID.randomUUID().toString().split("-")[0]}"
              categoryId = properties.discordCategory
              guild = message.guild
            }?.also { it.sendMessage("Hello ${message.author.asMention}").queue() }
          }?.let { Answer.ofText(it.asMention) }
}
