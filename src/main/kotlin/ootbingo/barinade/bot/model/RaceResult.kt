package ootbingo.barinade.bot.model

import java.time.Duration

data class RaceResult(var race: Race, val player: Player, val place: Long, val time: Duration, val message: String)
