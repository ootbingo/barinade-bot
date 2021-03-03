package ootbingo.barinade.bot.racing_services.racetime.racing.oauth

import java.time.Duration
import java.time.Instant

class Token(val token: String = "", val expiryTime: Instant = Instant.EPOCH) {
  fun expired(now: Instant) = now.let{ it.isAfter(expiryTime) || Duration.between(it, expiryTime).toHours() < 2 }
}
