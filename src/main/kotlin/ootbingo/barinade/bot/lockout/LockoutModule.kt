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
import ootbingo.barinade.bot.discord.racing.DiscordRaceRoomFactory
import ootbingo.barinade.bot.discord.racing.DiscordRaceRoomManager
import ootbingo.barinade.bot.discord.racing.LockoutRaceRoom
import ootbingo.barinade.bot.extensions.castOrNull
import ootbingo.barinade.bot.extensions.exception
import org.slf4j.LoggerFactory
import java.util.*

@LilyModule
class LockoutModule(
    private val properties: LockoutProperties,
    private val discordChannelService: DiscordChannelService,
    private val lockoutFactory: DiscordRaceRoomFactory<LockoutRaceRoom>,
    private val raceRoomManager: DiscordRaceRoomManager,
) {

  private val logger = LoggerFactory.getLogger(LockoutModule::class.java)

  @LilyCommand("lockout")
  fun lockout(command: Command): Answer<AnswerInfo>? =
      try {
        command.messageInfo
            .castOrNull<DiscordMessageInfo>()
            ?.message
            ?.channel
            ?.takeIf { it.id == properties.discordChannel }
            ?.castOrNull<TextChannel>()
            ?.let { createLockoutChannel(it.guild) }
            ?.also { raceRoomManager.addRaceRoom(it, lockoutFactory.createRaceRoom(it)) }
            ?.let { Answer.ofText("New race: ${it.asMention}") }
      } catch (e: Exception) {
        logger.exception("Failed to create lockout channel", e)
        Answer.ofText("An error occurred when creating the race channel")
      }

  private fun createLockoutChannel(server: Guild): TextChannel? =
      discordChannelService.createChannel {
        name = "lockout-${UUID.randomUUID().toString().split("-")[0]}"
        categoryId = properties.discordCategory
        guild = server
      }
}
