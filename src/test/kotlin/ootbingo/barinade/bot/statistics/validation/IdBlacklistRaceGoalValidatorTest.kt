package ootbingo.barinade.bot.statistics.validation

import ootbingo.barinade.bot.properties.BingoRaceProperties
import ootbingo.barinade.bot.properties.model.WhitelistBingo
import ootbingo.barinade.bot.statistics.validation.IdBlacklistRaceGoalValidator.IdType.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.random.Random

class IdBlacklistRaceGoalValidatorTest {

  private val validator = IdBlacklistRaceGoalValidator()

  @Test
  internal fun noBingoWhenRaceIdBlacklisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.blacklist = listOf(raceId)

    raceId isIdType BLACKLISTED
  }

  @Test
  internal fun isBingoWhenRaceIdWhitelisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    raceId isIdType WHITELISTED
  }

  @Test
  internal fun noBingoWhenRaceIdBlacklistedAndWhitelisted() {

    val raceId = Random.nextInt(0, 999999).toString()
    BingoRaceProperties.blacklist = listOf(raceId)
    BingoRaceProperties.whitelist = listOf(WhitelistBingo(raceId, null))

    raceId isIdType BLACKLISTED
  }

  @Test
  internal fun neutralWhenRaceOnNeitherList() {

    val raceId = UUID.randomUUID().toString()
    BingoRaceProperties.blacklist = (1..5).map { UUID.randomUUID().toString() }
    BingoRaceProperties.whitelist = (1..5).map { UUID.randomUUID().toString() }.map { WhitelistBingo(it, null) }

    raceId isIdType NEUTRAL
  }

  //<editor-fold desc="Helper">

  private infix fun String.isIdType(expectedIdType: IdBlacklistRaceGoalValidator.IdType) {
    assertThat(validator.validateRaceId(this)).isEqualTo(expectedIdType)
  }

  //</editor-fold>
}
