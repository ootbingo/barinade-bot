package ootbingo.barinade.bot.discord

import de.scaramangado.lily.core.annotations.LilyCommand
import de.scaramangado.lily.core.annotations.LilyModule
import de.scaramangado.lily.core.communication.Answer
import de.scaramangado.lily.core.communication.AnswerInfo
import de.scaramangado.lily.core.communication.Command
import de.scaramangado.lily.discord.connection.DiscordMessageInfo
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.newBingoRace

@LilyModule
class RaceRoomOpeningModule(private val racetimeHttpClient: RacetimeHttpClient) {

  @LilyCommand("newrace")
  fun newRace(chatCommand: Command): Answer<AnswerInfo>? {

    if (chatCommand.messageInfo !is DiscordMessageInfo) {
      return null
    }

    return Answer.ofText(racetimeHttpClient.startRace(newBingoRace(false)))
  }

  @LilyCommand("newteamrace")
  fun newTeamRace(chatCommand: Command): Answer<AnswerInfo>? {

    if (chatCommand.messageInfo !is DiscordMessageInfo) {
      return null
    }

    return Answer.ofText(racetimeHttpClient.startRace(newBingoRace(true)))
  }
}
