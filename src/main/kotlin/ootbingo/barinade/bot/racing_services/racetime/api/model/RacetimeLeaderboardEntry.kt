package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeLeaderboardEntry(
  var user: RacetimeUser = RacetimeUser(),
)
