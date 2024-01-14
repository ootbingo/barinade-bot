package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant.RacetimeEntrantStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

class RaceOpenStage(
    completeStage: (AntiBingoState) -> Unit,
) : AntiBingoStage(completeStage) {

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {
    // Do nothing
  }

  override fun raceUpdate(race: RacetimeRace) {
    if (race.entrants.size >= 2 && race.entrants.all { it.status == READY }) {
      completeStage(AntiBingoState(race.entrants.map { it.user }, emptyList()))
    }
  }

  override fun handleCommand(command: ChatMessage) {
    // Do nothing
  }
}
