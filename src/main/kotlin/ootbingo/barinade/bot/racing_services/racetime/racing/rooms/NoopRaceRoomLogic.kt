package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace

data object NoopRaceRoomLogic : RaceRoomLogic {

  override val commands: Map<String, (ChatMessage) -> Unit>
    get() = emptyMap()

  override fun initialize(race: RacetimeRace) {
    // do nothing
  }

  override fun onRaceUpdate(race: RacetimeRace) {
    // do nothing
  }
}
