package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEntrant.RacetimeEntrantStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage

class RaceOpenStage(
  private val entrantPairGenerator: EntrantPairGenerator,
  completeStage: (AntiBingoState) -> Unit,
  private val sendDm: (String, RacetimeUser) -> Unit,
) : AntiBingoStage(completeStage) {

  private var slug: String = ""

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {
    slug = initialState.slug
  }

  override fun raceUpdate(race: RacetimeRace) {

    if (race.entrants.size < 2 || race.entrants.any { it.status != READY }) {
      return
    }

    val entrants = race.entrants.map { it.user }
    val entrantMappings = entrantPairGenerator.generatePairs(entrants)

    entrantMappings.forEach {
      sendDm.invoke("Please choose a row for ${it.choosesFor.name}", it.entrant)
    }

    completeStage(AntiBingoState(slug, entrants, entrantMappings))
  }

  override fun handleCommand(command: ChatMessage) {
    // Do nothing
  }
}
