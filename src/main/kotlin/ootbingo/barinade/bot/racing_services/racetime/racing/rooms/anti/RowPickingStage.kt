package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

class RowPickingStage(
    completeStage: (AntiBingoState) -> Unit,
) : AntiBingoStage(completeStage) {

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {
    TODO("Not yet implemented")
  }

  override fun raceUpdate(race: RacetimeRace) {
    TODO("Not yet implemented")
  }

  override fun handleCommand(command: ChatMessage) {
    TODO("Not yet implemented")
  }
}
