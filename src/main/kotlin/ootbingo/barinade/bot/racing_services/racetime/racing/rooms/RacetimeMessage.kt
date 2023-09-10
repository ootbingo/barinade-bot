package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import kotlinx.serialization.Serializable
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser

sealed class RacetimeMessage

@Serializable
class ChatMessage(
    var id: String = "",
    var user: RacetimeUser? = null,
    var bot: String? = null,
    var message: String = "",
    var messagePlain: String = "",
    var highlight: Boolean = false,
    var isBot: Boolean = false,
    var isSystem: Boolean = false,
) : RacetimeMessage()

@Serializable
class RaceUpdate(var race: RacetimeRace) : RacetimeMessage()
