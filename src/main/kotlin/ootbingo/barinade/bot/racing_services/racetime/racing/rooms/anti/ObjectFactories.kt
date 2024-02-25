package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser

fun antiBingoState(
  slug: String = "",
  entrants: List<RacetimeUser> = emptyList(),
  entrantMappings: List<AntiBingoState.EntrantMapping> = emptyList(),
) = AntiBingoState(slug, entrants, entrantMappings)
