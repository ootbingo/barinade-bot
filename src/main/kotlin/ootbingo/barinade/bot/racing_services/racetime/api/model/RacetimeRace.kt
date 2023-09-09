package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.configuration.SerializableInstant
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClientConfiguration

@Serializable
data class RacetimeRace(
    var name: String = "",
    var status: RacetimeRaceStatus = RacetimeRaceStatus.OPEN,
    var goal: RacetimeRaceGoal = RacetimeRaceGoal(),
    var info: String = "",
    var entrants: List<RacetimeEntrant> = emptyList(),
    var endedAt: SerializableInstant? = null,
    var recorded: Boolean = false,
    var version: Int = 0,
    var teamRace: Boolean = false,
) {

  @Serializable
  data class RacetimeRaceGoal(
      var name: String = "",
      var custom: Boolean = true,
  )

  @Serializable(RacetimeHttpClientConfiguration.RaceStatusSerializer::class)
  enum class RacetimeRaceStatus {

    OPEN, INVITATIONAL, PENDING, IN_PROGRESS, FINISHED, CANCELLED
  }
}
