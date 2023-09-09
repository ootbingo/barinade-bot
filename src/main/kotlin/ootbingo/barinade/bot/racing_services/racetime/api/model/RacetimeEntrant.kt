package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClientConfiguration
import kotlin.time.Duration

@Serializable
data class RacetimeEntrant(
    var user: RacetimeUser = RacetimeUser(),
    var finishTime: Duration? = null,
    var place: Int? = null,
    var status: RacetimeEntrantStatus? = null,
) {

  @Serializable(RacetimeHttpClientConfiguration.EntrantStatusSerializer::class)
  enum class RacetimeEntrantStatus {

    REQUESTED, INVITED, DECLINED, READY, NOT_READY, IN_PROGRESS, DONE, DNF, DQ
  }
}
