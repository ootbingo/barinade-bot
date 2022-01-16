package ootbingo.barinade.bot.discord.racing

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import net.dv8tion.jda.api.entities.Message
import ootbingo.barinade.bot.extensions.checkFirstForNull
import ootbingo.barinade.bot.extensions.exception
import org.slf4j.LoggerFactory

@LilyModule
class DiscordRacingModule(private val manager: DiscordRaceRoomManager) {

  private val logger = LoggerFactory.getLogger(DiscordRacingModule::class.java)

  @LilyCommand("enter", "join")
  fun enter(command: Command): Answer<AnswerInfo>? =
      forwardCommand(command, DiscordRaceRoom::enter)

  @LilyCommand("unenter", "leave")
  fun unenter(command: Command): Answer<AnswerInfo>? =
      forwardCommand(command, DiscordRaceRoom::unenter)

  @LilyCommand("ready")
  fun ready(command: Command): Answer<AnswerInfo>? =
      forwardCommand(command, DiscordRaceRoom::ready)

  @LilyCommand("unready")
  fun unready(command: Command): Answer<AnswerInfo>? =
      forwardCommand(command, DiscordRaceRoom::unready)

  @LilyCommand("done")
  fun done(command: Command): Answer<AnswerInfo>? =
      forwardCommand(command, DiscordRaceRoom::done)

  @LilyCommand("bingosync")
  fun bingosync(command: Command): Answer<AnswerInfo>? =
      forwardCommand(command, DiscordRaceRoom::bingosync)

  private fun forwardCommand(command: Command, forward: RaceRoomCommand): Answer<AnswerInfo>? =
      command.messageInfo
          .takeIf { it is DiscordMessageInfo }
          ?.let { it as DiscordMessageInfo }
          ?.message
          ?.let { it.raceRoom() to it }
          ?.checkFirstForNull()
          ?.let { forward.invoke(it.first, it.second.author) }
          ?.let { Answer.ofText(it) }

  private fun Message.raceRoom() =
      try {
        manager.getRaceRoomForChannel(textChannel)
      } catch (e: IllegalStateException) {
        null
      } catch (e: Exception) {
        logger.exception("Failed to find Discord race room", e)
        null
      }
}
