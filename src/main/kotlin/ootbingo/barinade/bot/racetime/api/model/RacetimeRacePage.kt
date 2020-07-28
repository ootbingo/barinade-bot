package ootbingo.barinade.bot.racetime.api.model

data class RacetimeRacePage(
    var count: Int = 0,
    var numPages: Int = 0,
    var races: List<RacetimeRace> = emptyList()
)
