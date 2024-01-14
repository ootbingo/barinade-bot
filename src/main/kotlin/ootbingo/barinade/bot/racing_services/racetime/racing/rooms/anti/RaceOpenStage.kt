package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.AntiBingoRaceRoomLogic
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

class RaceOpenStage(
    completeStage: AntiBingoRaceRoomLogic.(AntiBingoState) -> Unit,
) : AntiBingoStage(completeStage) {

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {
    // Do nothing
  }

  override fun raceUpdate(race: RacetimeRace) {
    TODO("Not yet implemented")
  }

  override fun handleCommand(command: ChatMessage) {
    // Do nothing
  }
}
