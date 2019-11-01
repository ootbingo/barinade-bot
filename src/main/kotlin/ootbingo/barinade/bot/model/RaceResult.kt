package ootbingo.barinade.bot.model

import java.time.Duration

open class RaceResult(var race: Race, val player: Player, val place: Long, val time: Duration, val message: String) {

  open fun isForfeit(): Boolean = time.isNegative
}
