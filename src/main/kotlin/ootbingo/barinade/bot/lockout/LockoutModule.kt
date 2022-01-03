package ootbingo.barinade.bot.lockout

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
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
          ?.let { (it as DiscordMessageInfo).message }
          ?.let { createLockoutChannel(it.guild) }
          ?.let { Answer.ofText(it.asMention) }

  private fun createLockoutChannel(server: Guild): TextChannel? =
      discordChannelService.createChannel {
        name = "lockout-${UUID.randomUUID().toString().split("-")[0]}"
        categoryId = properties.discordCategory
        guild = server
      }
}
