package ootbingo.barinade.bot.racing_services.racetime.api.model

import java.time.Instant

data class RacetimeRace(
    var name: String = "",
    var status: RacetimeRaceStatus = RacetimeRaceStatus.OPEN,
    var goal: RacetimeRaceGoal = RacetimeRaceGoal(),
    var info: String = "",
    var entrants: List<RacetimeEntrant> = emptyList(),
    var endedAt: Instant? = null,
    var recorded: Boolean = false
) {

  data class RacetimeRaceGoal(
      var name: String = "",
      var custom: Boolean = true
  )

  enum class RacetimeRaceStatus {
    OPEN, INVITATIONAL, PENDING, IN_PROGRESS, FINISHED, CANCELLED
  }
}
