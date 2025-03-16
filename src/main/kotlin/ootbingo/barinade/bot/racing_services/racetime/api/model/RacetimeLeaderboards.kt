package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeLeaderboards(
  var leaderboards: List<RacetimeLeaderboard> = emptyList(),
)
