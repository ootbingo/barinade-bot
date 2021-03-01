package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import java.time.Instant

sealed class RacetimeMessage

class ChatMessage(
    var id: String = "",
    var user: RacetimeUser? = null,
    var bot: String? = null,
    var postedAt: Instant = Instant.EPOCH,
    var message: String = "",
    var messagePlain: String = "",
    var highlight: Boolean = false,
    var isBot: Boolean = false,
    var isSystem: Boolean = false
) : RacetimeMessage()

class RaceUpdate(var race: RacetimeRace): RacetimeMessage()
