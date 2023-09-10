package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeCategory(
    var currentRaces: List<RacetimeRace> = emptyList(),
)
