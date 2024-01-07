package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace

sealed interface RaceRoomLogic {

  val commands: Map<String, (ChatMessage) -> Unit>
  fun initialize(race: RacetimeRace)
  fun onRaceUpdate(race: RacetimeRace)
}
