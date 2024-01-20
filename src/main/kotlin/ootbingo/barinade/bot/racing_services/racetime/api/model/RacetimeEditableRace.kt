package ootbingo.barinade.bot.racing_services.racetime.api.model

import kotlinx.serialization.Serializable

@Serializable
class RacetimeEditableRace(
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
