package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant.RacetimeEntrantStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

class RaceOpenStage(
    private val entrantPairGenerator: EntrantPairGenerator,
    completeStage: (AntiBingoState) -> Unit,
) : AntiBingoStage(completeStage) {

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {
    // Do nothing
  }

  override fun raceUpdate(race: RacetimeRace) {

    if (race.entrants.size < 2 || race.entrants.any { it.status != READY }) {
      return
    }

    val entrants = race.entrants.map { it.user }

    // TODO DM entrants

    completeStage(AntiBingoState(entrants, entrantPairGenerator.generatePairs(entrants)))
  }

  override fun handleCommand(command: ChatMessage) {
    // Do nothing
  }
}
