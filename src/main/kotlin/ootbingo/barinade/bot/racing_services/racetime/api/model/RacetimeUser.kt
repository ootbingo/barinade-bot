package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeUser(
    var id: String = "",
    var name: String = "",
)
