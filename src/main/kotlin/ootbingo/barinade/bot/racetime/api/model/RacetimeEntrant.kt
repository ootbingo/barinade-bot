package ootbingo.barinade.bot.racetime.api.model

import java.time.Duration

data class RacetimeEntrant(
    var user: RacetimeUser = RacetimeUser(),
    var finishTime: Duration? = null,
    var place: Int? = null
)
