package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser

sealed class RacetimeMessage

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

class RaceUpdate(var race: RacetimeRace) : RacetimeMessage()
