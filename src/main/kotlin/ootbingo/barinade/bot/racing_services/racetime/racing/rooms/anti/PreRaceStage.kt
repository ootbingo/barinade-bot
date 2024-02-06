package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

data object PreRaceStage : AntiBingoStage({}) {

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {
    // Do nothing
  }

  override fun raceUpdate(race: RacetimeRace) {
    // Do nothing
  }

  override fun handleCommand(command: ChatMessage) {
    // Do nothing
  }
}
