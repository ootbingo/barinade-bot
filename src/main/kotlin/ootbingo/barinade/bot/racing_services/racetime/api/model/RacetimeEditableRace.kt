package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RacetimeEditableRace(
  val goal: String?,
  val teamRace: Boolean,
  val invitational: Boolean,
  val unlisted: Boolean,
  val infoUser: String,
  var infoBot: String,
  val requireEvenTeams: Boolean,
  val startDelay: Int,
  val timeLimit: Int,
  val timeLimitAutoComplete: Boolean,
  val streamingRequired: Boolean,
  var autoStart: Boolean,
  val allowComments: Boolean,
  val hideComments: Boolean,
  val allowPreraceChat: Boolean,
  val allowMidraceChat: Boolean,
  val allowNonEntrantChat: Boolean,
  var chatMessageDelay: Int,
)

fun newBingoRace(teamRace: Boolean) = RacetimeEditableRace(
  goal = "Bingo",
  teamRace = teamRace,
  invitational = false,
  unlisted = false,
  infoUser = "",
  infoBot = "",
  requireEvenTeams = false,
  startDelay = 15,
  timeLimit = 24,
  timeLimitAutoComplete = true,
  streamingRequired = false,
  autoStart = true,
  allowComments = true,
  hideComments = false,
  allowPreraceChat = true,
  allowMidraceChat = true,
  allowNonEntrantChat = true,
  chatMessageDelay = 0,
)
