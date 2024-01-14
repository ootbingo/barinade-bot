package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

sealed class AntiBingoStage(
    protected val completeStage: (AntiBingoState) -> Unit,
) {

  abstract fun initialize(initialState: AntiBingoState, race: RacetimeRace)
  abstract fun raceUpdate(race: RacetimeRace)
  abstract fun handleCommand(command: ChatMessage)
}
