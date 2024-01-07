package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.configuration.RaceStatusSerializer
import ootbingo.barinade.bot.configuration.SerializableInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

@Serializable
data class RacetimeRace(
    var name: String = "",
    var slug: String = "",
    var status: RacetimeRaceStatus = RacetimeRaceStatus.OPEN,
    var goal: RacetimeRaceGoal = RacetimeRaceGoal(),
    var info: String = "",
    var infoBot: String? = "",
    var infoUser: String = "",
    var entrants: List<RacetimeEntrant> = emptyList(),
    var startDelay: Duration = 15.seconds,
    var endedAt: SerializableInstant? = null,
    var unlisted: Boolean = false,
    var timeLimit: Duration = 1.days,
    var timeLimitAutoComplete: Boolean = true,
    var streamingRequired: Boolean = false,
    var autoStart: Boolean = false,
    var recorded: Boolean = false,
    var version: Int = 0,
    var teamRace: Boolean = false,
    var requireEvenTeams: Boolean = false,
    var allowComments: Boolean = true,
    var hideComments: Boolean = false,
    var allowPreraceChat: Boolean = true,
    var allowMidraceChat: Boolean = true,
    var allowNonEntrantChat: Boolean = true,
    var chatMessageDelay: Duration = Duration.ZERO,
) {

  @Serializable
  data class RacetimeRaceGoal(
      var name: String = "",
      var custom: Boolean = true,
  )

  @Serializable(RaceStatusSerializer::class)
  enum class RacetimeRaceStatus {

    OPEN, INVITATIONAL, PENDING, IN_PROGRESS, FINISHED, CANCELLED
  }

  fun toEditableRace() = RacetimeEditableRace(
      if (!goal.custom) goal.name else null,
//      if (goal.custom) goal.name else null,
      teamRace,
      status == RacetimeRaceStatus.INVITATIONAL,
      unlisted,
      infoUser,
      infoBot ?: "",
      requireEvenTeams,
      startDelay.inWholeSeconds.toInt(),
      timeLimit.inWholeHours.toInt(),
      timeLimitAutoComplete,
      streamingRequired,
      autoStart,
      allowComments,
      hideComments,
      allowPreraceChat,
      allowMidraceChat,
      allowNonEntrantChat,
      chatMessageDelay.inWholeSeconds.toInt(),
  )
}
