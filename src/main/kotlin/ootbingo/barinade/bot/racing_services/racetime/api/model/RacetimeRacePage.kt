package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeRacePage(
    var count: Int = 0,
    var numPages: Int = 0,
    var races: List<RacetimeRace> = emptyList(),
)
