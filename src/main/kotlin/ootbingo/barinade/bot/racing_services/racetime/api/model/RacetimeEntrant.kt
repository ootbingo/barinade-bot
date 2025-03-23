package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.configuration.EntrantStatusSerializer
import kotlin.time.Duration

@Serializable
data class RacetimeEntrant(
  var user: RacetimeUser? = null,
  var finishTime: Duration? = null,
  var place: Int? = null,
  var status: RacetimeEntrantStatus? = null,
) {

  @Serializable(EntrantStatusSerializer::class)
  enum class RacetimeEntrantStatus {

    REQUESTED, INVITED, DECLINED, READY, NOT_READY, IN_PROGRESS, DONE, DNF, DQ
  }
}
