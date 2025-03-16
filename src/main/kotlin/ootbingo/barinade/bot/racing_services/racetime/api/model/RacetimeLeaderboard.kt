package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeLeaderboard(
  var goal: String = "",
  var rankings: List<RacetimeLeaderboardEntry> = emptyList(),
)
