package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.properties.BingoRaceProperties
import ootbingo.barinade.bot.statistics.validation.IdBlacklistRaceGoalValidator.IdType.*
import org.springframework.stereotype.Service

@Service
class IdBlacklistRaceGoalValidator {

  private val blacklist by lazy { BingoRaceProperties.blacklist }
  private val whitelist by lazy { BingoRaceProperties.whitelist.map { it.raceId } }

  fun validateRaceId(raceId: String): IdType {
    return when (raceId) {
      in blacklist -> BLACKLISTED
      in whitelist -> WHITELISTED
      else -> NEUTRAL
    }
  }

  enum class IdType {
    BLACKLISTED,
    WHITELISTED,
    NEUTRAL,
  }
}
